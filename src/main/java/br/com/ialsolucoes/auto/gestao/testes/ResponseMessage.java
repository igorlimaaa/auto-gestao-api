package br.com.ialsolucoes.auto.gestao.testes;

import lombok.Setter;

import lombok.Getter;

@Getter
@Setter
public class ResponseMessage {
	private String message;

	public ResponseMessage(String message) {
		this.message = message;
	}

}
