package br.com.gestaocondial.autogestao.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.domain.Pessoa;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.repository.CobrancaBancoPendenteRepository;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.repository.PessoaRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;

/**
 * Perfil do tipo MORADOR só enxerga o que é seu — a própria unidade, o próprio cadastro e as
 * cobranças da própria unidade —, em listas e em detalhes. Os demais perfis seguem vendo o
 * condomínio inteiro.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MoradorSoVeOProprioTest {

	private static final String EMAIL_101 = "morador101@teste.com";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CobrancaOrdinariaRepository cobrancaRepository;

	@Autowired
	private CobrancaBancoPendenteRepository pendenciaRepository;

	@Autowired
	private CondominioRepository condominioRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	private Condominio condominio;

	private Unidade unidade101;

	private Unidade unidade102;

	private Pessoa pessoa101;

	private Pessoa pessoa102;

	private CobrancaOrdinaria cobranca101;

	private CobrancaOrdinaria cobranca102;

	@BeforeEach
	void prepararCenario() {
		Condominio novo = new Condominio();
		novo.setEndereco(enderecoRepository.save(new Endereco()));
		novo.setPossuiTaxaExtra(false);
		condominio = condominioRepository.save(novo);

		unidade101 = unidade("101");
		unidade102 = unidade("102");
		pessoa101 = pessoa(EMAIL_101, unidade101);
		pessoa102 = pessoa("morador102@teste.com", unidade102);
		cobranca101 = cobranca(unidade101);
		cobranca102 = cobranca(unidade102);
	}

	@AfterEach
	void limparDados() {
		SecurityContextHolder.clearContext();
		pendenciaRepository.deleteAll();
		cobrancaRepository.deleteAll();
		pessoaRepository.deleteAll();
		unidadeRepository.deleteAll();
		condominioRepository.deleteAll();
		enderecoRepository.deleteAll();
	}

	// ------------------------------------------------------------------ unidades

	@Test
	void moradorListaSoAPropriaUnidade() throws Exception {
		mockMvc.perform(get("/unidade").with(morador(EMAIL_101)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(unidade101.getId()));
	}

	@Test
	void moradorAbreAPropriaUnidadeMasNaoAOutra() throws Exception {
		mockMvc.perform(get("/unidade/" + unidade101.getId()).with(morador(EMAIL_101))).andExpect(status().isOk());
		mockMvc.perform(get("/unidade/" + unidade102.getId()).with(morador(EMAIL_101)))
				.andExpect(status().isForbidden());
	}

	@Test
	void sindicoListaTodasAsUnidades() throws Exception {
		mockMvc.perform(get("/unidade").with(sindico()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	// ------------------------------------------------------------------ moradores

	@Test
	void moradorListaSoOProprioCadastro() throws Exception {
		mockMvc.perform(get("/pessoa").with(morador(EMAIL_101)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(pessoa101.getId()));
	}

	@Test
	void moradorNaoVeOCadastroDeOutroMoradorNemPeloCondominio() throws Exception {
		mockMvc.perform(get("/pessoa/" + pessoa101.getId()).with(morador(EMAIL_101))).andExpect(status().isOk());
		mockMvc.perform(get("/pessoa/" + pessoa102.getId()).with(morador(EMAIL_101)))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/pessoa/listPessoasCondominio/" + condominio.getId()).with(morador(EMAIL_101)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void sindicoListaTodosOsMoradores() throws Exception {
		mockMvc.perform(get("/pessoa").with(sindico()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	// ------------------------------------------------------------------ cobranças

	@Test
	void moradorListaSoAsCobrancasDaPropriaUnidade() throws Exception {
		mockMvc.perform(get("/cobrancaOrdinaria").with(morador(EMAIL_101)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(cobranca101.getId()));
	}

	@Test
	void moradorNaoFiltraParaVerCobrancaDeOutraUnidade() throws Exception {
		mockMvc.perform(get("/cobrancaOrdinaria").param("idUnidade", unidade102.getId().toString())
				.with(morador(EMAIL_101)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void moradorAbreACobrancaDaPropriaUnidadeMasNaoDeOutra() throws Exception {
		mockMvc.perform(get("/cobrancaOrdinaria/" + cobranca101.getId()).with(morador(EMAIL_101)))
				.andExpect(status().isOk());
		mockMvc.perform(get("/cobrancaOrdinaria/" + cobranca102.getId()).with(morador(EMAIL_101)))
				.andExpect(status().isForbidden());
	}

	@Test
	void usuarioSemCadastroDeMoradorNaoVeNada() throws Exception {
		mockMvc.perform(get("/unidade").with(morador("desconhecido@teste.com")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/pessoa").with(morador("desconhecido@teste.com")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/cobrancaOrdinaria").with(morador("desconhecido@teste.com")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void sindicoListaTodasAsCobrancas() throws Exception {
		mockMvc.perform(get("/cobrancaOrdinaria").with(sindico()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void perfilNaoMoradorSemPapelAmploContinuaVendoOCondominioTodo() throws Exception {
		// Financeiro/subsíndico: tipo diferente de MORADOR, sem papel de síndico.
		mockMvc.perform(get("/cobrancaOrdinaria")
				.with(perfil("financeiro@teste.com", "FUNCIONARIO", List.of("FINANCEIRO"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	// ------------------------------------------------------------------ apoio

	private RequestPostProcessor morador(String email) {
		return perfil(email, "MORADOR", List.of("MORADOR"));
	}

	private RequestPostProcessor sindico() {
		return perfil("sindico@teste.com", "SINDICO", List.of("SINDICO"));
	}

	/** O profile de teste não liga a segurança: a autenticação vai direto no SecurityContextHolder. */
	private RequestPostProcessor perfil(String email, String tipo, List<String> papeis) {
		Jwt token = Jwt.withTokenValue("teste").header("alg", "none").subject(email)
				.claim("tipoPerfil", tipo).claim("papeis", papeis)
				.claim("condominio", condominio.getId()).build();
		JwtAuthenticationToken autenticacao = new JwtAuthenticationToken(token,
				List.of(new SimpleGrantedAuthority("UNIDADE_LER"), new SimpleGrantedAuthority("MORADOR_LER"),
						new SimpleGrantedAuthority("COBRANCA_LER")));
		return request -> {
			SecurityContextHolder.getContext().setAuthentication(autenticacao);
			return request;
		};
	}

	private Unidade unidade(String numero) {
		Unidade unidade = new Unidade();
		unidade.setCondominio(condominio);
		unidade.setBloco("");
		unidade.setNumero(numero);
		return unidadeRepository.save(unidade);
	}

	private Pessoa pessoa(String email, Unidade unidade) {
		Pessoa pessoa = new Pessoa();
		pessoa.setNomeCompleto("Morador " + unidade.getNumero());
		pessoa.setEmail(email);
		pessoa.setCondominio(condominio);
		pessoa.setUnidade(unidade);
		return pessoaRepository.save(pessoa);
	}

	private CobrancaOrdinaria cobranca(Unidade unidade) {
		CobrancaOrdinaria cobranca = new CobrancaOrdinaria();
		cobranca.setUnidade(unidade);
		cobranca.setCompetencia(LocalDate.now().withDayOfMonth(1));
		cobranca.setDataVencimento(LocalDate.now().plusDays(5));
		return cobrancaRepository.save(cobranca);
	}

}
