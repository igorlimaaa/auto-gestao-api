package br.com.gestaocondial.autogestao.service;

import java.time.LocalDate;
import java.util.List;

import br.com.gestaocondial.autogestao.dto.EventoDto;
import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;

import jakarta.validation.Valid;

public interface EventoService {

	List<EventoDto> listEventos(Long idCondominio, LocalDate inicio, LocalDate fim, SituacaoEventoEnum situacao);

	EventoDto findEvento(Long idEvento);

	EventoDto createEvento(@Valid EventoDto evento);

	EventoDto updateEvento(Long idEvento, @Valid EventoDto evento);

	EventoDto decidirEvento(Long idEvento, boolean aprovar, String motivo);

	EventoDto cancelarEvento(Long idEvento);

}
