package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.dto.CondominioDto;

@Mapper(componentModel = "spring")
public interface CondominioMapper {
	
	Condominio condominioDtoToDomain(CondominioDto condominioDto);
	
	List<Condominio> listCondominioDtoToListDomain(List<CondominioDto> condominioDto);
	
	CondominioDto condominioDomainToDto(Condominio condominioDomain);
	
	List<CondominioDto> listCondominioDomainToDto(List<Condominio> condominioDomain);

}
