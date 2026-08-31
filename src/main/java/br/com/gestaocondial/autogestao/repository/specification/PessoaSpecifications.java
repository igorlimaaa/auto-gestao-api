package br.com.gestaocondial.autogestao.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import br.com.gestaocondial.autogestao.domain.Pessoa;

public final class PessoaSpecifications {

	private PessoaSpecifications() {
	}

	public static Specification<Pessoa> doCondominio(Long idCondominio) {
		if (idCondominio == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("condominio").get("id"), idCondominio);
	}

	public static Specification<Pessoa> daUnidade(Long idUnidade) {
		if (idUnidade == null) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.equal(raiz.get("unidade").get("id"), idUnidade);
	}

	/** Moradores ainda não vinculados a nenhuma unidade — o cadastro solto é estado válido. */
	public static Specification<Pessoa> semUnidade(Boolean semUnidade) {
		if (!Boolean.TRUE.equals(semUnidade)) {
			return Specification.unrestricted();
		}
		return (raiz, consulta, construtor) -> construtor.isNull(raiz.get("unidade"));
	}

	public static Specification<Pessoa> comBusca(String busca) {
		if (busca == null || busca.isBlank()) {
			return Specification.unrestricted();
		}
		String padrao = "%" + busca.trim().toLowerCase() + "%";
		return (raiz, consulta, construtor) -> construtor.or(
				construtor.like(construtor.lower(construtor.coalesce(raiz.get("nomeCompleto"), "")), padrao),
				construtor.like(construtor.lower(construtor.coalesce(raiz.get("cpf"), "")), padrao));
	}

}
