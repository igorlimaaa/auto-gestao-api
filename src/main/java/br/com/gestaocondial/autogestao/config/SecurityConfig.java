package br.com.gestaocondial.autogestao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança com dois profiles:
 * - "dev": permite tudo (sem autenticação) — segurança desligada para debug local.
 * - "prod": OAuth2 Resource Server (autenticação JWT obrigatória).
 * - "test": desabilitado via application-test.properties (SecurityAutoConfiguration excluída).
 *
 * Cada bean tem seu próprio @Profile, não a classe inteira.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {


	// Profile DEV: permite tudo sem autenticação
	@Bean
	@Profile("dev")
	public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/docs", "/docs/**", "/docs/index.html").permitAll()
				.anyRequest().permitAll());
		return http.build();
	}

	// Profile PROD: autenticação JWT obrigatória
	@Bean
	@Profile("prod")
	public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/docs", "/docs/**", "/webjars/**", "/openapi/**", "/v3/api-docs/**",
					"/actuator/health", "/error")
				.permitAll()
				.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
				// jwk-set-uri resolvido via spring.security.oauth2.resourceserver.jwt.issuer-uri
				// em application.properties
			}));

		return http.build();
	}

}
