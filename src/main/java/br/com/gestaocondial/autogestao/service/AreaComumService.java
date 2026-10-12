package br.com.gestaocondial.autogestao.service;

import java.util.List;

import br.com.gestaocondial.autogestao.dto.AreaComumDto;

import jakarta.validation.Valid;

public interface AreaComumService {

	List<AreaComumDto> listAreas(Long idCondominio, Boolean ativa, Boolean reservavel);

	AreaComumDto createArea(@Valid AreaComumDto area);

	AreaComumDto updateArea(Long idArea, @Valid AreaComumDto area);

	void deleteArea(Long idArea);

}
