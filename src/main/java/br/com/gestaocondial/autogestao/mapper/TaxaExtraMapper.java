package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.domain.TaxaExtra;
import br.com.gestaocondial.autogestao.dto.TaxaExtraDto;

@Mapper(componentModel = "spring")
public interface TaxaExtraMapper {
	
	TaxaExtraDto taxaExtraDomainToDto (TaxaExtra taxa);
	
	TaxaExtra taxaExtraDtoToDomain (TaxaExtraDto taxaDto);
	
	List<TaxaExtraDto> listTaxaExtraDomainToDto (List<TaxaExtra> listTaxaExtra);

}
