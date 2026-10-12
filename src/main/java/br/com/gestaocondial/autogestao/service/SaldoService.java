package br.com.gestaocondial.autogestao.service;

import br.com.gestaocondial.autogestao.dto.SaldoBancarioDto;

public interface SaldoService {

	/**
	 * Saldo da conta do condomínio no Banco Inter. Sem {@code idCondominio}, vale o do perfil ativo.
	 * Respostas ficam em cache por um minuto, para não gastar chamadas do banco a cada tela aberta.
	 */
	SaldoBancarioDto consultarSaldo(Long idCondominio);

}
