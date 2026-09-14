package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.dto.CobrancaOrdinariaDto;
import br.com.gestaocondial.autogestao.enumeration.SituacaoCobrancaEnum;

/**
 * Converte entre o modelo de wire gerado a partir do contrato OpenAPI
 * ({@code api.model.CobrancaOrdinaria}) e o DTO interno usado pela camada de service. Datas em
 * `format: date` já geram {@code java.time.LocalDate} nos dois lados — sem conversão manual,
 * diferente do par {@code Date}/{@code OffsetDateTime} de {@code TaxaExtraApiMapper}.
 */
@Mapper(componentModel = "spring", uses = UnidadeApiMapper.class)
public interface CobrancaOrdinariaApiMapper {

	CobrancaOrdinariaDto toDto(br.com.gestaocondial.autogestao.api.model.CobrancaOrdinaria api);

	br.com.gestaocondial.autogestao.api.model.CobrancaOrdinaria toApi(CobrancaOrdinariaDto dto);

	List<br.com.gestaocondial.autogestao.api.model.CobrancaOrdinaria> toApiList(List<CobrancaOrdinariaDto> dto);

	// Mapeamento manual do enum, pelo mesmo motivo documentado em PessoaApiMapper: o codegen
	// automático de enum-to-enum do MapStruct 1.6.3 é instável para este tipo de par gerado
	// dentro do modelo de wire. Nomes de constante identicos nos dois lados.
	default SituacaoCobrancaEnum map(
			br.com.gestaocondial.autogestao.api.model.CobrancaOrdinaria.SituacaoEnum value) {
		return value == null ? null : SituacaoCobrancaEnum.valueOf(value.name());
	}

	default br.com.gestaocondial.autogestao.api.model.CobrancaOrdinaria.SituacaoEnum map(SituacaoCobrancaEnum value) {
		return value == null ? null
				: br.com.gestaocondial.autogestao.api.model.CobrancaOrdinaria.SituacaoEnum.valueOf(value.name());
	}

}
