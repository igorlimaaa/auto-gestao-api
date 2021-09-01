package br.com.ialsolucoes.auto.gestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.ialsolucoes.auto.gestao.domain.Endereco;
import br.com.ialsolucoes.auto.gestao.dto.EnderecoDto;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {

	Endereco enderecoDtoToDomain(EnderecoDto enderecoDto);

	EnderecoDto enderecoDomainToDto(Endereco enderecoDomain);

	List<EnderecoDto> listEnderecoDomainToDto(List<Endereco> listEndereco);

}
