package br.com.gestaocondial.autogestao.service;

/**
 * Sincroniza as cobranças da taxa ordinária com o Banco Inter (somente leitura). Em ambos os
 * métodos, {@code idCondominio} nulo significa todos os condomínios; condomínio sem credenciais
 * do banco configuradas é ignorado.
 */
public interface SincronizacaoBancariaService {

	/**
	 * Importa as cobranças emitidas no banco (janela de 30 dias atrás a 60 à frente, por
	 * vencimento): cria ou completa o registro local e grava o que não muda depois da emissão.
	 */
	void importar(Long idCondominio);

	/** Reconsulta, uma a uma, as cobranças vindas do banco que ainda não foram pagas nem canceladas. */
	void sincronizarPendentes(Long idCondominio);

}
