package br.com.gestaocondial.autogestao.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.EventoApi;
import br.com.gestaocondial.autogestao.api.model.DecisaoDeEvento;
import br.com.gestaocondial.autogestao.api.model.Evento;
import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;
import br.com.gestaocondial.autogestao.mapper.EventoApiMapper;
import br.com.gestaocondial.autogestao.service.EventoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EventoController implements EventoApi {

	private final EventoService eventoService;

	private final EventoApiMapper apiMapper;

	@Override
	@PreAuthorize("hasAuthority('EVENTO_LER')")
	public ResponseEntity<List<Evento>> listEventos(Long idCondominio, LocalDate inicio, LocalDate fim,
			String situacao) {
		SituacaoEventoEnum filtro = situacao == null || situacao.isBlank() ? null
				: SituacaoEventoEnum.valueOf(situacao);
		return ResponseEntity.ok(apiMapper.toApiList(eventoService.listEventos(idCondominio, inicio, fim, filtro)));
	}

	@Override
	@PreAuthorize("hasAuthority('EVENTO_LER')")
	public ResponseEntity<Evento> findEvento(Long idEvento) {
		return ResponseEntity.ok(apiMapper.toApi(eventoService.findEvento(idEvento)));
	}

	@Override
	@PreAuthorize("hasAuthority('EVENTO_ESCREVER')")
	public ResponseEntity<Evento> createEvento(Evento evento) {
		var criado = eventoService.createEvento(apiMapper.toDto(evento));
		return new ResponseEntity<>(apiMapper.toApi(criado), HttpStatus.CREATED);
	}

	@Override
	@PreAuthorize("hasAuthority('EVENTO_ESCREVER')")
	public ResponseEntity<Evento> updateEvento(Long idEvento, Evento evento) {
		return ResponseEntity.ok(apiMapper.toApi(eventoService.updateEvento(idEvento, apiMapper.toDto(evento))));
	}

	@Override
	@PreAuthorize("hasAuthority('EVENTO_APROVAR')")
	public ResponseEntity<Evento> decidirEvento(Long idEvento, DecisaoDeEvento decisao) {
		return ResponseEntity.ok(apiMapper.toApi(
				eventoService.decidirEvento(idEvento, Boolean.TRUE.equals(decisao.getAprovar()), decisao.getMotivo())));
	}

	@Override
	@PreAuthorize("hasAuthority('EVENTO_ESCREVER')")
	public ResponseEntity<Evento> cancelarEvento(Long idEvento) {
		return ResponseEntity.ok(apiMapper.toApi(eventoService.cancelarEvento(idEvento)));
	}

}
