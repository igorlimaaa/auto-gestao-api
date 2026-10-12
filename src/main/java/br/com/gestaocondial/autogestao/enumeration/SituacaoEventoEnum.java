package br.com.gestaocondial.autogestao.enumeration;

/** Em que pé está o evento. Só {@code APROVADO} aparece para todo o condomínio. */
public enum SituacaoEventoEnum {

	/** Cadastrado por morador, aguardando o síndico ou o administrador. */
	PENDENTE,

	APROVADO,

	RECUSADO,

	CANCELADO

}
