package br.com.gestaocondial.autogestao.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.gestaocondial.autogestao.domain.Evento;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.dto.EventoDto;

/**
 * Entidade -> DTO. Os campos que dependem de quem pede (solicitante visível, o que pode fazer) não
 * vêm daqui: o service os preenche, porque dependem do perfil da requisição.
 */
@Mapper(componentModel = "spring")
public interface EventoMapper {

	@Mapping(target = "idCondominio", source = "condominio.id")
	@Mapping(target = "idAreaComum", source = "areaComum.id")
	@Mapping(target = "nomeDaAreaComum", source = "areaComum.nome")
	@Mapping(target = "idUnidade", source = "unidade.id")
	@Mapping(target = "rotuloDaUnidade", source = "unidade")
	@Mapping(target = "motivoDaDecisao", source = "motivoDecisao")
	@Mapping(target = "dataDaDecisao", source = "dataDecisao")
	@Mapping(target = "ehMeu", ignore = true)
	@Mapping(target = "podeEditar", ignore = true)
	@Mapping(target = "podeCancelar", ignore = true)
	@Mapping(target = "podeDecidir", ignore = true)
	EventoDto toDto(Evento evento);

	default String rotulo(Unidade unidade) {
		if (unidade == null) {
			return null;
		}
		String bloco = unidade.getBloco();
		return bloco == null || bloco.isEmpty() ? unidade.getNumero() : bloco + " " + unidade.getNumero();
	}

}
