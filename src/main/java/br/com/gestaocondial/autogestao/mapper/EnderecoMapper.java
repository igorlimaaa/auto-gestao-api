package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.dto.EnderecoDto;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {

	Endereco enderecoDtoToDomain(EnderecoDto enderecoDto);

	EnderecoDto enderecoDomainToDto(Endereco enderecoDomain);

	List<EnderecoDto> listEnderecoDomainToDto(List<Endereco> listEndereco);

}
