package br.com.gestaocondial.autogestao.banking;

/** Falha ao falar com o Banco Inter (rede, certificado, autenticação ou resposta inesperada). */
public class InterApiException extends RuntimeException {

	public InterApiException(String mensagem) {
		super(mensagem);
	}

	public InterApiException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}

}
