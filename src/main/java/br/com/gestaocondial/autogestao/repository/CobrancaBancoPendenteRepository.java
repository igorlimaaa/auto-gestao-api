package br.com.gestaocondial.autogestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gestaocondial.autogestao.domain.CobrancaBancoPendente;

public interface CobrancaBancoPendenteRepository extends JpaRepository<CobrancaBancoPendente, Long> {

	List<CobrancaBancoPendente> findByIdCondominioOrderByDataVencimentoAsc(Long idCondominio);

	List<CobrancaBancoPendente> findAllByOrderByDataVencimentoAsc();

	/**
	 * Delete em lote, e não {@code deleteBy...}: o Hibernate executa os INSERTs antes dos DELETEs
	 * no flush, e a constraint única (condomínio, código) estouraria ao regravar o mesmo conjunto.
	 */
	@Modifying
	@Query("delete from CobrancaBancoPendente p where p.idCondominio = :idCondominio")
	void apagarDoCondominio(@Param("idCondominio") Long idCondominio);

}
