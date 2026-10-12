package br.com.gestaocondial.autogestao.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.gestaocondial.autogestao.domain.AreaComum;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.domain.Pessoa;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.enumeration.ModoDeReservaEnum;
import br.com.gestaocondial.autogestao.repository.AreaComumRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.repository.EventoRepository;
import br.com.gestaocondial.autogestao.repository.PessoaRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;

/**
 * Agenda: quem cadastra, o que o morador gera (pedido pendente), quem aprova, o que cada perfil
 * enxerga, e a regra de que uma área não pode estar em dois lugares ao mesmo tempo.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AgendaControllerTest {

	private static final String MORADOR_101 = "morador101@teste.com";

	private static final String MORADOR_102 = "morador102@teste.com";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EventoRepository eventoRepository;

	@Autowired
	private AreaComumRepository areaRepository;

	@Autowired
	private CondominioRepository condominioRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	private Condominio condominio;

	private AreaComum salao;

	private AreaComum elevador;

	private AreaComum festas;

	@BeforeEach
	void prepararCenario() {
		Condominio novo = new Condominio();
		novo.setEndereco(enderecoRepository.save(new Endereco()));
		novo.setPossuiTaxaExtra(false);
		condominio = condominioRepository.save(novo);

		pessoa(MORADOR_101, unidade("101"));
		pessoa(MORADOR_102, unidade("102"));
		salao = area("Salão de festas", true, LocalTime.of(8, 0), LocalTime.of(22, 0));
		elevador = area("Elevador de serviço", false, null, null);
		festas = area("Salão de eventos", true, null, null);
		festas.setModoDeReserva(ModoDeReservaEnum.DIA_INTEIRO);
		areaRepository.save(festas);
	}

	@AfterEach
	void limparDados() {
		SecurityContextHolder.clearContext();
		eventoRepository.deleteAll();
		areaRepository.deleteAll();
		pessoaRepository.deleteAll();
		unidadeRepository.deleteAll();
		condominioRepository.deleteAll();
		enderecoRepository.deleteAll();
	}

	// ------------------------------------------------------------------ quem cadastra

	@Test
	void eventoDeMoradorNasceComoPedidoPendente() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.situacao").value("PENDENTE"))
				.andExpect(jsonPath("$.rotuloDaUnidade").value("101"))
				.andExpect(jsonPath("$.ehMeu").value(true))
				.andExpect(jsonPath("$.podeCancelar").value(true))
				.andExpect(jsonPath("$.podeDecidir").value(false));
	}

	@Test
	void eventoDoSindicoNasceAprovado() throws Exception {
		criar(sindico(), "REUNIAO", null, amanha(19), amanha(21), "Reunião do conselho")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.situacao").value("APROVADO"));
	}

	// ------------------------------------------------------------------ quem vê o quê

	@Test
	void pedidoPendenteSoApareceParaOAutorEParaQuemAprova() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário").andExpect(status().isCreated());

		listar(morador(MORADOR_101)).andExpect(jsonPath("$.length()").value(1));
		listar(morador(MORADOR_102)).andExpect(jsonPath("$.length()").value(0));
		listar(sindico()).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].solicitanteEmail").value(MORADOR_101));
	}

	@Test
	void eventoAprovadoApareceParaTodosMasSemIdentificarQuemPediu() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário").andExpect(status().isCreated());
		Long id = eventoRepository.findAll().get(0).getId();
		decidir(sindico(), id, true, null).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("APROVADO"));

		listar(morador(MORADOR_102)).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].solicitanteEmail").doesNotExist())
				.andExpect(jsonPath("$[0].solicitanteNome").doesNotExist())
				.andExpect(jsonPath("$[0].ehMeu").value(false))
				.andExpect(jsonPath("$[0].podeCancelar").value(false));
	}

	@Test
	void moradorNaoAbrePorIdOPedidoDeOutro() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário").andExpect(status().isCreated());
		Long id = eventoRepository.findAll().get(0).getId();

		mockMvc.perform(get("/evento/" + id).with(morador(MORADOR_102))).andExpect(status().isNotFound());
		mockMvc.perform(get("/evento/" + id).with(morador(MORADOR_101))).andExpect(status().isOk());
	}

	// ------------------------------------------------------------------ aprovação

	@Test
	void moradorNaoDecide() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário").andExpect(status().isCreated());
		Long id = eventoRepository.findAll().get(0).getId();

		decidir(morador(MORADOR_101), id, true, null).andExpect(status().isForbidden());
	}

	@Test
	void recusarExigeMotivoEOMoradorEnxergaOMotivo() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário").andExpect(status().isCreated());
		Long id = eventoRepository.findAll().get(0).getId();

		decidir(sindico(), id, false, " ").andExpect(status().isBadRequest());
		decidir(sindico(), id, false, "Salão em manutenção").andExpect(status().isOk())
				.andExpect(jsonPath("$.situacao").value("RECUSADO"));

		mockMvc.perform(get("/evento/" + id).with(morador(MORADOR_101))).andExpect(status().isOk())
				.andExpect(jsonPath("$.motivoDaDecisao").value("Salão em manutenção"))
				.andExpect(jsonPath("$.podeCancelar").value(false));
	}

	@Test
	void naoDecideDuasVezes() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário").andExpect(status().isCreated());
		Long id = eventoRepository.findAll().get(0).getId();

		decidir(sindico(), id, true, null).andExpect(status().isOk());
		decidir(sindico(), id, false, "Mudei de ideia").andExpect(status().isConflict());
	}

	// ------------------------------------------------------------------ áreas

	@Test
	void duasReservasSobrepostasNaMesmaAreaNaoConvivemAprovadas() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Festa A").andExpect(status().isCreated());
		criarReserva(morador(MORADOR_102), amanha(16), amanha(20), "Festa B").andExpect(status().isCreated());
		List<Long> ids = eventoRepository.findAll().stream().map(e -> e.getId()).toList();

		decidir(sindico(), ids.get(0), true, null).andExpect(status().isOk());
		decidir(sindico(), ids.get(1), true, null).andExpect(status().isConflict());
	}

	@Test
	void reservaQueNaoSobrepoeEhPermitidaEHorarioEncostadoNaoConflita() throws Exception {
		criarReserva(sindico(), amanha(10), amanha(14), "Festa A").andExpect(status().isCreated());
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Festa B").andExpect(status().isCreated());
	}

	@Test
	void reservaSobreHorarioAprovadoEhBarradaJaNoCadastro() throws Exception {
		criarReserva(sindico(), amanha(10), amanha(14), "Festa A").andExpect(status().isCreated());
		criarReserva(morador(MORADOR_101), amanha(12), amanha(16), "Festa B").andExpect(status().isConflict())
				.andExpect(jsonPath("$.erros[0].mensagem").value(containsString("já está ocupada")));
	}

	@Test
	void manutencaoFechaAAreaParaReservas() throws Exception {
		criar(sindico(), "MANUTENCAO", salao, amanha(9), amanha(12), "Pintura do salão").andExpect(status().isCreated());
		criarReserva(morador(MORADOR_101), amanha(10), amanha(11), "Festa").andExpect(status().isConflict());
	}

	@Test
	void reservaForaDoHorarioDaAreaEhRecusada() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(6), amanha(9), "Muito cedo").andExpect(status().isBadRequest());
		criarReserva(morador(MORADOR_101), amanha(20), amanha(23), "Muito tarde").andExpect(status().isBadRequest());
	}

	@Test
	void naoSeReservaAreaQueNaoAceitaReservaNemNoPassado() throws Exception {
		criar(morador(MORADOR_101), "RESERVA_AREA", elevador, amanha(10), amanha(12), "Mudança")
				.andExpect(status().isBadRequest());
		criarReserva(morador(MORADOR_101), ontem(14), ontem(16), "Passado").andExpect(status().isBadRequest());
	}

	@Test
	void reservaExigeAreaEMoradorSemUnidadeNaoReserva() throws Exception {
		criar(morador(MORADOR_101), "RESERVA_AREA", null, amanha(10), amanha(12), "Sem área")
				.andExpect(status().isBadRequest());
		criarReserva(morador("sem-unidade@teste.com"), amanha(10), amanha(12), "Sem unidade")
				.andExpect(status().isBadRequest());
	}

	@Test
	void areaDesativadaNaoPodeSerEscolhida() throws Exception {
		salao.setAtiva(false);
		areaRepository.save(salao);
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Festa").andExpect(status().isBadRequest());
	}

	// ------------------------------------------------------------------ área reservada pelo dia inteiro

	@Test
	void salaoDeDiaInteiroEReservadoSoPelaData() throws Exception {
		criarDiaInteiro(morador(MORADOR_101), festas, 3, "Casamento")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.situacao").value("PENDENTE"))
				.andExpect(jsonPath("$.diaInteiro").value(true));
	}

	@Test
	void doisMoradoresNaoReservamOSalaoNoMesmoDia() throws Exception {
		criarDiaInteiro(sindico(), festas, 3, "Festa A").andExpect(status().isCreated());
		criarDiaInteiro(morador(MORADOR_101), festas, 3, "Festa B").andExpect(status().isConflict());
		// O dia seguinte está livre: o que fecha é o dia, não a semana.
		criarDiaInteiro(morador(MORADOR_101), festas, 4, "Festa B").andExpect(status().isCreated());
	}

	@Test
	void pedidoPendenteNaoTravaODiaMasAprovarUmDeixaOOutroSemPoderSerAprovado() throws Exception {
		criarDiaInteiro(morador(MORADOR_101), festas, 3, "Festa A").andExpect(status().isCreated());
		criarDiaInteiro(morador(MORADOR_102), festas, 3, "Festa B").andExpect(status().isCreated());
		List<Long> ids = eventoRepository.findAll().stream().map(e -> e.getId()).toList();

		decidir(sindico(), ids.get(0), true, null).andExpect(status().isOk());
		decidir(sindico(), ids.get(1), true, null).andExpect(status().isConflict());
	}

	@Test
	void manutencaoNoSalaoNaquelaDataBarraAReservaDoDiaInteiro() throws Exception {
		criar(sindico(), "MANUTENCAO", festas, amanha(9), amanha(12), "Pintura").andExpect(status().isCreated());
		criarDiaInteiro(morador(MORADOR_101), festas, 1, "Festa").andExpect(status().isConflict());
	}

	@Test
	void reservaDeDiaInteiroNaoAceitaHorarioNemVariosDiasNemPassado() throws Exception {
		criarReservaDeHorario(festas, amanha(14), amanha(18)).andExpect(status().isBadRequest());
		criar(morador(MORADOR_101), "RESERVA_AREA", festas, amanha(0), amanha(0).plusDays(2), "Dois dias")
				.andExpect(status().isBadRequest());
		criar(morador(MORADOR_101), "RESERVA_AREA", festas, LocalDate.now().minusDays(2).atStartOfDay(),
				LocalDate.now().minusDays(1).atStartOfDay(), "Passado").andExpect(status().isBadRequest());
	}

	@Test
	void areaDeDiaInteiroNaoGuardaHorarioDeFuncionamento() throws Exception {
		mockMvc.perform(post("/areaComum").with(sindico()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Espaço gourmet\",\"reservavel\":true,\"modoDeReserva\":\"DIA_INTEIRO\","
						+ "\"horaAbertura\":\"08:00\",\"horaFechamento\":\"22:00\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.modoDeReserva").value("DIA_INTEIRO"))
				.andExpect(jsonPath("$.horaAbertura").doesNotExist());
	}

	// ------------------------------------------------------------------ cancelar

	@Test
	void autorCancelaOProprioPedidoMasOutroMoradorNao() throws Exception {
		criarReserva(morador(MORADOR_101), amanha(14), amanha(18), "Aniversário").andExpect(status().isCreated());
		Long id = eventoRepository.findAll().get(0).getId();

		mockMvc.perform(post("/evento/" + id + "/cancelamento").with(morador(MORADOR_102)))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/evento/" + id + "/cancelamento").with(morador(MORADOR_101)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("CANCELADO"));
		mockMvc.perform(post("/evento/" + id + "/cancelamento").with(morador(MORADOR_101)))
				.andExpect(status().isConflict());
	}

	@Test
	void cancelarLiberaOHorarioDaArea() throws Exception {
		criarReserva(sindico(), amanha(10), amanha(14), "Festa A").andExpect(status().isCreated());
		Long id = eventoRepository.findAll().get(0).getId();
		mockMvc.perform(post("/evento/" + id + "/cancelamento").with(sindico())).andExpect(status().isOk());

		criarReserva(morador(MORADOR_101), amanha(10), amanha(14), "Festa B").andExpect(status().isCreated());
	}

	// ------------------------------------------------------------------ catálogo de áreas

	@Test
	void naoHaDuasAreasComOMesmoNome() throws Exception {
		mockMvc.perform(post("/areaComum").with(sindico()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"salão de festas\",\"reservavel\":true}")).andExpect(status().isConflict());
	}

	@Test
	void areaComEventosNaoSeExcluiMasSemEventosSim() throws Exception {
		criarReserva(sindico(), amanha(10), amanha(14), "Festa").andExpect(status().isCreated());

		mockMvc.perform(delete("/areaComum/" + salao.getId()).with(sindico())).andExpect(status().isConflict());
		mockMvc.perform(delete("/areaComum/" + elevador.getId()).with(sindico())).andExpect(status().isNoContent());
	}

	@Test
	void horarioDaAreaPrecisaVirComAberturaEFechamento() throws Exception {
		mockMvc.perform(post("/areaComum").with(sindico()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Piscina\",\"reservavel\":false,\"horaAbertura\":\"08:00\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/areaComum").with(sindico()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Piscina\",\"reservavel\":false,\"horaAbertura\":\"08:00\",\"horaFechamento\":\"20:00\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.horaFechamento").value("20:00"));
	}

	// ------------------------------------------------------------------ apoio

	private ResultActions criarReserva(RequestPostProcessor quem, LocalDateTime inicio, LocalDateTime fim, String titulo)
			throws Exception {
		return criar(quem, "RESERVA_AREA", salao, inicio, fim, titulo);
	}

	private ResultActions criarDiaInteiro(RequestPostProcessor quem, AreaComum area, int diasAdiante, String titulo)
			throws Exception {
		LocalDateTime dia = LocalDate.now().plusDays(diasAdiante).atStartOfDay();
		return criar(quem, "RESERVA_AREA", area, dia, dia.plusDays(1), titulo, true);
	}

	private ResultActions criarReservaDeHorario(AreaComum area, LocalDateTime inicio, LocalDateTime fim)
			throws Exception {
		return criar(morador(MORADOR_101), "RESERVA_AREA", area, inicio, fim, "Por horário");
	}

	private ResultActions criar(RequestPostProcessor quem, String tipo, AreaComum area, LocalDateTime inicio,
			LocalDateTime fim, String titulo) throws Exception {
		return criar(quem, tipo, area, inicio, fim, titulo, false);
	}

	private ResultActions criar(RequestPostProcessor quem, String tipo, AreaComum area, LocalDateTime inicio,
			LocalDateTime fim, String titulo, boolean diaInteiro) throws Exception {
		String corpo = "{\"tipo\":\"%s\",\"titulo\":\"%s\",\"inicio\":\"%s\",\"fim\":\"%s\",\"diaInteiro\":%s%s}"
				.formatted(tipo, titulo, iso(inicio), iso(fim), diaInteiro,
						area == null ? "" : ",\"idAreaComum\":" + area.getId());
		return mockMvc.perform(post("/evento").with(quem).contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private ResultActions decidir(RequestPostProcessor quem, Long id, boolean aprovar, String motivo) throws Exception {
		String corpo = "{\"aprovar\":%s%s}".formatted(aprovar, motivo == null ? "" : ",\"motivo\":\"" + motivo + "\"");
		return mockMvc.perform(post("/evento/" + id + "/decisao").with(quem).contentType(MediaType.APPLICATION_JSON)
				.content(corpo));
	}

	private ResultActions listar(RequestPostProcessor quem) throws Exception {
		return mockMvc.perform(get("/evento").with(quem)).andExpect(status().isOk());
	}

	private static String iso(LocalDateTime hora) {
		return OffsetDateTime.of(hora, ZoneId.systemDefault().getRules().getOffset(hora))
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	private static LocalDateTime amanha(int hora) {
		return LocalDate.now().plusDays(1).atTime(hora, 0);
	}

	private static LocalDateTime ontem(int hora) {
		return LocalDate.now().minusDays(1).atTime(hora, 0);
	}

	private RequestPostProcessor morador(String email) {
		return perfil(email, "MORADOR", List.of("MORADOR"), List.of("EVENTO_LER", "EVENTO_ESCREVER", "AREA_LER"));
	}

	private RequestPostProcessor sindico() {
		return perfil("sindico@teste.com", "SINDICO", List.of("SINDICO"),
				List.of("EVENTO_LER", "EVENTO_ESCREVER", "EVENTO_APROVAR", "AREA_LER", "AREA_ESCREVER"));
	}

	/** O profile de teste não liga a segurança: a autenticação vai direto no SecurityContextHolder. */
	private RequestPostProcessor perfil(String email, String tipo, List<String> papeis, List<String> permissoes) {
		Jwt token = Jwt.withTokenValue("teste").header("alg", "none").subject(email)
				.claim("tipoPerfil", tipo).claim("papeis", papeis)
				.claim("condominio", condominio.getId()).build();
		JwtAuthenticationToken autenticacao = new JwtAuthenticationToken(token,
				permissoes.stream().map(SimpleGrantedAuthority::new).toList());
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

	private void pessoa(String email, Unidade unidade) {
		Pessoa pessoa = new Pessoa();
		pessoa.setNomeCompleto("Morador " + unidade.getNumero());
		pessoa.setEmail(email);
		pessoa.setCondominio(condominio);
		pessoa.setUnidade(unidade);
		pessoaRepository.save(pessoa);
	}

	private AreaComum area(String nome, boolean reservavel, LocalTime abertura, LocalTime fechamento) {
		AreaComum area = new AreaComum();
		area.setCondominio(condominio);
		area.setNome(nome);
		area.setReservavel(reservavel);
		area.setHoraAbertura(abertura);
		area.setHoraFechamento(fechamento);
		return areaRepository.save(area);
	}

}
