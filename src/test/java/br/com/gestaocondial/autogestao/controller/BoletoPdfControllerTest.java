package br.com.gestaocondial.autogestao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.gestaocondial.autogestao.banking.InterApiException;
import br.com.gestaocondial.autogestao.banking.InterCobrancaClient;
import br.com.gestaocondial.autogestao.domain.CobrancaBancoPendente;
import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.enumeration.MotivoDePendenciaEnum;
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
 * Boleto em PDF: quem pode abrir (administrador/síndico qualquer unidade; os demais só a própria),
 * só cobrança em aberto e vinda do banco, e como as falhas do Inter chegam ao cliente.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BoletoPdfControllerTest {

	private static final byte[] PDF = "%PDF-1.4 fake %%EOF".getBytes();

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

	@MockitoBean
	private InterCobrancaClient banco;

	private Condominio condominio;

	private Unidade unidade101;

	private Unidade unidade102;

	private CobrancaOrdinaria aberta101;

	private CobrancaOrdinaria aberta102;

	@BeforeEach
	void prepararCenario() {
		condominio = novoCondominio();
		unidade101 = unidade(condominio, "101");
		unidade102 = unidade(condominio, "102");
		morador("morador101@teste.com", unidade101);
		aberta101 = cobranca(unidade101, "cod-101", null, LocalDate.now().plusDays(5));
		aberta102 = cobranca(unidade102, "cod-102", null, LocalDate.now().minusDays(3));
		when(banco.baixarPdf(any(), any())).thenReturn(PDF);
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

	// ------------------------------------------------------------------ quem pode abrir

	@Test
	void sindicoAbreOBoletoDeQualquerUnidadeDoCondominio() throws Exception {
		mockMvc.perform(get(url(aberta102)).with(sindico(condominio.getId())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/pdf"))
				.andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("boleto-102-")))
				.andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
				.andExpect(content().bytes(PDF));
		verify(banco).baixarPdf(condominio.getId(), "cod-102");
	}

	@Test
	void administradorGlobalAbreOBoletoDeQualquerCondominio() throws Exception {
		mockMvc.perform(get(url(aberta101)).with(administrador())).andExpect(status().isOk());
	}

	@Test
	void moradorAbreSoOBoletoDaPropriaUnidade() throws Exception {
		mockMvc.perform(get(url(aberta101)).with(morador("morador101@teste.com", condominio.getId())))
				.andExpect(status().isOk())
				.andExpect(content().bytes(PDF));
	}

	@Test
	void moradorNaoAbreOBoletoDeOutraUnidade() throws Exception {
		mockMvc.perform(get(url(aberta102)).with(morador("morador101@teste.com", condominio.getId())))
				.andExpect(status().isForbidden());
		verify(banco, never()).baixarPdf(any(), any());
	}

	@Test
	void usuarioSemCadastroDeMoradorNaoAbreNenhumBoleto() throws Exception {
		mockMvc.perform(get(url(aberta101)).with(morador("desconhecido@teste.com", condominio.getId())))
				.andExpect(status().isForbidden());
	}

	@Test
	void sindicoDeOutroCondominioNaoAbre() throws Exception {
		mockMvc.perform(get(url(aberta101)).with(sindico(condominio.getId() + 999)))
				.andExpect(status().isForbidden());
		verify(banco, never()).baixarPdf(any(), any());
	}

	// ------------------------------------------------------------------ só cobrança em aberto do banco

	@Test
	void cobrancaPagaNaoTemBoleto() throws Exception {
		aberta101.setDataPagamento(LocalDate.now());
		cobrancaRepository.save(aberta101);

		mockMvc.perform(get(url(aberta101)).with(sindico(condominio.getId()))).andExpect(status().isConflict());
		verify(banco, never()).baixarPdf(any(), any());
	}

	@Test
	void cobrancaCanceladaNoBancoNaoTemBoleto() throws Exception {
		aberta101.setSituacaoBanco("CANCELADO");
		cobrancaRepository.save(aberta101);

		mockMvc.perform(get(url(aberta101)).with(sindico(condominio.getId()))).andExpect(status().isConflict());
	}

	@Test
	void cobrancaSemVinculoComOBancoNaoTemBoleto() throws Exception {
		aberta101.setCodigoSolicitacao(null);
		cobrancaRepository.save(aberta101);

		mockMvc.perform(get(url(aberta101)).with(sindico(condominio.getId()))).andExpect(status().isConflict());
	}

	@Test
	void cobrancaInexistenteDa404() throws Exception {
		mockMvc.perform(get("/cobrancaOrdinaria/999999/pdf").with(administrador())).andExpect(status().isNotFound());
	}

	// ------------------------------------------------------------------ falhas do Inter

	@Test
	void falhaDoBancoViraBadGatewayComMensagemFixa() throws Exception {
		when(banco.baixarPdf(any(), any()))
				.thenThrow(new InterApiException("detalhe interno com certificado", null, 500));

		mockMvc.perform(get(url(aberta101)).with(sindico(condominio.getId())))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.erros[0].mensagem").value(
						"Não foi possível consultar o banco agora. Tente novamente em instantes."));
	}

	@Test
	void cobrancaInexistenteNoBancoDa404() throws Exception {
		when(banco.baixarPdf(any(), any())).thenThrow(new InterApiException("nao achou", null, 404));

		mockMvc.perform(get(url(aberta101)).with(sindico(condominio.getId()))).andExpect(status().isNotFound());
	}

	// ------------------------------------------------------------------ podeVerBoleto na lista

	@Test
	void listaMarcaPodeVerBoletoConformeOPerfil() throws Exception {
		CobrancaOrdinaria paga = cobranca(unidade101, "cod-paga", LocalDate.now(), LocalDate.now().minusDays(40),
				LocalDate.now().minusMonths(1).withDayOfMonth(1));

		// Síndico: toda cobrança em aberto do banco; a paga não.
		mockMvc.perform(get("/cobrancaOrdinaria").with(sindico(condominio.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == " + aberta101.getId() + ")].podeVerBoleto").value(true))
				.andExpect(jsonPath("$[?(@.id == " + aberta102.getId() + ")].podeVerBoleto").value(true))
				.andExpect(jsonPath("$[?(@.id == " + paga.getId() + ")].podeVerBoleto").value(false));

		// Morador da 101: a lista traz só as cobranças da própria unidade (a paga e a em aberto).
		mockMvc.perform(get("/cobrancaOrdinaria").with(morador("morador101@teste.com", condominio.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[?(@.id == " + aberta101.getId() + ")].podeVerBoleto").value(true))
				.andExpect(jsonPath("$[?(@.id == " + aberta102.getId() + ")]").isEmpty());
	}

	// ------------------------------------------------------------------ pendências do banco

	@Test
	void sindicoVeAsPendenciasDoBancoEMoradorNao() throws Exception {
		CobrancaBancoPendente pendente = new CobrancaBancoPendente();
		pendente.setIdCondominio(condominio.getId());
		pendente.setCodigoSolicitacao("cod-sem-unidade");
		pendente.setSeuNumero("1405-26");
		pendente.setDataVencimento(LocalDate.of(2026, 10, 10));
		pendente.setValor(850.0);
		pendente.setMotivo(MotivoDePendenciaEnum.SEM_UNIDADE);
		pendenciaRepository.save(pendente);

		mockMvc.perform(get("/cobrancaOrdinaria/pendencias-do-banco").with(sindico(condominio.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].seuNumero").value("1405-26"))
				.andExpect(jsonPath("$[0].motivo").value("SEM_UNIDADE"));

		mockMvc.perform(get("/cobrancaOrdinaria/pendencias-do-banco")
				.with(morador("morador101@teste.com", condominio.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	// ------------------------------------------------------------------ apoio

	private static String url(CobrancaOrdinaria cobranca) {
		return "/cobrancaOrdinaria/" + cobranca.getId() + "/pdf";
	}

	private static RequestPostProcessor sindico(Long idCondominio) {
		return perfil("sindico@teste.com", "SINDICO", idCondominio, List.of("SINDICO"));
	}

	private static RequestPostProcessor administrador() {
		return perfil("admin@teste.com", "ADMINISTRADORA", null, List.of("ADMINISTRADOR"));
	}

	private static RequestPostProcessor morador(String email, Long idCondominio) {
		return perfil(email, "MORADOR", idCondominio, List.of("MORADOR"));
	}

	/**
	 * No profile de teste o autoconfigure de segurança está desligado (application-test.properties),
	 * então não há filtro que leia o {@code jwt()} do MockMvc: a autenticação é instalada direto no
	 * {@code SecurityContextHolder}, que é o que {@code EscopoDoCondominio} e {@code AlcanceDoPerfil} leem.
	 */
	private static RequestPostProcessor perfil(String email, String tipo, Long idCondominio, List<String> papeis) {
		Jwt.Builder token = Jwt.withTokenValue("teste").header("alg", "none").subject(email)
				.claim("tipoPerfil", tipo).claim("papeis", papeis);
		if (idCondominio != null) {
			token.claim("condominio", idCondominio);
		}
		JwtAuthenticationToken autenticacao = new JwtAuthenticationToken(token.build(),
				List.of(new SimpleGrantedAuthority("COBRANCA_LER")));
		return request -> {
			SecurityContextHolder.getContext().setAuthentication(autenticacao);
			return request;
		};
	}

	private Condominio novoCondominio() {
		Condominio novo = new Condominio();
		novo.setEndereco(enderecoRepository.save(new Endereco()));
		novo.setPossuiTaxaExtra(false);
		return condominioRepository.save(novo);
	}

	private Unidade unidade(Condominio dono, String numero) {
		Unidade unidade = new Unidade();
		unidade.setCondominio(dono);
		unidade.setBloco("");
		unidade.setNumero(numero);
		return unidadeRepository.save(unidade);
	}

	private void morador(String email, Unidade unidade) {
		Pessoa pessoa = new Pessoa();
		pessoa.setNomeCompleto("Morador " + unidade.getNumero());
		pessoa.setEmail(email);
		pessoa.setCondominio(unidade.getCondominio());
		pessoa.setUnidade(unidade);
		pessoaRepository.save(pessoa);
	}

	private CobrancaOrdinaria cobranca(Unidade unidade, String codigo, LocalDate pagamento, LocalDate vencimento) {
		return cobranca(unidade, codigo, pagamento, vencimento, LocalDate.now().withDayOfMonth(1));
	}

	private CobrancaOrdinaria cobranca(Unidade unidade, String codigo, LocalDate pagamento, LocalDate vencimento,
			LocalDate competencia) {
		CobrancaOrdinaria cobranca = new CobrancaOrdinaria();
		cobranca.setUnidade(unidade);
		cobranca.setCompetencia(competencia);
		cobranca.setDataVencimento(vencimento);
		cobranca.setCodigoSolicitacao(codigo);
		cobranca.setDataPagamento(pagamento);
		return cobrancaRepository.save(cobranca);
	}

}
