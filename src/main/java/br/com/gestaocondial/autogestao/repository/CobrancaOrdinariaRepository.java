package br.com.gestaocondial.autogestao.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;

public interface CobrancaOrdinariaRepository
		extends JpaRepository<CobrancaOrdinaria, Long>, JpaSpecificationExecutor<CobrancaOrdinaria> {

	boolean existsByUnidadeIdAndCompetencia(Long idUnidade, LocalDate competencia);

	/** Usado pelo job para saber se um condomínio já teve alguma cobrança gerada (bootstrap). */
	boolean existsByUnidadeCondominioId(Long idCondominio);

}
