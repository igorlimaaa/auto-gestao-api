package br.com.gestaocondial.autogestao.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.dto.UnidadeDto;

@Mapper(componentModel = "spring", uses = CondominioMapper.class)
public interface UnidadeMapper {

	/** {@code quantidadeDeMoradores} é contado pelo service, não vem da entidade. */
	@Mapping(target = "quantidadeDeMoradores", ignore = true)
	@Mapping(target = "fracaoIdeal", source = "condominio.quantidadeUnidades", qualifiedByName = "fracaoIdeal")
	UnidadeDto unidadeDomainToDto(Unidade unidade);

	Unidade unidadeDtoToDomain(UnidadeDto unidadeDto);

	List<UnidadeDto> listUnidadeDomainToDto(List<Unidade> unidades);

	/**
	 * Fração ideal igualitária: 1 / total de unidades, com 6 casas (a precisão que a coluna sempre
	 * teve). A soma das frações pode diferir de 1 por arredondamento (24 unidades → 0,041667 × 24
	 * = 1,000008). Sem a quantidade informada no condomínio, não há fração.
	 */
	@Named("fracaoIdeal")
	default BigDecimal fracaoIdeal(Integer quantidadeUnidades) {
		if (quantidadeUnidades == null || quantidadeUnidades <= 0) {
			return null;
		}
		return BigDecimal.ONE.divide(BigDecimal.valueOf(quantidadeUnidades), 6, RoundingMode.HALF_UP);
	}

}
