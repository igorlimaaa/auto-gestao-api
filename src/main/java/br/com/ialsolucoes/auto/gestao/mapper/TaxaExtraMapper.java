package br.com.ialsolucoes.auto.gestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.ialsolucoes.auto.gestao.domain.TaxaExtra;
import br.com.ialsolucoes.auto.gestao.dto.TaxaExtraDto;

@Mapper(componentModel = "spring")
public interface TaxaExtraMapper {
	
	TaxaExtraDto taxaExtraDomainToDto (TaxaExtra taxa);
	
	TaxaExtra taxaExtraDtoToDomain (TaxaExtraDto taxaDto);
	
	List<TaxaExtraDto> listTaxaExtraDomainToDto (List<TaxaExtra> listTaxaExtra);

}
