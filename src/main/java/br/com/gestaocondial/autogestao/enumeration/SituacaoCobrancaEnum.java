package br.com.gestaocondial.autogestao.enumeration;

/**
 * Nunca persistido — calculado a partir de {@code dataPagamento}/{@code dataVencimento} no
 * momento da leitura (ver {@code CobrancaOrdinariaImpl.calcularSituacao}).
 */
public enum SituacaoCobrancaEnum {

	PENDENTE,
	PAGO,
	INADIMPLENTE

}
