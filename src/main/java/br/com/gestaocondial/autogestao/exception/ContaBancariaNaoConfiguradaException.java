package br.com.gestaocondial.autogestao.exception;

/** O condomínio não tem conta no Banco Inter configurada, então não há saldo nem cobrança a consultar. */
public class ContaBancariaNaoConfiguradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ContaBancariaNaoConfiguradaException(String message) {
		super(message);
	}

}
