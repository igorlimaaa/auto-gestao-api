package br.com.gestaocondial.autogestao.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Faz a camada de segurança responder no mesmo envelope de erro do
 * {@link GlobalExceptionHandler} ({@code ListaDeErrosOutputDto}), em vez do corpo vazio padrão
 * do Spring Security. É o 401 em JSON que o frontend usa como gatilho para levar o usuário de
 * volta ao login.
 */
@Component
public class RespostaDeErroDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		escrever(response, HttpStatus.UNAUTHORIZED,
				"Token de autenticação ausente, expirado ou inválido. Autentique-se novamente.");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		escrever(response, HttpStatus.FORBIDDEN,
				"O perfil ativo desta sessão não tem permissão para acessar este recurso.");
	}

	private void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write("{\"erros\":[{\"parametro\":null,\"mensagem\":\"" + escapar(mensagem)
				+ "\"}],\"quantidadeDeErros\":1}");
	}

	private static String escapar(String valor) {
		return valor.replace("\\", "\\\\").replace("\"", "\\\"");
	}

}
