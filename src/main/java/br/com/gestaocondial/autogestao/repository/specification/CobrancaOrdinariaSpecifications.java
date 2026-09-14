package br.com.gestaocondial.autogestao.repository.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;

/**
 * Filtros da listagem de cobranças. Filtro ausente devolve {@link Specification#unrestricted()},
 * nunca {@code null} — {@code Specification.allOf} rejeita elementos nulos.
 */
public final class CobrancaOrdinariaSpecifications {

	private CobrancaOrdinariaSpecifications() {
	}

	public static Specification<CobrancaOrdinaria> doCondominio(Long idCondominio) {
		if (idCondominio == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(
				raiz.get("unidade").get("condominio").get("id"), idCondominio);
	}

	public static Specification<CobrancaOrdinaria> doUnidade(Long idUnidade) {
		if (idUnidade == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("unidade").get("id"), idUnidade);
	}

	public static Specification<CobrancaOrdinaria> daCompetencia(LocalDate competencia) {
		if (competencia == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("competencia"), competencia);
	}

}
