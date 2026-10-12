package br.com.gestaocondial.autogestao.service;

import br.com.gestaocondial.autogestao.dto.BoletoPdfDto;

public interface BoletoService {

	/**
	 * Busca o PDF no Banco Inter na hora (nada é guardado). Exige que a cobrança seja do alcance do
	 * perfil, tenha vindo do banco e esteja em aberto.
	 */
	BoletoPdfDto obterPdf(Long idCobrancaOrdinaria);

}
