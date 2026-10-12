package br.com.gestaocondial.autogestao.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.gestaocondial.autogestao.banking.InterApiException;
import br.com.gestaocondial.autogestao.banking.InterCobrancaClient;
import br.com.gestaocondial.autogestao.banking.InterSaldo;

/** Saldo da conta: valores, cache, escopo por condomínio e as falhas possíveis do banco. */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SaldoBancarioControllerTest {

	/** Cada teste usa um condomínio diferente: o cache de um minuto é por condomínio. */
	private static long proximoCondominio = 9_000;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InterCobrancaClient banco;

	private long idCondominio;

	@BeforeEach
	void prepararCenario() {
		idCondominio = ++proximoCondominio;
		when(banco.integrado(idCondominio)).thenReturn(true);
		when(banco.consultarSaldo(idCondominio)).thenReturn(new InterSaldo(new BigDecimal("12345.67"),
				new BigDecimal("100.00"), new BigDecimal("50.00"), null, new BigDecimal("0.00")));
	}

	@AfterEach
	void limpar() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void sindicoVeOSaldoDoProprioCondominioSemCache() throws Exception {
		mockMvc.perform(get("/saldoBancario").with(sindico(idCondominio)))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", containsString("no-store")))
				.andExpect(jsonPath("$.idCondominio").value(idCondominio))
				.andExpect(jsonPath("$.disponivel").value(12345.67))
				// 100 de cheque + 50 judicial + 0 administrativo (ausente).
				.andExpect(jsonPath("$.bloqueado").value(150.0))
				.andExpect(jsonPath("$.consultadoEm").exists());
	}

	@Test
	void consultasSeguidasNaoGastamChamadasDoBanco() throws Exception {
		mockMvc.perform(get("/saldoBancario").with(sindico(idCondominio))).andExpect(status().isOk());
		mockMvc.perform(get("/saldoBancario").with(sindico(idCondominio))).andExpect(status().isOk());

		verify(banco, times(1)).consultarSaldo(idCondominio);
	}

	@Test
	void naoConsultaOSaldoDeOutroCondominio() throws Exception {
		mockMvc.perform(get("/saldoBancario").param("idCondominio", String.valueOf(idCondominio + 1))
				.with(sindico(idCondominio)))
				.andExpect(status().isForbidden());
		verify(banco, never()).consultarSaldo(any());
	}

	@Test
	void condominioSemContaDoInterRecebe409() throws Exception {
		when(banco.integrado(idCondominio)).thenReturn(false);

		mockMvc.perform(get("/saldoBancario").with(sindico(idCondominio)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.erros[0].mensagem").value(containsString("não tem a conta")));
	}

	@Test
	void falhaDoBancoViraBadGatewayComMensagemFixa() throws Exception {
		when(banco.consultarSaldo(idCondominio))
				.thenThrow(new InterApiException("detalhe interno com certificado", null, 500));

		mockMvc.perform(get("/saldoBancario").with(sindico(idCondominio)))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.erros[0].mensagem").value(
						"Não foi possível consultar o banco agora. Tente novamente em instantes."));
	}

	private RequestPostProcessor sindico(long condominio) {
		Jwt token = Jwt.withTokenValue("teste").header("alg", "none").subject("sindico@teste.com")
				.claim("tipoPerfil", "SINDICO").claim("papeis", List.of("SINDICO"))
				.claim("condominio", condominio).build();
		JwtAuthenticationToken autenticacao = new JwtAuthenticationToken(token,
				List.of(new SimpleGrantedAuthority("SALDO_LER")));
		return request -> {
			SecurityContextHolder.getContext().setAuthentication(autenticacao);
			return request;
		};
	}

}
