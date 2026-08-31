package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.UnidadeDto;

/**
 * Converte entre o modelo de wire gerado a partir do contrato OpenAPI
 * ({@code api.model.Unidade}) e o DTO interno usado pela camada de service.
 */
@Mapper(componentModel = "spring", uses = CondominioApiMapper.class)
public interface UnidadeApiMapper {

	UnidadeDto toDto(br.com.gestaocondial.autogestao.api.model.Unidade api);

	br.com.gestaocondial.autogestao.api.model.Unidade toApi(UnidadeDto dto);

	List<br.com.gestaocondial.autogestao.api.model.Unidade> toApiList(List<UnidadeDto> dto);

}
