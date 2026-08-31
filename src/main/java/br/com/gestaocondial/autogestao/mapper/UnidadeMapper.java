package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.dto.UnidadeDto;

@Mapper(componentModel = "spring", uses = CondominioMapper.class)
public interface UnidadeMapper {

	/** {@code quantidadeDeMoradores} é contado pelo service, não vem da entidade. */
	@Mapping(target = "quantidadeDeMoradores", ignore = true)
	UnidadeDto unidadeDomainToDto(Unidade unidade);

	Unidade unidadeDtoToDomain(UnidadeDto unidadeDto);

	List<UnidadeDto> listUnidadeDomainToDto(List<Unidade> unidades);

}
