package br.com.gestaocondial.autogestao.job;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.exception.CondominioNaoEncontradoException;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

/**
 * Gera, com antecedência, o registro de cobrança da taxa ordinária de cada unidade ativa.
 *
 * <p>A "competência vigente" de um condomínio (com {@code diaVencimentoTaxaOrdinaria}
 * cadastrado) é sempre calculada a partir de hoje, nunca do histórico de cobranças já geradas:
 * é o mês corrente, a menos que o gatilho de 10 dias depois do vencimento daquele mês já tenha
 * passado — nesse caso já é o mês seguinte (e assim por diante, se vários ciclos já tiverem
 * passado). Isso evita gerar uma competência retroativa quando o condomínio é configurado (ou
 * o job roda pela primeira vez) depois que o próprio gatilho do mês corrente já passou — ex.:
 * vencimento cadastrado no dia 3, condomínio configurado no dia 14: o gatilho do mês corrente
 * (dia 13) já passou, então a competência vigente é a do mês seguinte, não a do mês corrente.
 *
 * <p>Ver {@link #competenciaVigente(int, LocalDate)}. Dar ~10 dias de antecedência ao gatilho
 * (vencimento + 10 dias) dá ao administrador tempo de preencher PIX/linha digitável antes do
 * próximo vencimento.</p>
 *
 * <p>Idempotente: {@code existsByUnidadeIdAndCompetencia} é conferido antes de cada insert, e a
 * constraint única {@code uk_cobranca_unidade_competencia} (migration V8) é a rede de segurança
 * caso o job rode mais de uma vez no mesmo dia.</p>
 */
@Component
@RequiredArgsConstructor
public class GeradorDeCobrancaOrdinariaJob {

	private final CondominioRepository condominioRepository;

	private final UnidadeRepository unidadeRepository;

	private final CobrancaOrdinariaRepository cobrancaRepository;

	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void gerar() {
		gerar(LocalDate.now());
	}

	/** Pacote-privado: permite testar a lógica de datas passando um "hoje" fixo. */
	void gerar(LocalDate hoje) {
		for (Condominio condominio : condominioRepository.findByDiaVencimentoTaxaOrdinariaIsNotNull()) {
			gerarParaCondominio(condominio, hoje);
		}
	}

	/**
	 * Disparo manual (endpoint {@code POST /cobrancaOrdinaria/gerar}), restrito a um único
	 * condomínio — usa a data de hoje de verdade, nunca uma simulada. Mesma lógica e mesma
	 * idempotência do job agendado, só que sob demanda.
	 */
	@Transactional
	public void gerarParaCondominio(Long idCondominio) {
		Condominio condominio = condominioRepository.findById(idCondominio)
				.orElseThrow(() -> new CondominioNaoEncontradoException("Condomínio não encontrado: " + idCondominio));
		if (condominio.getDiaVencimentoTaxaOrdinaria() == null) {
			throw new IllegalArgumentException(
					"Condomínio ainda não tem o dia de vencimento da taxa ordinária cadastrado.");
		}
		gerarParaCondominio(condominio, LocalDate.now());
	}

	private void gerarParaCondominio(Condominio condominio, LocalDate hoje) {
		int dia = condominio.getDiaVencimentoTaxaOrdinaria();
		YearMonth mesVigente = competenciaVigente(dia, hoje);
		LocalDate competenciaAlvo = mesVigente.atDay(1);
		LocalDate vencimentoAlvo = diaNoMes(dia, mesVigente);

		for (Unidade unidade : unidadeRepository.findByCondominioIdAndAtivaTrue(condominio.getId())) {
			if (cobrancaRepository.existsByUnidadeIdAndCompetencia(unidade.getId(), competenciaAlvo)) {
				continue;
			}
			CobrancaOrdinaria cobranca = new CobrancaOrdinaria();
			cobranca.setUnidade(unidade);
			cobranca.setCompetencia(competenciaAlvo);
			cobranca.setDataVencimento(vencimentoAlvo);
			cobranca.setValor(condominio.getValorTaxaCondominial());
			cobrancaRepository.save(cobranca);
		}
	}

	/**
	 * Mês cuja cobrança deveria existir hoje, dados só o dia de vencimento — não depende de
	 * nenhuma cobrança já ter sido gerada antes. É o mês corrente, a menos que o gatilho daquele
	 * mês (vencimento + 10 dias) já tenha passado (ou seja hoje), caso em que já rolou para o mês
	 * seguinte — repetindo o teste até achar um mês cujo gatilho ainda não chegou.
	 */
	static YearMonth competenciaVigente(int dia, LocalDate hoje) {
		YearMonth mes = YearMonth.from(hoje);
		while (!diaNoMes(dia, mes).plusDays(10).isAfter(hoje)) {
			mes = mes.plusMonths(1);
		}
		return mes;
	}

	/** Vencimento clampado ao último dia do mês-alvo — ex.: dia 31 em fevereiro vira dia 28/29. */
	static LocalDate diaNoMes(int dia, YearMonth mes) {
		return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
	}

}
