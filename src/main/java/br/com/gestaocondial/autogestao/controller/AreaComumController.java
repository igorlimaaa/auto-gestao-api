package br.com.gestaocondial.autogestao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.AreaComumApi;
import br.com.gestaocondial.autogestao.api.model.AreaComum;
import br.com.gestaocondial.autogestao.mapper.AreaComumApiMapper;
import br.com.gestaocondial.autogestao.service.AreaComumService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AreaComumController implements AreaComumApi {

	private final AreaComumService areaService;

	private final AreaComumApiMapper apiMapper;

	@Override
	@PreAuthorize("hasAuthority('AREA_LER')")
	public ResponseEntity<List<AreaComum>> listAreasComuns(Long idCondominio, Boolean ativa, Boolean reservavel) {
		return ResponseEntity.ok(apiMapper.toApiList(areaService.listAreas(idCondominio, ativa, reservavel)));
	}

	@Override
	@PreAuthorize("hasAuthority('AREA_ESCREVER')")
	public ResponseEntity<AreaComum> createAreaComum(AreaComum areaComum) {
		var criada = areaService.createArea(apiMapper.toDto(areaComum));
		return new ResponseEntity<>(apiMapper.toApi(criada), HttpStatus.CREATED);
	}

	@Override
	@PreAuthorize("hasAuthority('AREA_ESCREVER')")
	public ResponseEntity<AreaComum> updateAreaComum(Long idAreaComum, AreaComum areaComum) {
		return ResponseEntity.ok(apiMapper.toApi(areaService.updateArea(idAreaComum, apiMapper.toDto(areaComum))));
	}

	@Override
	@PreAuthorize("hasAuthority('AREA_ESCREVER')")
	public ResponseEntity<Void> deleteAreaComum(Long idAreaComum) {
		areaService.deleteArea(idAreaComum);
		return ResponseEntity.noContent().build();
	}

}
