package br.com.gestaocondial.autogestao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.gestaocondial.autogestao.banking.InterCobranca;
import br.com.gestaocondial.autogestao.banking.InterCobrancaClient;
import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.service.SincronizacaoBancariaService;

import tools.jackson.databind.json.JsonMapper;

/**
 * Usa as respostas reais do Inter (listagem e detalhe) como fixture: valida a desserialização
 * (valorNominal ora string, ora número; campos desconhecidos) e o fluxo de importação e
 * sincronização contra o schema real das migrations, com o cliente HTTP simulado.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SincronizacaoBancariaImplTest {

	@Autowired
	private SincronizacaoBancariaService sincronizacao;

	@Autowired
	private CobrancaOrdinariaRepository cobrancaRepository;

	@Autowired
	private CondominioRepository condominioRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@MockitoBean
	private InterCobrancaClient banco;

	private Condominio condominio;

	@BeforeEach
	void prepararCenario() throws Exception {
		cobrancaRepository.deleteAll();
		unidadeRepository.deleteAll();
		condominioRepository.deleteAll();

		Endereco endereco = enderecoRepository.save(new Endereco());
		Condominio novo = new Condominio();
		novo.setEndereco(endereco);
		novo.setPossuiTaxaExtra(false);
		condominio = condominioRepository.save(novo);
		unidade("1404");
		unidade("1203");

		when(banco.integrado(condominio.getId())).thenReturn(true);
		when(banco.listar(eq(condominio.getId()), any(), any())).thenReturn(ler("/inter/listagem.json", InterCobranca.Pagina.class).cobrancas());
	}

	/** O H2 é compartilhado entre as classes de teste: sem limpar, o que criamos vaza (ex.: endereço id 10). */
	@AfterEach
	void limparDados() {
		cobrancaRepository.deleteAll();
		unidadeRepository.deleteAll();
		condominioRepository.deleteAll();
		enderecoRepository.deleteAll();
	}

	@Test
	void importaGravandoOsDadosImutaveisCasandoPorUnidadeECompetencia() {
		sincronizacao.importar(condominio.getId());

		CobrancaOrdinaria paga = cobrancaRepository.findByCodigoSolicitacao("c0a7aee1-f83f-4c7a-a753-c04542c3c279").orElseThrow();
		assertEquals("1203", paga.getUnidade().getNumero());
		assertEquals(LocalDate.of(2026, 10, 1), paga.getCompetencia());
		assertEquals(LocalDate.of(2026, 10, 10), paga.getDataVencimento());
		assertEquals("1203-26", paga.getSeuNumero());
		assertEquals(850.0, paga.getValor());
		assertEquals("07790001161217664479208601462271815950000085000", paga.getLinhaDigitavel());
		assertEquals("00020101021226980014BR.GOV.BCB.PIX-B", paga.getPixCopiaCola());
		assertEquals("5543179151790110274000MaJc8n9UwVlF3", paga.getTxid());
		// RECEBIDO: data do pagamento vem de dataSituacao, e a origem/valor recebido são guardados.
		assertEquals(LocalDate.of(2026, 10, 1), paga.getDataPagamento());
		assertEquals("PIX", paga.getOrigemRecebimento());
		assertEquals(850.0, paga.getValorTotalRecebido());

		CobrancaOrdinaria aberta = cobrancaRepository.findByCodigoSolicitacao("84600ef0-5960-4618-8afa-c8a098348524").orElseThrow();
		assertEquals("A_RECEBER", aberta.getSituacaoBanco());
		assertNull(aberta.getDataPagamento());
		assertNotNull(aberta.getDataUltimaSincronizacao());
	}

	@Test
	void reaproveitaORegistroQueOJobLocalJaCriouParaACompetencia() {
		Unidade unidade = unidadeRepository.findByCondominioIdAndNumero(condominio.getId(), "1404").get(0);
		CobrancaOrdinaria local = new CobrancaOrdinaria();
		local.setUnidade(unidade);
		local.setCompetencia(LocalDate.of(2026, 10, 1));
		local.setDataVencimento(LocalDate.of(2026, 10, 5));
		local.setPixCopiaCola("digitado-a-mao");
		Long idLocal = cobrancaRepository.save(local).getId();

		sincronizacao.importar(condominio.getId());

		assertEquals(2, cobrancaRepository.count(), "não deve duplicar a competência");
		CobrancaOrdinaria vinculada = cobrancaRepository.findById(idLocal).orElseThrow();
		assertEquals("84600ef0-5960-4618-8afa-c8a098348524", vinculada.getCodigoSolicitacao());
		assertEquals("00020101021226980014BR.GOV.BCB.PIX-A", vinculada.getPixCopiaCola(), "o banco vale mais que o digitado");
		assertEquals(LocalDate.of(2026, 10, 10), vinculada.getDataVencimento());
	}

	@Test
	void importacaoEIdempotente() {
		sincronizacao.importar(condominio.getId());
		sincronizacao.importar(condominio.getId());

		assertEquals(2, cobrancaRepository.count());
	}

	@Test
	void reimportarNaoMexeEmCobrancaJaPagaMasAtualizaAsEmAberto() {
		sincronizacao.importar(condominio.getId());
		LocalDateTime antiga = LocalDateTime.of(2026, 10, 1, 3, 30);
		CobrancaOrdinaria paga = cobrancaRepository.findByCodigoSolicitacao("c0a7aee1-f83f-4c7a-a753-c04542c3c279").orElseThrow();
		CobrancaOrdinaria aberta = cobrancaRepository.findByCodigoSolicitacao("84600ef0-5960-4618-8afa-c8a098348524").orElseThrow();
		paga.setDataUltimaSincronizacao(antiga);
		aberta.setDataUltimaSincronizacao(antiga);
		cobrancaRepository.saveAll(List.of(paga, aberta));

		sincronizacao.importar(condominio.getId());

		assertEquals(antiga, cobrancaRepository.findById(paga.getId()).orElseThrow().getDataUltimaSincronizacao(),
				"cobrança paga não deve ter o 'conferida em' movido pela importação");
		assertTrue(cobrancaRepository.findById(aberta.getId()).orElseThrow().getDataUltimaSincronizacao().isAfter(antiga),
				"cobrança em aberto continua sendo atualizada");
	}

	@Test
	void ignoraCobrancaDeUnidadeIsentaDaTaxa() {
		Unidade isenta = unidadeRepository.findByCondominioIdAndNumero(condominio.getId(), "1203").get(0);
		isenta.setPercentualDescontoTaxa(new java.math.BigDecimal("100"));
		unidadeRepository.save(isenta);

		sincronizacao.importar(condominio.getId());

		assertEquals(1, cobrancaRepository.count(), "só a da unidade 1404 deve ser importada");
		assertTrue(cobrancaRepository.findByCodigoSolicitacao("c0a7aee1-f83f-4c7a-a753-c04542c3c279").isEmpty());
	}

	@Test
	void descontoParcialNaoImpedeAImportacaoEOValorDoBancoPrevalece() {
		Unidade metade = unidadeRepository.findByCondominioIdAndNumero(condominio.getId(), "1203").get(0);
		metade.setPercentualDescontoTaxa(new java.math.BigDecimal("50"));
		unidadeRepository.save(metade);

		sincronizacao.importar(condominio.getId());

		assertEquals(2, cobrancaRepository.count());
	}

	@Test
	void ignoraCobrancaCujaUnidadeNaoExiste() {
		unidadeRepository.findByCondominioIdAndNumero(condominio.getId(), "1203")
				.forEach(u -> unidadeRepository.delete(u));

		sincronizacao.importar(condominio.getId());

		assertEquals(1, cobrancaRepository.count());
		assertTrue(cobrancaRepository.findByCodigoSolicitacao("c0a7aee1-f83f-4c7a-a753-c04542c3c279").isEmpty());
	}

	@Test
	void sincronizarReconsultaSoAsPendentesEMarcaPagamento() throws Exception {
		sincronizacao.importar(condominio.getId());
		when(banco.recuperar(condominio.getId(), "84600ef0-5960-4618-8afa-c8a098348524"))
				.thenReturn(ler("/inter/detalhe-recebido.json", InterCobranca.class));

		sincronizacao.sincronizarPendentes(condominio.getId());

		CobrancaOrdinaria agora = cobrancaRepository.findByCodigoSolicitacao("84600ef0-5960-4618-8afa-c8a098348524").orElseThrow();
		assertEquals("RECEBIDO", agora.getSituacaoBanco());
		assertEquals(LocalDate.of(2026, 10, 9), agora.getDataPagamento());
		assertEquals("BOLETO", agora.getOrigemRecebimento());
		// A cobrança já paga na importação não é reconsultada.
		verify(banco, never()).recuperar(condominio.getId(), "c0a7aee1-f83f-4c7a-a753-c04542c3c279");

		// E, paga, a outra também sai do filtro: uma segunda rodada não chama o banco de novo.
		sincronizacao.sincronizarPendentes(condominio.getId());
		verify(banco).recuperar(condominio.getId(), "84600ef0-5960-4618-8afa-c8a098348524");
	}

	@Test
	void condominioSemCredenciaisEIgnorado() {
		when(banco.integrado(condominio.getId())).thenReturn(false);

		sincronizacao.importar(condominio.getId());

		assertEquals(0, cobrancaRepository.count());
		verify(banco, never()).listar(any(), any(), any());
	}

	private void unidade(String numero) {
		Unidade unidade = new Unidade();
		unidade.setCondominio(condominio);
		unidade.setBloco("");
		unidade.setNumero(numero);
		unidadeRepository.save(unidade);
	}

	private static <T> T ler(String recurso, Class<T> tipo) throws Exception {
		try (InputStream entrada = SincronizacaoBancariaImplTest.class.getResourceAsStream(recurso)) {
			return JsonMapper.builder().build().readValue(entrada, tipo);
		}
	}

}
