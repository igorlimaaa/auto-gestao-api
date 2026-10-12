package br.com.gestaocondial.autogestao.repository.specification;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import br.com.gestaocondial.autogestao.domain.Evento;
import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;

/** Filtros da agenda. Filtro ausente devolve {@link Specification#unrestricted()}. */
public final class EventoSpecifications {

	private EventoSpecifications() {
	}

	public static Specification<Evento> doCondominio(Long idCondominio) {
		if (idCondominio == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("condominio").get("id"), idCondominio);
	}

	/** Eventos que tocam o intervalo [inicio, fim): começam antes do fim e terminam depois do início. */
	public static Specification<Evento> noPeriodo(LocalDateTime inicio, LocalDateTime fim) {
		Specification<Evento> comeca = fim == null ? Specification.unrestricted()
				: (raiz, consulta, construtor) -> construtor.lessThan(raiz.get("inicio"), fim);
		Specification<Evento> termina = inicio == null ? Specification.unrestricted()
				: (raiz, consulta, construtor) -> construtor.greaterThan(raiz.get("fim"), inicio);
		return Specification.allOf(comeca, termina);
	}

	public static Specification<Evento> comSituacao(SituacaoEventoEnum situacao) {
		if (situacao == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("situacao"), situacao);
	}

}
