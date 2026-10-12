package br.com.gestaocondial.autogestao.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;

/** Desconto por unidade na geração: 0% cobra cheio, parcial cobra proporcional, 100% não gera cobrança. */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class GeradorDeCobrancaOrdinariaJobIntegracaoTest {

	@Autowired
	private GeradorDeCobrancaOrdinariaJob job;

	@Autowired
	private CobrancaOrdinariaRepository cobrancaRepository;

	@Autowired
	private CondominioRepository condominioRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	private Condominio condominio;

	@BeforeEach
	void prepararCenario() {
		cobrancaRepository.deleteAll();
		unidadeRepository.deleteAll();
		condominioRepository.deleteAll();
		Condominio novo = new Condominio();
		novo.setEndereco(enderecoRepository.save(new Endereco()));
		novo.setPossuiTaxaExtra(false);
		novo.setValorTaxaCondominial(850.0);
		novo.setDiaVencimentoTaxaOrdinaria(10);
		condominio = condominioRepository.save(novo);
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
	void geraConformeODescontoDeCadaUnidade() {
		unidade("101", null);
		unidade("102", "50");
		unidade("1403", "100");

		job.gerarParaCondominio(condominio.getId());

		List<CobrancaOrdinaria> geradas = cobrancaRepository.findAll();
		assertEquals(2, geradas.size(), "a unidade isenta não deve receber cobrança");
		assertEquals(850.0, valorDa(geradas, "101"));
		assertEquals(425.0, valorDa(geradas, "102"));
		assertFalse(geradas.stream().anyMatch(c -> c.getUnidade().getNumero().equals("1403")));
	}

	@Test
	void gerarDeNovoNaoCriaCobrancaParaAIsentaNemDuplicaAsDemais() {
		unidade("101", "0");
		unidade("1403", "100");

		job.gerarParaCondominio(condominio.getId());
		job.gerarParaCondominio(condominio.getId());

		assertEquals(1, cobrancaRepository.count());
	}

	@Test
	void descontoDe99ComaNoventaENoveAindaCobra() {
		unidade("201", "99.99");

		job.gerarParaCondominio(condominio.getId());

		assertTrue(cobrancaRepository.findAll().get(0).getValor() > 0);
		assertEquals(1, cobrancaRepository.count());
	}

	private void unidade(String numero, String percentual) {
		Unidade unidade = new Unidade();
		unidade.setCondominio(condominio);
		unidade.setBloco("");
		unidade.setNumero(numero);
		if (percentual != null) {
			unidade.setPercentualDescontoTaxa(new BigDecimal(percentual));
		}
		unidadeRepository.save(unidade);
	}

	private static Double valorDa(List<CobrancaOrdinaria> cobrancas, String numero) {
		return cobrancas.stream().filter(c -> c.getUnidade().getNumero().equals(numero)).findFirst().orElseThrow()
				.getValor();
	}

}
