package br.com.gestaocondial.autogestao.service;

import java.time.LocalDate;
import java.util.List;

import br.com.gestaocondial.autogestao.dto.CobrancaBancoPendenteDto;
import br.com.gestaocondial.autogestao.dto.CobrancaOrdinariaDto;

public interface CobrancaOrdinariaService {

	List<CobrancaOrdinariaDto> listCobrancas(Long idCondominio, Long idUnidade, LocalDate competencia);

	CobrancaOrdinariaDto findCobranca(Long idCobrancaOrdinaria);

	CobrancaOrdinariaDto atualizarDadosDePagamento(Long idCobrancaOrdinaria, String pixCopiaCola,
			String linhaDigitavel);

	CobrancaOrdinariaDto registrarPagamento(Long idCobrancaOrdinaria, LocalDate dataPagamento);

	/**
	 * Cobranças do banco que não viraram cobrança no sistema (sem unidade, isenta etc.). Só perfis de
	 * visão ampla (administrador e síndico) as veem; os demais recebem lista vazia.
	 */
	List<CobrancaBancoPendenteDto> listPendenciasDoBanco(Long idCondominio);

}
