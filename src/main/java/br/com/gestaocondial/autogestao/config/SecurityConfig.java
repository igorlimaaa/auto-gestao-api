package br.com.gestaocondial.autogestao.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Toda API de domínio exige um JWT válido emitido pelo
 * {@code gestao-condial-oauth-service}; sem ele a resposta é 401 em JSON, e o frontend leva o
 * usuário de volta ao login.
 *
 * <p>Só ficam abertos os endpoints que precisam ser alcançáveis sem sessão: a documentação e o
 * {@code /actuator/health} — este último porque o job de deploy o consulta para confirmar que
 * o serviço subiu.</p>
 *
 * <p>Esta cadeia vale em <b>dev e prod igualmente</b>. Antes, o profile dev era
 * {@code permitAll()} em tudo: o comportamento de autenticação só aparecia em produção, que é
 * o pior lugar para descobrir que ele está errado. O profile {@code test} continua sem
 * segurança, desligada via {@code spring.autoconfigure.exclude} em
 * {@code application-test.properties}.</p>
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

	private static final String[] ROTAS_PUBLICAS = {
			"/docs", "/docs/**", "/webjars/**", "/openapi/**", "/v3/api-docs/**",
			"/actuator/health", "/actuator/health/**", "/error"
	};

	private final List<String> origensPermitidas;

	private final String jwkSetUri;

	private final String issuerUri;

	public SecurityConfig(
			@Value("${seguranca.cors.origens-permitidas:http://localhost:3000}") List<String> origensPermitidas,
			@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
			@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
		this.origensPermitidas = origensPermitidas;
		this.jwkSetUri = jwkSetUri;
		this.issuerUri = issuerUri;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			ConversorDeAutoridadesDoJwt conversorDeAutoridades,
			RespostaDeErroDeSeguranca respostaDeErro) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(ROTAS_PUBLICAS).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDeAutoridades))
						.authenticationEntryPoint(respostaDeErro)
						.accessDeniedHandler(respostaDeErro))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(respostaDeErro)
						.accessDeniedHandler(respostaDeErro));

		return http.build();
	}

	/**
	 * Decoder próprio, no lugar do autoconfigurado pelo Spring Boot, para somar o
	 * {@link ValidadorDeEscopoDeAcesso} às validações padrão (assinatura, expiração e
	 * {@code iss}).
	 */
	@Bean
	public JwtDecoder jwtDecoder() {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>(
				JwtValidators.createDefaultWithIssuer(issuerUri),
				new ValidadorDeEscopoDeAcesso()));
		return decoder;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuracao = new CorsConfiguration();
		configuracao.setAllowedOrigins(origensPermitidas);
		configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		configuracao.setAllowCredentials(true);
		configuracao.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
		fonte.registerCorsConfiguration("/**", configuracao);
		return fonte;
	}

}
