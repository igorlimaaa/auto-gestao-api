package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.dto.CobrancaOrdinariaDto;

@Mapper(componentModel = "spring", uses = UnidadeMapper.class)
public interface CobrancaOrdinariaMapper {

	/** {@code situacao} é calculada pelo service, nunca vem da entidade. */
	@Mapping(target = "situacao", ignore = true)
	CobrancaOrdinariaDto cobrancaOrdinariaDomainToDto(CobrancaOrdinaria cobranca);

	CobrancaOrdinaria cobrancaOrdinariaDtoToDomain(CobrancaOrdinariaDto cobrancaDto);

	List<CobrancaOrdinariaDto> listCobrancaOrdinariaDomainToDto(List<CobrancaOrdinaria> cobrancas);

}
