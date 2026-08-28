package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.MeioContatoDto;
import br.com.gestaocondial.autogestao.dto.PessoaDto;
import br.com.gestaocondial.autogestao.enumeration.MeioContatoEnum;

/**
 * Converte entre os modelos de wire gerados a partir do contrato OpenAPI
 * ({@code api.model.Pessoa}, {@code api.model.MeioContato}) e os DTOs
 * internos usados pela camada de service ({@code dto.PessoaDto},
 * {@code dto.MeioContatoDto}). O enum de wire (gerado dentro de
 * {@code api.model.MeioContato}) é mapeado por nome de constante para
 * {@link br.com.gestaocondial.autogestao.enumeration.MeioContatoEnum}
 * (EMAIL/TELEF em ambos).
 */
@Mapper(componentModel = "spring", uses = { CondominioApiMapper.class, EnderecoApiMapper.class })
public interface PessoaApiMapper {

	PessoaDto toDto(br.com.gestaocondial.autogestao.api.model.Pessoa api);

	br.com.gestaocondial.autogestao.api.model.Pessoa toApi(PessoaDto dto);

	List<br.com.gestaocondial.autogestao.api.model.Pessoa> toApiList(List<PessoaDto> dto);

	List<PessoaDto> toDtoList(List<br.com.gestaocondial.autogestao.api.model.Pessoa> api);

	MeioContatoDto toDto(br.com.gestaocondial.autogestao.api.model.MeioContato api);

	br.com.gestaocondial.autogestao.api.model.MeioContato toApi(MeioContatoDto dto);

	// Mapeamento manual do enum (em vez de deixar o MapStruct gerar o switch
	// automaticamente): o codegen automático de enum-to-enum do MapStruct
	// 1.6.3 produz um switch corrompido para este par EMAIL/TELEF
	// (br.com.gestaocondial.autogestao.api.model.MeioContato.TipoMeioContatoEnum
	// <-> MeioContatoEnum) — provável bug do gerador. Os nomes das constantes
	// são idênticos nos dois enums, então valueOf(name()) é equivalente e
	// evita depender do switch gerado.
	default MeioContatoEnum map(br.com.gestaocondial.autogestao.api.model.MeioContato.TipoMeioContatoEnum value) {
		return value == null ? null : MeioContatoEnum.valueOf(value.name());
	}

	default br.com.gestaocondial.autogestao.api.model.MeioContato.TipoMeioContatoEnum map(MeioContatoEnum value) {
		return value == null ? null
				: br.com.gestaocondial.autogestao.api.model.MeioContato.TipoMeioContatoEnum.valueOf(value.name());
	}

}
