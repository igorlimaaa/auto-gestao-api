package br.com.gestaocondial.autogestao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.gestaocondial.autogestao.domain.Unidade;

public interface UnidadeRepository extends JpaRepository<Unidade, Long>, JpaSpecificationExecutor<Unidade> {

	List<Unidade> findByCondominioId(Long idCondominio);

	long countByCondominioId(Long idCondominio);

	/**
	 * Espelha o índice único de {@code tb_unidade}: não existem duas unidades com o mesmo
	 * bloco e número no mesmo condomínio. Bloco vazio (e não nulo) representa "sem bloco". Conferir aqui devolve 409 com mensagem, em vez de um
	 * 500 vindo da violação de constraint.
	 */
	Optional<Unidade> findByCondominioIdAndBlocoAndNumero(Long idCondominio, String bloco, String numero);

}
