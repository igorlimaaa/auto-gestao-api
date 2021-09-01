package br.com.ialsolucoes.auto.gestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.ialsolucoes.auto.gestao.domain.Condominio;
import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;

@Mapper(componentModel = "spring")
public interface CondominioMapper {
	
	Condominio condominioDtoToDomain(CondominioDto condominioDto);
	
	List<Condominio> listCondominioDtoToListDomain(List<CondominioDto> condominioDto);
	
	CondominioDto condominioDomainToDto(Condominio condominioDomain);
	
	List<CondominioDto> listCondominioDomainToDto(List<Condominio> condominioDomain);

}
