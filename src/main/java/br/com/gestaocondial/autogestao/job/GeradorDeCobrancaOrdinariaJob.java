package br.com.gestaocondial.autogestao.job;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

/**
 * Gera, com antecedência, o registro de cobrança da taxa ordinária de cada unidade ativa.
 *
 * <p>Regra por condomínio (com {@code diaVencimentoTaxaOrdinaria} cadastrado):</p>
 * <ul>
 * <li><b>Bootstrap</b>: se o condomínio ainda não tem nenhuma cobrança gerada, gera a
 * competência do mês atual imediatamente — não espera o gatilho abaixo, porque não existe um
 * ciclo anterior do qual contar os "10 dias depois".</li>
 * <li><b>Regime</b>: senão, só gera a competência seguinte quando hoje for exatamente 10 dias
 * depois do vencimento deste mês (ex.: vencimento dia 10 → gera no dia 20 a competência do mês
 * seguinte), dando ao administrador ~20 dias para preencher PIX/linha digitável antes do
 * próximo vencimento.</li>
 * </ul>
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

	private void gerarParaCondominio(Condominio condominio, LocalDate hoje) {
		int dia = condominio.getDiaVencimentoTaxaOrdinaria();
		LocalDate vencimentoDesteMes = diaNoMes(dia, YearMonth.from(hoje));

		LocalDate competenciaAlvo;
		LocalDate vencimentoAlvo;

		boolean bootstrap = !cobrancaRepository.existsByUnidadeCondominioId(condominio.getId());
		if (bootstrap) {
			competenciaAlvo = hoje.withDayOfMonth(1);
			vencimentoAlvo = vencimentoDesteMes;
		} else {
			if (!hoje.isEqual(vencimentoDesteMes.plusDays(10))) {
				return;
			}
			YearMonth proximoMes = YearMonth.from(hoje).plusMonths(1);
			competenciaAlvo = proximoMes.atDay(1);
			vencimentoAlvo = diaNoMes(dia, proximoMes);
		}

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

	/** Vencimento clampado ao último dia do mês-alvo — ex.: dia 31 em fevereiro vira dia 28/29. */
	static LocalDate diaNoMes(int dia, YearMonth mes) {
		return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
	}

}
