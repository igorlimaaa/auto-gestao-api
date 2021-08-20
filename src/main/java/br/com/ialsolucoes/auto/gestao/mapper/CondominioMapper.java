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
	@Mapping(source = "endereco.id", target = "endereco.id")
	@Mapping(source = "endereco.complemento", target = "endereco.ds_complemento")
	@Mapping(source = "endereco.endereco", target = "endereco.ds_endereco")
	@Mapping(source = "endereco.cep", target = "endereco.nr_cep")
	Condominio condominioDtoToDomain(CondominioDto condominioDto);
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "ddd", target = "nr_ddd")
	@Mapping(source = "numeroTelefone", target = "nr_telefone")
	@Mapping(source = "valorTaxa", target = "vl_taxa")
	@Mapping(source = "endereco.id", target = "endereco.id")
	@Mapping(source = "endereco.complemento", target = "endereco.ds_complemento")
	@Mapping(source = "endereco.endereco", target = "endereco.ds_endereco")
	@Mapping(source = "endereco.cep", target = "endereco.nr_cep")
	List<Condominio> listCondominioDtoToListDomain(List<CondominioDto> condominioDto);
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "nr_ddd", target = "ddd")
	@Mapping(source = "nr_telefone", target = "numeroTelefone")
	@Mapping(source = "vl_taxa", target = "valorTaxa")
	@Mapping(source = "endereco.id", target = "endereco.id")
	@Mapping(source = "endereco.ds_complemento", target = "endereco.complemento")
	@Mapping(source = "endereco.ds_endereco", target = "endereco.endereco")
	@Mapping(source = "endereco.nr_cep", target = "endereco.cep")
	CondominioDto condominioDomainToDto(Condominio condominioDomain);
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "nr_ddd", target = "ddd")
	@Mapping(source = "nr_telefone", target = "numeroTelefone")
	@Mapping(source = "vl_taxa", target = "valorTaxa")
	@Mapping(source = "endereco.id", target = "endereco.id")
	@Mapping(source = "endereco.ds_complemento", target = "endereco.complemento")
	@Mapping(source = "endereco.ds_endereco", target = "endereco.endereco")
	@Mapping(source = "endereco.nr_cep", target = "endereco.cep")
	List<CondominioDto> listCondominioDomainToDto(List<Condominio> condominioDomain);

}
