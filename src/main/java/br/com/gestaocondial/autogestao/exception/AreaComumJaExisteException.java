package br.com.gestaocondial.autogestao.exception;

/** O condomínio já tem uma área comum com esse nome. */
public class AreaComumJaExisteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AreaComumJaExisteException(String message) {
		super(message);
	}

}
