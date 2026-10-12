package br.com.gestaocondial.autogestao.enumeration;

/** Por que uma cobrança emitida no banco não virou cobrança no sistema. */
public enum MotivoDePendenciaEnum {

	/** Nenhuma unidade cadastrada com o número do {@code seuNumero}. */
	SEM_UNIDADE,

	/** O mesmo número existe em mais de um bloco, e o {@code seuNumero} não traz o bloco. */
	UNIDADE_AMBIGUA,

	/** A unidade está marcada como isenta (desconto de 100%), mas o banco emitiu cobrança para ela. */
	UNIDADE_ISENTA,

	/** A unidade já tem outra cobrança do banco nessa competência. */
	COMPETENCIA_DUPLICADA

}
