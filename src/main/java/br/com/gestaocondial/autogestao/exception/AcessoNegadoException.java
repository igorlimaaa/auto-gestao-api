package br.com.gestaocondial.autogestao.exception;

/**
 * O perfil ativo tem a permissão exigida, mas não sobre o condomínio pedido.
 */
public class AcessoNegadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AcessoNegadoException(String message) {
		super(message);
	}

}
