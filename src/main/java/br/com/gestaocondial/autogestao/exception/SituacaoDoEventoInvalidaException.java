package br.com.gestaocondial.autogestao.exception;

/** A operação não cabe na situação atual do evento (decidir um que não está pendente, cancelar um recusado). */
public class SituacaoDoEventoInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SituacaoDoEventoInvalidaException(String message) {
		super(message);
	}

}
