package br.com.gestaocondial.autogestao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import br.com.gestaocondial.autogestao.config.AlcanceDoPerfil;
import br.com.gestaocondial.autogestao.config.AlcanceDoPerfil.Alcance;
import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.domain.AreaComum;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Evento;
import br.com.gestaocondial.autogestao.domain.Pessoa;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.dto.EventoDto;
import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;
import br.com.gestaocondial.autogestao.enumeration.TipoEventoEnum;
import br.com.gestaocondial.autogestao.exception.AcessoNegadoException;
import br.com.gestaocondial.autogestao.exception.AreaComumNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.CondominioNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.ConflitoDeAgendaException;
import br.com.gestaocondial.autogestao.exception.EventoNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.SituacaoDoEventoInvalidaException;
import br.com.gestaocondial.autogestao.mapper.EventoMapper;
import br.com.gestaocondial.autogestao.repository.AreaComumRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EventoRepository;
import br.com.gestaocondial.autogestao.repository.PessoaRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.repository.specification.EventoSpecifications;
import br.com.gestaocondial.autogestao.service.EventoService;

import lombok.RequiredArgsConstructor;

/**
 * Agenda do condomínio. Regras que valem conhecer:
 *
 * <ul>
 * <li><b>Quem cadastra:</b> todo perfil. Morador ({@link Alcance#apenasProprio()}) gera evento
 * {@code PENDENTE}; os demais, {@code APROVADO}.</li>
 * <li><b>Quem decide:</b> quem tem {@link Alcance#aprova()} (síndico e administrador).</li>
 * <li><b>Quem vê:</b> todos veem os aprovados; só o autor e quem aprova veem pendentes, recusados e
 * cancelados — e só eles veem quem pediu.</li>
 * <li><b>Área ocupada:</b> reserva e manutenção de uma área não se sobrepõem a outra aprovada.</li>
 * </ul>
 */
@Service
@Validated
@RequiredArgsConstructor
public class EventoImpl implements EventoService {

	private final EventoRepository eventoRepository;

	private final AreaComumRepository areaRepository;

	private final CondominioRepository condominioRepository;

	private final UnidadeRepository unidadeRepository;

	private final PessoaRepository pessoaRepository;

	private final EventoMapper mapper;

	private final AlcanceDoPerfil alcanceDoPerfil;

	private final EscopoDoCondominio escopo;

	@Override
	@Transactional(readOnly = true)
	public List<EventoDto> listEventos(Long idCondominio, LocalDate inicio, LocalDate fim,
			SituacaoEventoEnum situacao) {
		Specification<Evento> filtro = Specification.allOf(
				EventoSpecifications.doCondominio(escopo.restringir(idCondominio)),
				EventoSpecifications.noPeriodo(inicio == null ? null : inicio.atStartOfDay(),
						fim == null ? null : fim.atStartOfDay()),
				EventoSpecifications.comSituacao(situacao));

		Alcance alcance = alcanceDoPerfil.alcance();
		return eventoRepository.findAll(filtro, Sort.by("inicio").ascending()).stream()
				.filter(evento -> visivel(evento, alcance))
				.map(evento -> paraDto(evento, alcance))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public EventoDto findEvento(Long idEvento) {
		Evento evento = porId(idEvento);
		Alcance alcance = alcanceDoPerfil.alcance();
		escopo.exigirAcesso(evento.getCondominio().getId());
		if (!visivel(evento, alcance)) {
			// Para quem não pode ver, o evento não existe — não confirmar que há um pedido alheio.
			throw new EventoNaoEncontradoException("Evento " + idEvento + " não encontrado.");
		}
		return paraDto(evento, alcance);
	}

	@Override
	@Transactional
	public EventoDto createEvento(EventoDto dto) {
		Alcance alcance = alcanceDoPerfil.alcance();
		Condominio condominio = condominioDe(dto.getIdCondominio());
		AreaComum area = areaDe(dto.getIdAreaComum(), condominio);

		Evento evento = new Evento();
		evento.setCondominio(condominio);
		aplicar(evento, dto, area);
		validar(evento, null);

		if (alcance.apenasProprio() && evento.getTipo() == TipoEventoEnum.RESERVA_AREA && alcance.unidades().isEmpty()) {
			throw new IllegalArgumentException(
					"Seu cadastro não está vinculado a uma unidade. Peça ao síndico para vinculá-lo antes de reservar.");
		}

		evento.setSituacao(alcance.apenasProprio() ? SituacaoEventoEnum.PENDENTE : SituacaoEventoEnum.APROVADO);
		identificarSolicitante(evento, alcance);
		return paraDto(eventoRepository.save(evento), alcance);
	}

	@Override
	@Transactional
	public EventoDto updateEvento(Long idEvento, EventoDto dto) {
		Alcance alcance = alcanceDoPerfil.alcance();
		Evento evento = porId(idEvento);
		escopo.exigirAcesso(evento.getCondominio().getId());
		if (!podeEditar(evento, alcance)) {
			throw new AcessoNegadoException("Você não pode alterar este evento.");
		}

		aplicar(evento, dto, areaDe(dto.getIdAreaComum(), evento.getCondominio()));
		validar(evento, idEvento);
		return paraDto(eventoRepository.save(evento), alcance);
	}

	@Override
	@Transactional
	public EventoDto decidirEvento(Long idEvento, boolean aprovar, String motivo) {
		Alcance alcance = alcanceDoPerfil.alcance();
		Evento evento = porId(idEvento);
		escopo.exigirAcesso(evento.getCondominio().getId());
		if (!alcance.aprova()) {
			throw new AcessoNegadoException("Só o síndico ou o administrador decide sobre eventos.");
		}
		if (evento.getSituacao() != SituacaoEventoEnum.PENDENTE) {
			throw new SituacaoDoEventoInvalidaException("Este evento não está pendente de aprovação.");
		}

		String motivoLimpo = motivo == null || motivo.isBlank() ? null : motivo.trim();
		if (aprovar) {
			// Outro evento pode ter sido aprovado na mesma área desde que este foi pedido.
			exigirAreaLivre(evento, idEvento);
			evento.setSituacao(SituacaoEventoEnum.APROVADO);
		} else {
			if (motivoLimpo == null) {
				throw new IllegalArgumentException("Informe o motivo da recusa.");
			}
			evento.setSituacao(SituacaoEventoEnum.RECUSADO);
		}
		evento.setMotivoDecisao(motivoLimpo);
		evento.setDataDecisao(LocalDateTime.now());
		evento.setDecididoPor(alcance.email());
		return paraDto(eventoRepository.save(evento), alcance);
	}

	@Override
	@Transactional
	public EventoDto cancelarEvento(Long idEvento) {
		Alcance alcance = alcanceDoPerfil.alcance();
		Evento evento = porId(idEvento);
		escopo.exigirAcesso(evento.getCondominio().getId());
		if (!alcance.aprova() && !ehAutor(evento, alcance)) {
			throw new AcessoNegadoException("Você só pode cancelar os eventos que cadastrou.");
		}
		if (!emAndamentoOuAprovado(evento)) {
			throw new SituacaoDoEventoInvalidaException("Este evento já está " + rotulo(evento.getSituacao()) + ".");
		}

		evento.setSituacao(SituacaoEventoEnum.CANCELADO);
		return paraDto(eventoRepository.save(evento), alcance);
	}

	// ------------------------------------------------------------------ regras

	private void aplicar(Evento evento, EventoDto dto, AreaComum area) {
		evento.setAreaComum(area);
		evento.setTipo(dto.getTipo());
		evento.setTitulo(dto.getTitulo().trim());
		evento.setDescricao(dto.getDescricao() == null || dto.getDescricao().isBlank() ? null : dto.getDescricao().trim());
		evento.setInicio(dto.getInicio());
		evento.setFim(dto.getFim());
		evento.setDiaInteiro(Boolean.TRUE.equals(dto.getDiaInteiro()));
	}

	/** Coerência do evento e, se ocupa uma área, se ela está livre. */
	private void validar(Evento evento, Long idIgnorado) {
		if (!evento.getFim().isAfter(evento.getInicio())) {
			throw new IllegalArgumentException("O fim do evento precisa ser depois do início.");
		}

		AreaComum area = evento.getAreaComum();
		if (evento.getTipo() == TipoEventoEnum.RESERVA_AREA) {
			validarReserva(evento, area);
		}
		if (area != null && evento.getTipo().ocupaArea()) {
			exigirAreaLivre(evento, idIgnorado);
		}
	}

	private void validarReserva(Evento evento, AreaComum area) {
		if (area == null) {
			throw new IllegalArgumentException("Escolha a área que deseja reservar.");
		}
		if (!Boolean.TRUE.equals(area.getReservavel())) {
			throw new IllegalArgumentException("A área \"" + area.getNome() + "\" não aceita reservas.");
		}
		if (Boolean.TRUE.equals(evento.getDiaInteiro())) {
			throw new IllegalArgumentException("Reserva de área precisa de horário de início e de fim.");
		}
		if (evento.getInicio().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("A reserva precisa ser para uma data e hora futuras.");
		}

		boolean mesmoDia = evento.getInicio().toLocalDate().equals(evento.getFim().toLocalDate())
				|| evento.getFim().equals(evento.getInicio().toLocalDate().plusDays(1).atStartOfDay());
		if (!mesmoDia) {
			throw new IllegalArgumentException("A reserva precisa começar e terminar no mesmo dia.");
		}

		if (area.getHoraAbertura() != null && area.getHoraFechamento() != null) {
			LocalTime inicio = evento.getInicio().toLocalTime();
			LocalTime fim = evento.getFim().toLocalTime();
			boolean viraODia = fim.equals(LocalTime.MIDNIGHT);
			if (inicio.isBefore(area.getHoraAbertura()) || viraODia || fim.isAfter(area.getHoraFechamento())) {
				throw new IllegalArgumentException("A área \"" + area.getNome() + "\" só pode ser reservada entre "
						+ area.getHoraAbertura() + " e " + area.getHoraFechamento() + ".");
			}
		}
	}

	private void exigirAreaLivre(Evento evento, Long idIgnorado) {
		AreaComum area = evento.getAreaComum();
		if (area == null || !evento.getTipo().ocupaArea()) {
			return;
		}
		boolean ocupada = eventoRepository.existeConflito(area.getId(), SituacaoEventoEnum.APROVADO,
				List.of(TipoEventoEnum.RESERVA_AREA, TipoEventoEnum.MANUTENCAO), evento.getInicio(), evento.getFim(),
				idIgnorado == null ? -1L : idIgnorado);
		if (ocupada) {
			throw new ConflitoDeAgendaException("A área \"" + area.getNome() + "\" já está ocupada nesse horário.");
		}
	}

	private void identificarSolicitante(Evento evento, Alcance alcance) {
		evento.setSolicitanteEmail(alcance.email());
		Pessoa cadastro = cadastroDe(alcance.email(), evento.getCondominio().getId());
		evento.setSolicitanteNome(cadastro != null && cadastro.getNomeCompleto() != null
				? cadastro.getNomeCompleto()
				: alcance.email());
		// A unidade só é ligada ao pedido de quem é morador; síndico e demais agem pelo condomínio.
		if (alcance.apenasProprio() && !alcance.unidades().isEmpty()) {
			Long idUnidade = alcance.unidades().iterator().next();
			Unidade unidade = unidadeRepository.findById(idUnidade).orElse(null);
			evento.setUnidade(unidade);
		}
	}

	private Pessoa cadastroDe(String email, Long idCondominio) {
		if (email == null) {
			return null;
		}
		return pessoaRepository.findByEmailIgnoreCaseAndCondominioId(email, idCondominio).stream().findFirst()
				.orElse(null);
	}

	// ------------------------------------------------------------------ visibilidade e permissões

	private boolean visivel(Evento evento, Alcance alcance) {
		return alcance.aprova() || evento.getSituacao() == SituacaoEventoEnum.APROVADO || ehAutor(evento, alcance);
	}

	private boolean ehAutor(Evento evento, Alcance alcance) {
		return alcance.email() != null && alcance.email().equalsIgnoreCase(evento.getSolicitanteEmail());
	}

	private boolean emAndamentoOuAprovado(Evento evento) {
		return evento.getSituacao() == SituacaoEventoEnum.PENDENTE
				|| evento.getSituacao() == SituacaoEventoEnum.APROVADO;
	}

	private boolean podeEditar(Evento evento, Alcance alcance) {
		if (!emAndamentoOuAprovado(evento)) {
			return false;
		}
		return alcance.aprova() || (ehAutor(evento, alcance) && evento.getSituacao() == SituacaoEventoEnum.PENDENTE);
	}

	private EventoDto paraDto(Evento evento, Alcance alcance) {
		EventoDto dto = mapper.toDto(evento);
		boolean autor = ehAutor(evento, alcance);
		dto.setEhMeu(autor);
		dto.setPodeEditar(podeEditar(evento, alcance));
		dto.setPodeCancelar(emAndamentoOuAprovado(evento) && (alcance.aprova() || autor));
		dto.setPodeDecidir(alcance.aprova() && evento.getSituacao() == SituacaoEventoEnum.PENDENTE);

		// Quem pediu só é revelado ao próprio e a quem aprova; a decisão (e o motivo) também.
		if (!alcance.aprova() && !autor) {
			dto.setSolicitanteNome(null);
			dto.setSolicitanteEmail(null);
			dto.setMotivoDaDecisao(null);
		}
		return dto;
	}

	private static String rotulo(SituacaoEventoEnum situacao) {
		return switch (situacao) {
			case PENDENTE -> "pendente";
			case APROVADO -> "aprovado";
			case RECUSADO -> "recusado";
			case CANCELADO -> "cancelado";
		};
	}

	// ------------------------------------------------------------------ busca

	private Condominio condominioDe(Long idPedido) {
		Long id = idPedido != null ? idPedido : escopo.doPerfilAtivo();
		if (id == null) {
			throw new IllegalArgumentException("Informe o condomínio do evento.");
		}
		escopo.exigirAcesso(id);
		return condominioRepository.findById(id)
				.orElseThrow(() -> new CondominioNaoEncontradoException("Condomínio " + id + " não encontrado."));
	}

	private AreaComum areaDe(Long idArea, Condominio condominio) {
		if (idArea == null) {
			return null;
		}
		AreaComum area = areaRepository.findById(idArea)
				.orElseThrow(() -> new AreaComumNaoEncontradaException("Área " + idArea + " não encontrada."));
		if (!Objects.equals(area.getCondominio().getId(), condominio.getId())) {
			throw new IllegalArgumentException("A área escolhida pertence a outro condomínio.");
		}
		if (!Boolean.TRUE.equals(area.getAtiva())) {
			throw new IllegalArgumentException("A área \"" + area.getNome() + "\" está desativada.");
		}
		return area;
	}

	private Evento porId(Long idEvento) {
		return eventoRepository.findById(idEvento)
				.orElseThrow(() -> new EventoNaoEncontradoException("Evento " + idEvento + " não encontrado."));
	}

}
