package br.com.gestaocondial.autogestao.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles(value = "test")
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
class CondominioControllerTest {
	
	private static final String URI = "/condominio";

	@Autowired
	private MockMvc mockmvc;

//	@Autowired
//	private TestEntityManager entityManager;

	@Test
	void retorno404SeErroNoEndereco() throws Exception {
		// GlobalExceptionHandler mapeia EnderecoNaoEncontradoException para 404
		// (antes desta modernização, a exception tinha @ResponseStatus(BAD_REQUEST)
		// direto na classe; centralizar o tratamento corrigiu o status para 404,
		// coerente com "referência a um recurso relacionado que não existe").
		String json = "{\n"
				+ "  \"ddd\": 81,\n"
				+ "  \"endereco\": {\n"
				+ "    \"id\": 10\n"
				+ "  },\n"
				+ "  \"numeroTelefone\": 32045371,\n"
				+ "  \"valorJuros\": 1.0,\n"
				+ "  \"valorMulta\": 2.0,\n"
				+ "  \"valorTaxaCondominial\": 500.0\n"
				+ "}";

		// @EnableWebSecurity tambem vale no profile "test", e nenhum dos
		// SecurityFilterChain declarados casa com ele (sao @Profile("dev") e
		// @Profile("prod")), entao vale a cadeia padrao do Spring Security:
		// exige requisicao autenticada e token CSRF no POST. Sem os dois
		// post-processors abaixo a requisicao e barrada com 403 antes de chegar
		// ao controller, e o teste nunca chega a exercitar o 404.
		mockmvc.perform(MockMvcRequestBuilders
				.post(URI)
				.with(jwt())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers
				.status().is(HttpStatus.NOT_FOUND.value()));

	}

}
