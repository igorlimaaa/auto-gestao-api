package br.com.ialsolucoes.auto.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;
import br.com.ialsolucoes.auto.gestao.dto.TaxaExtraDto;

@Service
public interface CondominioService {
	
	public List<CondominioDto> listCondominios();
	
	public CondominioDto createNewCondominio(CondominioDto condominioDTo) throws Exception;
	
	public CondominioDto findCondominio(Long idCondominio);
	
	public TaxaExtraDto createTaxaExtra(TaxaExtraDto taxaDto);
	
	public List<TaxaExtraDto> listTaxaExtra(Long idCondominio);

}
