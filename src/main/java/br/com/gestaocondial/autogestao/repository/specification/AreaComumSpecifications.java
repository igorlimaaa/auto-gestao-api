package br.com.gestaocondial.autogestao.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import br.com.gestaocondial.autogestao.domain.AreaComum;

public final class AreaComumSpecifications {

	private AreaComumSpecifications() {
	}

	public static Specification<AreaComum> doCondominio(Long idCondominio) {
		if (idCondominio == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("condominio").get("id"), idCondominio);
	}

	public static Specification<AreaComum> comAtiva(Boolean ativa) {
		if (ativa == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("ativa"), ativa);
	}

	public static Specification<AreaComum> comReservavel(Boolean reservavel) {
		if (reservavel == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("reservavel"), reservavel);
	}

}
