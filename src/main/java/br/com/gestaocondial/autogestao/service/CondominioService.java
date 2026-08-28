package br.com.gestaocondial.autogestao.service;

import java.util.List;

import br.com.gestaocondial.autogestao.dto.CondominioDto;
import br.com.gestaocondial.autogestao.dto.TaxaExtraDto;

public interface CondominioService {

	public List<CondominioDto> listCondominios();

	/**
	 * @throws br.com.gestaocondial.autogestao.exception.EnderecoNaoEncontradoException
	 *             (unchecked) se o endereço informado não existir.
	 */
	public CondominioDto createNewCondominio(CondominioDto condominioDTo);

	public CondominioDto findCondominio(Long idCondominio);

	public TaxaExtraDto createTaxaExtra(TaxaExtraDto taxaDto);

	public List<TaxaExtraDto> listTaxaExtra(Long idCondominio);

}
