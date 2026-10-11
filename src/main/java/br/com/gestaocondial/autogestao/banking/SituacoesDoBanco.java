package br.com.gestaocondial.autogestao.banking;

import java.util.Set;

/**
 * Valores de {@code situacao} da cobrança no Inter (enum {@code BillingSituation} do SDK):
 * RECEBIDO, A_RECEBER, MARCADO_RECEBIDO, ATRASADO, CANCELADO, EXPIRADO, FALHA_EMISSAO,
 * EM_PROCESSAMENTO, PROTESTO. Guardadas como texto — o banco pode criar valores novos.
 */
public final class SituacoesDoBanco {

	public static final Set<String> PAGAS = Set.of("RECEBIDO", "MARCADO_RECEBIDO");

	public static final Set<String> CANCELADAS = Set.of("CANCELADO", "EXPIRADO");

	/** Nenhuma reconsulta muda mais nada: paga, cancelada ou expirada. */
	public static final Set<String> FINAIS = Set.of("RECEBIDO", "MARCADO_RECEBIDO", "CANCELADO", "EXPIRADO");

	private SituacoesDoBanco() {
	}

}
