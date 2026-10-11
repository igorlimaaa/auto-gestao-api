package br.com.gestaocondial.autogestao.banking;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

/**
 * Cliente <b>somente leitura</b> da API de Cobrança do Banco Inter — apenas {@code GET} de dados;
 * o único {@code POST} é o do token OAuth, exigido pela autenticação. Endpoints, parâmetros de
 * paginação ({@code paginacao.paginaAtual}, 0-based) e URL do token seguem o SDK oficial
 * (inter-co/pj-sdk-java): {@code /oauth/v2/token} e {@code /cobranca/v3/cobrancas}.
 *
 * <p>Cada condomínio tem conta própria, então cada um tem seu {@link RestClient} (com o
 * certificado dele) e seu token, ambos em cache. O token vale ~1h; é renovado um pouco antes de
 * expirar, e descartado se o banco responder 401.</p>
 */
@Slf4j
@Component
public class InterCobrancaClient {

	private static final int ITENS_POR_PAGINA = 100;

	private static final Duration MARGEM_DE_EXPIRACAO = Duration.ofSeconds(60);

	private final InterProperties propriedades;

	private final CredenciaisInterProvider credenciais;

	private final Map<Long, RestClient> clientes = new ConcurrentHashMap<>();

	private final Map<Long, Token> tokens = new ConcurrentHashMap<>();

	public InterCobrancaClient(InterProperties propriedades, CredenciaisInterProvider credenciais) {
		this.propriedades = propriedades;
		this.credenciais = credenciais;
	}

	/** O condomínio tem conta Inter configurada? */
	public boolean integrado(Long idCondominio) {
		return credenciais.buscar(idCondominio).isPresent();
	}

	/** {@code GET /cobranca/v3/cobrancas} — percorre todas as páginas da janela (por vencimento). */
	public List<InterCobranca> listar(Long idCondominio, LocalDate dataInicial, LocalDate dataFinal) {
		List<InterCobranca> todas = new ArrayList<>();
		int pagina = 0;
		InterCobranca.Pagina atual;
		do {
			int paginaAtual = pagina;
			atual = executar(idCondominio, cliente -> cliente.get()
					.uri(uri -> uri.path("/cobranca/v3/cobrancas")
							.queryParam("dataInicial", dataInicial)
							.queryParam("dataFinal", dataFinal)
							.queryParam("paginacao.paginaAtual", paginaAtual)
							.queryParam("paginacao.itensPorPagina", ITENS_POR_PAGINA)
							.build())
					.retrieve()
					.body(InterCobranca.Pagina.class));
			if (atual == null) {
				break;
			}
			todas.addAll(atual.cobrancasOuVazio());
			pagina++;
		} while (!atual.ehUltima());
		return todas;
	}

	/** {@code GET /cobranca/v3/cobrancas/{codigoSolicitacao}}. */
	public InterCobranca recuperar(Long idCondominio, String codigoSolicitacao) {
		return executar(idCondominio, cliente -> cliente.get()
				.uri("/cobranca/v3/cobrancas/{codigo}", codigoSolicitacao)
				.retrieve()
				.body(InterCobranca.class));
	}

	private <T> T executar(Long idCondominio, java.util.function.Function<RestClient, T> chamada) {
		try {
			return chamarComToken(idCondominio, chamada);
		} catch (RestClientResponseException e) {
			if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
				// Token revogado/expirado antes do previsto: renova e tenta uma única vez.
				tokens.remove(idCondominio);
				try {
					return chamarComToken(idCondominio, chamada);
				} catch (RestClientException segunda) {
					throw traduzir(idCondominio, segunda);
				}
			}
			throw traduzir(idCondominio, e);
		} catch (RestClientException e) {
			throw traduzir(idCondominio, e);
		}
	}

	private <T> T chamarComToken(Long idCondominio, java.util.function.Function<RestClient, T> chamada) {
		RestClient cliente = clienteDo(idCondominio);
		String token = tokenDo(idCondominio, cliente);
		// O token é anexado por requisição: o RestClient é compartilhado entre chamadas.
		RestClient comToken = cliente.mutate().defaultHeader("Authorization", "Bearer " + token).build();
		return chamada.apply(comToken);
	}

	private String tokenDo(Long idCondominio, RestClient cliente) {
		Token existente = tokens.get(idCondominio);
		if (existente != null && existente.validoAte().isAfter(Instant.now().plus(MARGEM_DE_EXPIRACAO))) {
			return existente.valor();
		}
		synchronized (this) {
			existente = tokens.get(idCondominio);
			if (existente != null && existente.validoAte().isAfter(Instant.now().plus(MARGEM_DE_EXPIRACAO))) {
				return existente.valor();
			}
			CredencialInter credencial = credenciais.buscar(idCondominio).orElseThrow(() -> new InterApiException(
					"Condomínio " + idCondominio + " não tem credenciais do Banco Inter configuradas."));
			MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
			formulario.add("client_id", credencial.clientId());
			formulario.add("client_secret", credencial.clientSecret());
			formulario.add("grant_type", "client_credentials");
			formulario.add("scope", propriedades.escopos());

			RespostaDeToken resposta = cliente.post()
					.uri("/oauth/v2/token")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(formulario)
					.retrieve()
					.body(RespostaDeToken.class);
			if (resposta == null || resposta.accessToken() == null) {
				throw new InterApiException("O Banco Inter não devolveu um token de acesso.");
			}
			long segundos = resposta.expiresIn() != null ? resposta.expiresIn() : 3600;
			Token novo = new Token(resposta.accessToken(), Instant.now().plusSeconds(segundos));
			tokens.put(idCondominio, novo);
			return novo.valor();
		}
	}

	private RestClient clienteDo(Long idCondominio) {
		return clientes.computeIfAbsent(idCondominio, id -> {
			CredencialInter credencial = credenciais.buscar(id).orElseThrow(() -> new InterApiException(
					"Condomínio " + id + " não tem credenciais do Banco Inter configuradas."));
			HttpClient http = HttpClient.newBuilder()
					.sslContext(InterSslContexts.criar(credencial))
					.connectTimeout(Duration.ofSeconds(10))
					.build();
			JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
			fabrica.setReadTimeout(Duration.ofSeconds(30));
			return RestClient.builder().baseUrl(propriedades.urlBase()).requestFactory(fabrica).build();
		});
	}

	/** Nunca inclui o corpo da resposta nem cabeçalhos na mensagem: podem carregar segredos. */
	private InterApiException traduzir(Long idCondominio, RestClientException e) {
		String detalhe = e instanceof RestClientResponseException r
				? "HTTP " + r.getStatusCode().value()
				: e.getClass().getSimpleName();
		return new InterApiException("Falha ao consultar o Banco Inter (condomínio " + idCondominio + "): " + detalhe, e);
	}

	/** Descarta o estado em cache de um condomínio (ex.: após trocar o certificado). */
	public void esquecer(Long idCondominio) {
		clientes.remove(idCondominio);
		tokens.remove(idCondominio);
	}

	private record Token(String valor, Instant validoAte) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record RespostaDeToken(@JsonProperty("access_token") String accessToken,
			@JsonProperty("expires_in") Integer expiresIn) {
	}

}
