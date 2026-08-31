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

	public CondominioDto updateCondominio(Long idCondominio, CondominioDto condominio);

	public void deleteCondominio(Long idCondominio);

	public TaxaExtraDto updateTaxaExtra(Long idTaxaExtra, TaxaExtraDto taxa);

	public void deleteTaxaExtra(Long idTaxaExtra);

}
