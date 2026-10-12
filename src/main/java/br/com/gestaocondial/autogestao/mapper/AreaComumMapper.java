package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.gestaocondial.autogestao.domain.AreaComum;
import br.com.gestaocondial.autogestao.dto.AreaComumDto;

@Mapper(componentModel = "spring")
public interface AreaComumMapper {

	@Mapping(target = "idCondominio", source = "condominio.id")
	AreaComumDto toDto(AreaComum area);

	List<AreaComumDto> toDtoList(List<AreaComum> areas);

}
