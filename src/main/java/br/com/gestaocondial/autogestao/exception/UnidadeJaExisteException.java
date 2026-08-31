package br.com.gestaocondial.autogestao.exception;

/**
 * Já existe uma unidade com o mesmo bloco e número no condomínio — espelha o índice único de
 * {@code tb_unidade}.
 */
public class UnidadeJaExisteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnidadeJaExisteException(String message) {
		super(message);
	}

}
