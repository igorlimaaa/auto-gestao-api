package br.com.gestaocondial.autogestao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.gestaocondial.autogestao.banking.InterApiException;
import br.com.gestaocondial.autogestao.banking.InterCobranca;
import br.com.gestaocondial.autogestao.banking.InterCobrancaClient;
import br.com.gestaocondial.autogestao.banking.SituacoesDoBanco;
import br.com.gestaocondial.autogestao.domain.CobrancaBancoPendente;
import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.enumeration.MotivoDePendenciaEnum;
import br.com.gestaocondial.autogestao.exception.CondominioNaoEncontradoException;
import br.com.gestaocondial.autogestao.repository.CobrancaBancoPendenteRepository;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.service.SincronizacaoBancariaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * As chamadas ao banco acontecem <b>fora</b> de transação: segurar uma conexão do pool durante
 * requisições HTTP de lote inteiro é pedir para esgotá-lo. Cada gravação é uma transação curta
 * própria ({@link TransactionTemplate}, e não {@code @Transactional}, justamente porque
 * métodos do mesmo bean não passam pelo proxy), e a falha de uma cobrança não derruba o lote.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacaoBancariaImpl implements SincronizacaoBancariaService {

	/** "1203-26" → unidade "1203", ano "26". A unidade pode conter hífen ("A-3-26"): o ano é o último trecho. */
	private static final Pattern SEU_NUMERO = Pattern.compile("^(.+)-(\\d{2}|\\d{4})$");

	private static final int DIAS_PARA_TRAS = 30;

	private static final int DIAS_PARA_FRENTE = 60;

	/** Falhas seguidas num condomínio (certificado vencido, credencial revogada) interrompem o resto dele. */
	private static final int FALHAS_SEGUIDAS_PARA_DESISTIR = 3;

	private final InterCobrancaClient banco;

	private final CobrancaOrdinariaRepository cobrancaRepository;

	private final CobrancaBancoPendenteRepository pendenciaRepository;

	private final CondominioRepository condominioRepository;

	private final UnidadeRepository unidadeRepository;

	private final TransactionTemplate transacao;

	/** Impede duas execuções simultâneas (job agendado + disparo manual) de duplicarem chamadas ao banco. */
	private final AtomicBoolean emExecucao = new AtomicBoolean(false);

	@Override
	public void importar(Long idCondominio) {
		executarExclusivo("importação", () -> {
			LocalDate hoje = LocalDate.now();
			for (Condominio condominio : condominios(idCondominio)) {
				if (!banco.integrado(condominio.getId())) {
					continue;
				}
				importarDoCondominio(condominio, hoje.minusDays(DIAS_PARA_TRAS), hoje.plusDays(DIAS_PARA_FRENTE));
			}
		});
	}

	@Override
	public void sincronizarPendentes(Long idCondominio) {
		executarExclusivo("sincronização", () -> {
			List<Pendente> pendentes = transacao.execute(status -> cobrancaRepository
					.findPendentesDeSincronizacao(idCondominio, SituacoesDoBanco.FINAIS).stream()
					.map(c -> new Pendente(c.getId(), c.getUnidade().getCondominio().getId(), c.getCodigoSolicitacao()))
					.toList());
			if (pendentes == null) {
				return;
			}
			Long condominioAtual = null;
			int falhasSeguidas = 0;
			boolean desistiu = false;
			for (Pendente pendente : pendentes) {
				if (!pendente.idCondominio().equals(condominioAtual)) {
					condominioAtual = pendente.idCondominio();
					falhasSeguidas = 0;
					desistiu = false;
				}
				if (desistiu || !banco.integrado(pendente.idCondominio())) {
					continue;
				}
				try {
					InterCobranca resposta = banco.recuperar(pendente.idCondominio(), pendente.codigoSolicitacao());
					transacao.executeWithoutResult(status -> cobrancaRepository.findById(pendente.id())
							.ifPresent(c -> atualizarMutaveis(c, resposta)));
					falhasSeguidas = 0;
				} catch (RuntimeException e) {
					log.warn("Cobrança {} do condomínio {} não sincronizada: {}", pendente.codigoSolicitacao(),
							pendente.idCondominio(), e.getMessage());
					if (++falhasSeguidas >= FALHAS_SEGUIDAS_PARA_DESISTIR) {
						log.error("Condomínio {}: {} falhas seguidas, pulando o restante desta rodada.",
								pendente.idCondominio(), falhasSeguidas);
						desistiu = true;
					}
				}
			}
		});
	}

	private void importarDoCondominio(Condominio condominio, LocalDate de, LocalDate ate) {
		List<InterCobranca> doBanco;
		try {
			doBanco = banco.listar(condominio.getId(), de, ate);
		} catch (InterApiException e) {
			// Sem a listagem não dá para saber o que está pendente: mantém o retrato anterior.
			log.error("Importação do condomínio {} falhou: {}", condominio.getId(), e.getMessage());
			return;
		}
		int gravadas = 0;
		List<CobrancaBancoPendente> pendencias = new ArrayList<>();
		for (InterCobranca item : doBanco) {
			try {
				Resultado resultado = transacao.execute(status -> gravar(condominio, item));
				if (resultado == null) {
					continue;
				}
				if (resultado.gravada()) {
					gravadas++;
				} else if (resultado.motivo() != null) {
					pendencias.add(pendencia(condominio, item, resultado.motivo()));
				}
			} catch (RuntimeException e) {
				log.warn("Cobrança {} do condomínio {} não importada: {}", codigoDe(item), condominio.getId(),
						e.getMessage());
			}
		}
		registrarPendencias(condominio.getId(), pendencias);
		log.info("Condomínio {}: {} cobrança(s) no banco, {} gravada(s), {} pendente(s) de vínculo.",
				condominio.getId(), doBanco.size(), gravadas, pendencias.size());
	}

	/** Substitui o retrato de pendências do condomínio pelo da importação que acabou de rodar. */
	private void registrarPendencias(Long idCondominio, List<CobrancaBancoPendente> pendencias) {
		transacao.executeWithoutResult(status -> {
			pendenciaRepository.apagarDoCondominio(idCondominio);
			pendenciaRepository.saveAll(pendencias);
		});
	}

	private static CobrancaBancoPendente pendencia(Condominio condominio, InterCobranca item, MotivoDePendenciaEnum motivo) {
		InterCobranca.Cobranca dados = item.cobranca();
		CobrancaBancoPendente pendente = new CobrancaBancoPendente();
		pendente.setIdCondominio(condominio.getId());
		pendente.setCodigoSolicitacao(dados.codigoSolicitacao());
		pendente.setSeuNumero(dados.seuNumero());
		pendente.setDataVencimento(dados.dataVencimento());
		pendente.setValor(dados.valorNominal() != null ? dados.valorNominal().doubleValue() : null);
		pendente.setMotivo(motivo);
		return pendente;
	}

	/** Grava a cobrança do banco; se não puder, devolve o motivo (ou nada, para item sem código). */
	private Resultado gravar(Condominio condominio, InterCobranca item) {
		InterCobranca.Cobranca dados = item.cobranca();
		if (dados == null || dados.codigoSolicitacao() == null) {
			return new Resultado(false, null);
		}
		Optional<CobrancaOrdinaria> existente = cobrancaRepository.findByCodigoSolicitacao(dados.codigoSolicitacao());
		CobrancaOrdinaria cobranca = existente.orElse(null);
		if (cobranca == null) {
			Localizacao localizacao = localizarOuCriar(condominio, item);
			if (localizacao.cobranca() == null) {
				return new Resultado(false, localizacao.motivo());
			}
			cobranca = localizacao.cobranca();
			vincular(cobranca, item);
		} else {
			completarImutaveis(cobranca, item);
			if (jaResolvida(cobranca)) {
				// Paga, cancelada ou expirada: nada mais muda no banco. Regravar só moveria o
				// "conferida em" a cada importação, mesmo critério da reconsulta horária.
				return new Resultado(true, null);
			}
		}
		atualizarMutaveis(cobranca, item);
		return new Resultado(true, null);
	}

	/** Mesmo critério de {@code findPendentesDeSincronizacao}: o que sai da reconsulta horária. */
	private static boolean jaResolvida(CobrancaOrdinaria cobranca) {
		return cobranca.getDataPagamento() != null || SituacoesDoBanco.finalizada(cobranca.getSituacaoBanco());
	}

	/**
	 * Casa a cobrança do banco com a unidade pelo {@code seuNumero} ("unidade-ano") e com a
	 * competência pelo mês do vencimento — o ano sozinho não distingue os 12 boletos da mesma
	 * unidade. O banco é a única origem: se não houver registro da competência, ele é criado aqui.
	 * Um registro local sem vínculo (de antes da integração) é aproveitado em vez de duplicado.
	 */
	private Localizacao localizarOuCriar(Condominio condominio, InterCobranca item) {
		InterCobranca.Cobranca dados = item.cobranca();
		List<Unidade> candidatas = unidadesDoSeuNumero(condominio.getId(), dados.seuNumero());
		if (candidatas.size() > 1) {
			log.warn("Cobrança {} (seuNumero={}): o número existe em mais de um bloco do condomínio {}.",
					dados.codigoSolicitacao(), dados.seuNumero(), condominio.getId());
			return Localizacao.sem(MotivoDePendenciaEnum.UNIDADE_AMBIGUA);
		}
		if (candidatas.isEmpty() || dados.dataVencimento() == null) {
			log.warn("Cobrança {} (seuNumero={}): nenhuma unidade correspondente no condomínio {}.",
					dados.codigoSolicitacao(), dados.seuNumero(), condominio.getId());
			return Localizacao.sem(MotivoDePendenciaEnum.SEM_UNIDADE);
		}
		Unidade unidade = candidatas.get(0);
		if (unidade.isentaDaTaxaOrdinaria()) {
			log.warn("Cobrança {} (seuNumero={}) é de uma unidade isenta da taxa (condomínio {}); ignorada. "
					+ "Cancele-a no banco.", dados.codigoSolicitacao(), dados.seuNumero(), condominio.getId());
			return Localizacao.sem(MotivoDePendenciaEnum.UNIDADE_ISENTA);
		}
		LocalDate competencia = dados.dataVencimento().withDayOfMonth(1);
		Optional<CobrancaOrdinaria> local = cobrancaRepository.findByUnidadeIdAndCompetencia(unidade.getId(),
				competencia);
		if (local.isPresent()) {
			String jaVinculada = local.get().getCodigoSolicitacao();
			if (jaVinculada != null && !jaVinculada.equals(dados.codigoSolicitacao())) {
				log.warn("Unidade {} já tem a cobrança {} na competência {}; {} ignorada.", unidade.getId(),
						jaVinculada, competencia, dados.codigoSolicitacao());
				return Localizacao.sem(MotivoDePendenciaEnum.COMPETENCIA_DUPLICADA);
			}
			return new Localizacao(local.get(), null);
		}
		CobrancaOrdinaria nova = new CobrancaOrdinaria();
		nova.setUnidade(unidade);
		nova.setCompetencia(competencia);
		return new Localizacao(nova, null);
	}

	/** Unidades do condomínio com o número do {@code seuNumero}; vazio se o formato não for "número-ano". */
	private List<Unidade> unidadesDoSeuNumero(Long idCondominio, String seuNumero) {
		if (seuNumero == null) {
			return List.of();
		}
		Matcher m = SEU_NUMERO.matcher(seuNumero.trim());
		if (!m.matches()) {
			return List.of();
		}
		// Mais de uma: mesmo número em blocos diferentes, e o seuNumero não traz o bloco.
		return unidadeRepository.findByCondominioIdAndNumero(idCondominio, m.group(1));
	}

	/** Primeira ligação com o banco: tudo que ele informa vale mais que o cadastro local. */
	private void vincular(CobrancaOrdinaria cobranca, InterCobranca item) {
		InterCobranca.Cobranca dados = item.cobranca();
		cobranca.setCodigoSolicitacao(dados.codigoSolicitacao());
		cobranca.setSeuNumero(dados.seuNumero());
		cobranca.setDataEmissao(dados.dataEmissao());
		cobranca.setDataVencimento(dados.dataVencimento());
		if (dados.valorNominal() != null) {
			cobranca.setValor(dados.valorNominal().doubleValue());
		}
		if (item.boleto() != null) {
			cobranca.setNossoNumero(item.boleto().nossoNumero());
			cobranca.setCodigoBarras(item.boleto().codigoBarras());
			cobranca.setLinhaDigitavel(item.boleto().linhaDigitavel());
		}
		if (item.pix() != null) {
			cobranca.setTxid(item.pix().txid());
			cobranca.setPixCopiaCola(item.pix().pixCopiaECola());
		}
	}

	/** Já vinculada: os dados imutáveis só são preenchidos se estiverem faltando. */
	private void completarImutaveis(CobrancaOrdinaria cobranca, InterCobranca item) {
		if (item.boleto() != null) {
			if (cobranca.getNossoNumero() == null) {
				cobranca.setNossoNumero(item.boleto().nossoNumero());
			}
			if (cobranca.getCodigoBarras() == null) {
				cobranca.setCodigoBarras(item.boleto().codigoBarras());
			}
			if (cobranca.getLinhaDigitavel() == null) {
				cobranca.setLinhaDigitavel(item.boleto().linhaDigitavel());
			}
		}
		if (item.pix() != null) {
			if (cobranca.getTxid() == null) {
				cobranca.setTxid(item.pix().txid());
			}
			if (cobranca.getPixCopiaCola() == null) {
				cobranca.setPixCopiaCola(item.pix().pixCopiaECola());
			}
		}
	}

	private void atualizarMutaveis(CobrancaOrdinaria cobranca, InterCobranca item) {
		InterCobranca.Cobranca dados = item.cobranca();
		if (dados == null) {
			return;
		}
		cobranca.setSituacaoBanco(dados.situacao());
		// O banco manda no valor e no vencimento enquanto a cobrança está em aberto: se alguém os
		// alterar lá, acompanhamos. A competência (chave de unicidade) não muda.
		if (dados.dataVencimento() != null) {
			cobranca.setDataVencimento(dados.dataVencimento());
		}
		if (dados.valorNominal() != null) {
			cobranca.setValor(dados.valorNominal().doubleValue());
		}
		cobranca.setOrigemRecebimento(dados.origemRecebimento());
		cobranca.setValorTotalRecebido(
				dados.valorTotalRecebido() != null ? dados.valorTotalRecebido().doubleValue() : null);
		if (SituacoesDoBanco.paga(dados.situacao())) {
			cobranca.setDataPagamento(dados.dataSituacao() != null ? dados.dataSituacao() : LocalDate.now());
		}
		cobranca.setDataUltimaSincronizacao(LocalDateTime.now());
		cobrancaRepository.save(cobranca);
	}

	private List<Condominio> condominios(Long idCondominio) {
		if (idCondominio == null) {
			return condominioRepository.findAll();
		}
		return List.of(condominioRepository.findById(idCondominio)
				.orElseThrow(() -> new CondominioNaoEncontradoException("Condomínio não encontrado: " + idCondominio)));
	}

	private void executarExclusivo(String nome, Runnable trabalho) {
		if (!emExecucao.compareAndSet(false, true)) {
			log.info("{} com o banco ignorada: outra já está em andamento.", nome);
			return;
		}
		try {
			trabalho.run();
		} finally {
			emExecucao.set(false);
		}
	}

	private static String codigoDe(InterCobranca item) {
		return item.cobranca() == null ? null : item.cobranca().codigoSolicitacao();
	}

	private record Pendente(Long id, Long idCondominio, String codigoSolicitacao) {
	}

	/** Resultado de gravar um item: gravada, ou o motivo de não ter virado cobrança. */
	private record Resultado(boolean gravada, MotivoDePendenciaEnum motivo) {
	}

	private record Localizacao(CobrancaOrdinaria cobranca, MotivoDePendenciaEnum motivo) {

		static Localizacao sem(MotivoDePendenciaEnum motivo) {
			return new Localizacao(null, motivo);
		}

	}

}
