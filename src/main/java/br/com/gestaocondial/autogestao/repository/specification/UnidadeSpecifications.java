package br.com.gestaocondial.autogestao.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import br.com.gestaocondial.autogestao.domain.Unidade;

/**
 * Filtros da listagem de unidades. Filtro ausente devolve
 * {@link Specification#unrestricted()}, nunca {@code null} — {@code Specification.allOf}
 * rejeita elementos nulos.
 */
public final class UnidadeSpecifications {

	private UnidadeSpecifications() {
	}

	public static Specification<Unidade> doCondominio(Long idCondominio) {
		if (idCondominio == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("condominio").get("id"), idCondominio);
	}

	public static Specification<Unidade> comAtiva(Boolean ativa) {
		if (ativa == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("ativa"), ativa);
	}

	public static Specification<Unidade> comBusca(String busca) {
		if (busca == null || busca.isBlank()) {
			return Specification.unrestricted();
		}
		String padrao = "%" + busca.trim().toLowerCase() + "%";
		return (raiz, consulta, construtor) -> construtor.or(
				construtor.like(construtor.lower(construtor.coalesce(raiz.get("bloco"), "")), padrao),
				construtor.like(construtor.lower(raiz.get("numero")), padrao));
	}

}
