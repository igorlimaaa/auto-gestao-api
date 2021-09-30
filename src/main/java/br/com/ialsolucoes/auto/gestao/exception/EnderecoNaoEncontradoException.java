package br.com.ialsolucoes.auto.gestao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EnderecoNaoEncontradoException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EnderecoNaoEncontradoException(String message) {
		super(message);
	}
	
	

}
