package br.com.gestaocondial.autogestao.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.api.model.SaldoBancario;
import br.com.gestaocondial.autogestao.dto.SaldoBancarioDto;

@Mapper(componentModel = "spring")
public interface SaldoBancarioApiMapper {

	SaldoBancario toApi(SaldoBancarioDto dto);

	default OffsetDateTime map(LocalDateTime value) {
		return value == null ? null : value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
	}

}
