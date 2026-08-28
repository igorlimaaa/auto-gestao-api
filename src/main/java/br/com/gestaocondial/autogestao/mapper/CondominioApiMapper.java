package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.CondominioDto;

/**
 * Converte entre o modelo de wire gerado a partir do contrato OpenAPI
 * ({@code api.model.Condominio}) e o DTO interno usado pela camada de service
 * ({@code dto.CondominioDto}).
 */
@Mapper(componentModel = "spring", uses = EnderecoApiMapper.class)
public interface CondominioApiMapper {

	CondominioDto toDto(br.com.gestaocondial.autogestao.api.model.Condominio api);

	br.com.gestaocondial.autogestao.api.model.Condominio toApi(CondominioDto dto);

	List<br.com.gestaocondial.autogestao.api.model.Condominio> toApiList(List<CondominioDto> dto);

}
