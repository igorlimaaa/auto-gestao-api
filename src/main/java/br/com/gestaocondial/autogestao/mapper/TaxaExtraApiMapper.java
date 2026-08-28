package br.com.gestaocondial.autogestao.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.TaxaExtraDto;

/**
 * Converte entre o modelo de wire gerado a partir do contrato OpenAPI
 * ({@code api.model.TaxaExtra}) e o DTO interno usado pela camada de service
 * ({@code dto.TaxaExtraDto}). O contrato representa {@code dataCadastro} como
 * {@link OffsetDateTime} (padrão OpenAPI date-time); o domínio/DTO interno
 * usa {@link Date} — por isso a conversão explícita abaixo.
 */
@Mapper(componentModel = "spring", uses = CondominioApiMapper.class)
public interface TaxaExtraApiMapper {

	TaxaExtraDto toDto(br.com.gestaocondial.autogestao.api.model.TaxaExtra api);

	br.com.gestaocondial.autogestao.api.model.TaxaExtra toApi(TaxaExtraDto dto);

	List<br.com.gestaocondial.autogestao.api.model.TaxaExtra> toApiList(List<TaxaExtraDto> dto);

	default Date map(OffsetDateTime value) {
		return value == null ? null : Date.from(value.toInstant());
	}

	default OffsetDateTime map(Date value) {
		return value == null ? null : value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
	}

}
