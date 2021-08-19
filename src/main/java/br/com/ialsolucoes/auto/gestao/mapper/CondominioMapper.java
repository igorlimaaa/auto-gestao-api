package br.com.ialsolucoes.auto.gestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.ialsolucoes.auto.gestao.domain.Condominio;
import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;

@Mapper(componentModel = "spring")
public interface CondominioMapper {
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "ddd", target = "nr_ddd")
	@Mapping(source = "numeroTelefone", target = "nr_telefone")
	@Mapping(source = "valorTaxa", target = "vl_taxa")
	Condominio condominioDtoToDomain(CondominioDto condominioDto);
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "ddd", target = "nr_ddd")
	@Mapping(source = "numeroTelefone", target = "nr_telefone")
	@Mapping(source = "valorTaxa", target = "vl_taxa")
	List<Condominio> listCondominioDtoToListDomain(List<CondominioDto> condominioDto);
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "nr_ddd", target = "ddd")
	@Mapping(source = "nr_telefone", target = "numeroTelefone")
	@Mapping(source = "vl_taxa", target = "valorTaxa")
	CondominioDto condominioDomainToDto(Condominio condominioDomain);
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "nr_ddd", target = "ddd")
	@Mapping(source = "nr_telefone", target = "numeroTelefone")
	@Mapping(source = "vl_taxa", target = "valorTaxa")
	List<CondominioDto> listCondominioDomainToDto(List<Condominio> condominioDomain);

}
