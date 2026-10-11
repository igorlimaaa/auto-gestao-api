package br.com.gestaocondial.autogestao.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;

public interface CobrancaOrdinariaRepository
		extends JpaRepository<CobrancaOrdinaria, Long>, JpaSpecificationExecutor<CobrancaOrdinaria> {

	boolean existsByUnidadeIdAndCompetencia(Long idUnidade, LocalDate competencia);

	Optional<CobrancaOrdinaria> findByCodigoSolicitacao(String codigoSolicitacao);

	Optional<CobrancaOrdinaria> findByUnidadeIdAndCompetencia(Long idUnidade, LocalDate competencia);

	/**
	 * Cobranças que vieram do banco e ainda podem mudar de situação — as que o job horário
	 * reconsulta. Pagas e canceladas/expiradas saem do filtro, então o volume cai sozinho.
	 */
	@Query("""
			select c from CobrancaOrdinaria c
			where c.dataPagamento is null
			  and c.codigoSolicitacao is not null
			  and (c.situacaoBanco is null or c.situacaoBanco not in :situacoesFinais)
			  and (:idCondominio is null or c.unidade.condominio.id = :idCondominio)
			""")
	List<CobrancaOrdinaria> findPendentesDeSincronizacao(@Param("idCondominio") Long idCondominio,
			@Param("situacoesFinais") Collection<String> situacoesFinais);

}
