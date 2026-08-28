package br.com.gestaocondial.autogestao.exception;

/**
 * Unchecked: os métodos do controller agora implementam as interfaces
 * geradas pelo openapi-generator a partir do contrato OpenAPI, cuja
 * assinatura não declara "throws" (contrato não modela exceptions
 * verificadas). Antes desta modernização era uma checked exception; manter
 * checked exigiria capturar e reembrulhar em toda implementação de
 * controller só para satisfazer o compilador — sem ganho real, já que o
 * tratamento é sempre centralizado no {@link
 * br.com.gestaocondial.autogestao.config.GlobalExceptionHandler}.
 */
public class EnderecoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EnderecoNaoEncontradoException(String message) {
		super(message);
	}

}
