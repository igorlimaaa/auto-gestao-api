package br.com.ialsolucoes.auto.gestao.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.ialsolucoes.auto.gestao.domain.Endereco;
import br.com.ialsolucoes.auto.gestao.dto.EnderecoDto;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "cep", target = "nr_cep")
	@Mapping(source = "complemento", target = "ds_complemento")
	@Mapping(source = "endereco", target = "ds_endereco")
	Endereco enderecoDtoToDomain(EnderecoDto enderecoDto);
	
	@Mapping(source = "id", target = "id")
	@Mapping(source = "nr_cep", target = "cep")
	@Mapping(source = "ds_complemento", target = "complemento")
	@Mapping(source = "ds_endereco", target = "endereco")
	EnderecoDto enderecoDomainToDto(Endereco enderecoDomain);

}
