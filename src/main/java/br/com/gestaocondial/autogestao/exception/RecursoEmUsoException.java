package br.com.gestaocondial.autogestao.exception;

/**
 * Exclusão recusada porque o registro ainda está vinculado a outros. Apagar em cascata
 * silenciosamente levaria junto cadastro de gente — a remoção precisa ser explícita, uma
 * ponta de cada vez.
 */
public class RecursoEmUsoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RecursoEmUsoException(String message) {
		super(message);
	}

}
