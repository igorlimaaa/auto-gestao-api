package br.com.gestaocondial.autogestao.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.EventoDto;
import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;
import br.com.gestaocondial.autogestao.enumeration.TipoEventoEnum;

import br.com.gestaocondial.autogestao.api.model.Evento;

/** Wire ({@code api.model.Evento}) <-> DTO interno. */
@Mapper(componentModel = "spring")
public interface EventoApiMapper {

	EventoDto toDto(Evento api);

	Evento toApi(EventoDto dto);

	List<Evento> toApiList(List<EventoDto> dtos);

	// Enums mapeados à mão, pelo mesmo motivo de CobrancaOrdinariaApiMapper.
	default TipoEventoEnum map(Evento.TipoEnum value) {
		return value == null ? null : TipoEventoEnum.valueOf(value.name());
	}

	default Evento.TipoEnum map(TipoEventoEnum value) {
		return value == null ? null : Evento.TipoEnum.valueOf(value.name());
	}

	default SituacaoEventoEnum map(Evento.SituacaoEnum value) {
		return value == null ? null : SituacaoEventoEnum.valueOf(value.name());
	}

	default Evento.SituacaoEnum map(SituacaoEventoEnum value) {
		return value == null ? null : Evento.SituacaoEnum.valueOf(value.name());
	}

	// date-time do contrato é OffsetDateTime; a agenda guarda a hora local do servidor.
	default LocalDateTime map(OffsetDateTime value) {
		return value == null ? null : value.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
	}

	default OffsetDateTime map(LocalDateTime value) {
		return value == null ? null : value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
	}

}
