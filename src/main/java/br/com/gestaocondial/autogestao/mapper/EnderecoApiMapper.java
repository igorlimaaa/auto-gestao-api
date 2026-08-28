package br.com.gestaocondial.autogestao.mapper;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.EnderecoDto;

/**
 * Converte entre o modelo de wire gerado a partir do contrato OpenAPI
 * ({@code api.model.Endereco}) e o DTO interno usado pela camada de service
 * ({@code dto.EnderecoDto}). Os controllers implementam as interfaces geradas
 * (contrato) e delegam para os services usando os DTOs internos — este mapper
 * faz a ponte entre as duas representações.
 */
@Mapper(componentModel = "spring")
public interface EnderecoApiMapper {

	EnderecoDto toDto(br.com.gestaocondial.autogestao.api.model.Endereco api);

	br.com.gestaocondial.autogestao.api.model.Endereco toApi(EnderecoDto dto);

}
