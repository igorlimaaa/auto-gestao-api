package br.com.gestaocondial.autogestao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.dto.CobrancaOrdinariaDto;
import br.com.gestaocondial.autogestao.enumeration.SituacaoCobrancaEnum;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.service.CobrancaOrdinariaService;

/**
 * A listagem tem que funcionar para cobranças que nunca falaram com o banco ({@code
 * situacaoBanco} nulo) — é o caso de todas as geradas só pelo job local.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CobrancaOrdinariaImplTest {

	@Autowired
	private CobrancaOrdinariaService service;

	@Autowired
	private CobrancaOrdinariaRepository cobrancaRepository;

	@Autowired
	private CondominioRepository condominioRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	private Unidade unidade;

	@BeforeEach
	void prepararCenario() {
		cobrancaRepository.deleteAll();
		unidadeRepository.deleteAll();
		condominioRepository.deleteAll();
		Condominio condominio = new Condominio();
		condominio.setEndereco(enderecoRepository.save(new Endereco()));
		condominio.setPossuiTaxaExtra(false);
		condominio = condominioRepository.save(condominio);
		Unidade nova = new Unidade();
		nova.setCondominio(condominio);
		nova.setBloco("");
		nova.setNumero("101");
		unidade = unidadeRepository.save(nova);
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
	void listaCobrancaLocalQueNuncaFoiSincronizadaComOBanco() {
		cobranca(LocalDate.now().plusDays(5), null, null);
		cobranca(LocalDate.now().minusDays(5), null, null);

		List<CobrancaOrdinariaDto> lista = service.listCobrancas(null, null, null);

		assertEquals(2, lista.size());
		assertEquals(1, lista.stream().filter(c -> c.getSituacao() == SituacaoCobrancaEnum.PENDENTE).count());
		assertEquals(1, lista.stream().filter(c -> c.getSituacao() == SituacaoCobrancaEnum.INADIMPLENTE).count());
	}

	@Test
	void cobrancaCanceladaNoBancoNaoContaComoInadimplente() {
		cobranca(LocalDate.now().minusDays(5), null, "CANCELADO");

		assertEquals(SituacaoCobrancaEnum.CANCELADA, service.listCobrancas(null, null, null).get(0).getSituacao());
	}

	private void cobranca(LocalDate vencimento, LocalDate pagamento, String situacaoBanco) {
		CobrancaOrdinaria c = new CobrancaOrdinaria();
		c.setUnidade(unidade);
		c.setCompetencia(vencimento.withDayOfMonth(1).plusMonths(cobrancaRepository.count()));
		c.setDataVencimento(vencimento);
		c.setDataPagamento(pagamento);
		c.setSituacaoBanco(situacaoBanco);
		cobrancaRepository.save(c);
	}

}
