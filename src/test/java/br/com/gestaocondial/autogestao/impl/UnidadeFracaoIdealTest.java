package br.com.gestaocondial.autogestao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.dto.CondominioDto;
import br.com.gestaocondial.autogestao.dto.UnidadeDto;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.service.UnidadeService;

/** A fração ideal deixou de ser digitada: é 1 / quantidade de unidades do condomínio, calculada na leitura. */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UnidadeFracaoIdealTest {

	@Autowired
	private UnidadeService unidadeService;

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
		condominio = new Condominio();
		condominio.setEndereco(enderecoRepository.save(new Endereco()));
		condominio.setPossuiTaxaExtra(false);
	}

	@AfterEach
	void limparDados() {
		cobrancaRepository.deleteAll();
		unidadeRepository.deleteAll();
		condominioRepository.deleteAll();
		enderecoRepository.deleteAll();
	}

	@Test
	void fracaoEUmSobreAQuantidadeDeUnidades() {
		condominio.setQuantidadeUnidades(24);
		condominio = condominioRepository.save(condominio);

		UnidadeDto criada = unidadeService.createUnidade(unidade("101", null));

		assertEquals(new BigDecimal("0.041667"), criada.getFracaoIdeal());
		assertEquals(new BigDecimal("0.041667"), listar().get(0).getFracaoIdeal());
	}

	@Test
	void fracaoAcompanhaAQuantidadeQuandoOCondominioMuda() {
		condominio.setQuantidadeUnidades(24);
		condominio = condominioRepository.save(condominio);
		unidadeService.createUnidade(unidade("101", null));

		condominio.setQuantidadeUnidades(10);
		condominioRepository.save(condominio);

		assertEquals(new BigDecimal("0.100000"), listar().get(0).getFracaoIdeal());
	}

	@Test
	void semAQuantidadeNaoHaFracao() {
		condominio = condominioRepository.save(condominio);

		UnidadeDto criada = unidadeService.createUnidade(unidade("101", null));

		assertNull(criada.getFracaoIdeal());
	}

	@Test
	void valorDeFracaoEnviadoPeloClienteEIgnorado() {
		condominio.setQuantidadeUnidades(4);
		condominio = condominioRepository.save(condominio);

		UnidadeDto criada = unidadeService.createUnidade(unidade("101", new BigDecimal("0.9")));

		assertEquals(new BigDecimal("0.250000"), criada.getFracaoIdeal());
	}

	private List<UnidadeDto> listar() {
		return unidadeService.listUnidades(condominio.getId(), null, null);
	}

	private UnidadeDto unidade(String numero, BigDecimal fracaoDoCliente) {
		UnidadeDto dto = new UnidadeDto();
		CondominioDto condo = new CondominioDto();
		condo.setId(condominio.getId());
		dto.setCondominio(condo);
		dto.setNumero(numero);
		dto.setFracaoIdeal(fracaoDoCliente);
		return dto;
	}

}
