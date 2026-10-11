package br.com.gestaocondial.autogestao.enumeration;

/**
 * Nunca persistido — calculado a partir de {@code dataPagamento}/{@code dataVencimento}/{@code situacaoBanco} no
 * momento da leitura (ver {@code CobrancaOrdinariaImpl.calcularSituacao}).
 */
public enum SituacaoCobrancaEnum {

	PENDENTE,
	PAGO,
	INADIMPLENTE,
	/** Cancelada ou expirada no banco — não é cobrada nem conta como inadimplência. */
	CANCELADA

}
