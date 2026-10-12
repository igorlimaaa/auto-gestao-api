package br.com.gestaocondial.autogestao.banking;

/** Falha ao falar com o Banco Inter (rede, certificado, autenticação ou resposta inesperada). */
public class InterApiException extends RuntimeException {

	/** Status HTTP devolvido pelo banco, ou {@code null} quando a falha foi antes de haver resposta. */
	private final Integer statusHttp;

	public InterApiException(String mensagem) {
		this(mensagem, null, null);
	}

	public InterApiException(String mensagem, Throwable causa) {
		this(mensagem, causa, null);
	}

	public InterApiException(String mensagem, Throwable causa, Integer statusHttp) {
		super(mensagem, causa);
		this.statusHttp = statusHttp;
	}

	public Integer getStatusHttp() {
		return statusHttp;
	}

}
