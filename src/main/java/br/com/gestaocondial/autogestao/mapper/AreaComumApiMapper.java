package br.com.gestaocondial.autogestao.mapper;

import java.time.LocalTime;
import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.AreaComumDto;
import br.com.gestaocondial.autogestao.enumeration.ModoDeReservaEnum;

/** Wire ({@code api.model.AreaComum}) <-> DTO interno. Os horários trafegam como "HH:mm". */
@Mapper(componentModel = "spring")
public interface AreaComumApiMapper {

	AreaComumDto toDto(br.com.gestaocondial.autogestao.api.model.AreaComum api);

	br.com.gestaocondial.autogestao.api.model.AreaComum toApi(AreaComumDto dto);

	List<br.com.gestaocondial.autogestao.api.model.AreaComum> toApiList(List<AreaComumDto> dtos);

	// Enum mapeado à mão, pelo mesmo motivo de CobrancaOrdinariaApiMapper.
	default ModoDeReservaEnum map(br.com.gestaocondial.autogestao.api.model.AreaComum.ModoDeReservaEnum value) {
		return value == null ? null : ModoDeReservaEnum.valueOf(value.name());
	}

	default br.com.gestaocondial.autogestao.api.model.AreaComum.ModoDeReservaEnum map(ModoDeReservaEnum value) {
		return value == null ? null
				: br.com.gestaocondial.autogestao.api.model.AreaComum.ModoDeReservaEnum.valueOf(value.name());
	}

	default LocalTime map(String hora) {
		return hora == null || hora.isBlank() ? null : LocalTime.parse(hora);
	}

	default String map(LocalTime hora) {
		return hora == null ? null : String.format("%02d:%02d", hora.getHour(), hora.getMinute());
	}

}
