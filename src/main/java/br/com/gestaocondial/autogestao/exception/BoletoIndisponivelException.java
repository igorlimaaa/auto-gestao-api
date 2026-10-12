package br.com.gestaocondial.autogestao.exception;

/** A cobrança existe, mas não há boleto a mostrar: sem vínculo com o banco, ou já paga/cancelada. */
public class BoletoIndisponivelException extends RuntimeException {

	public BoletoIndisponivelException(String mensagem) {
		super(mensagem);
	}

}
