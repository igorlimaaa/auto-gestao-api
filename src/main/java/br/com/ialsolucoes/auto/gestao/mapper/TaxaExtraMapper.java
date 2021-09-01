package br.com.ialsolucoes.auto.gestao.mapper;

import org.mapstruct.Mapper;

import br.com.ialsolucoes.auto.gestao.domain.TaxaExtra;
import br.com.ialsolucoes.auto.gestao.dto.TaxaExtraDto;

@Mapper(componentModel = "spring")
public interface TaxaExtraMapper {
	
	TaxaExtraDto taxaExtraDomainToDto (TaxaExtra taxa);
	
	TaxaExtra taxaExtraDtoToDomain (TaxaExtraDto taxaDto);

}
