package br.com.gestaocondial.autogestao.repository;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gestaocondial.autogestao.domain.Evento;
import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;
import br.com.gestaocondial.autogestao.enumeration.TipoEventoEnum;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

	boolean existsByAreaComumId(Long idAreaComum);

	/**
	 * Há outro evento do mesmo tipo-ocupante, nessa situação, na mesma área, cruzando o intervalo?
	 * Intervalos são semiabertos: um que termina às 14h não conflita com outro que começa às 14h.
	 */
	@Query("""
			select count(e) > 0 from Evento e
			where e.areaComum.id = :idArea
			  and e.situacao = :situacao
			  and e.tipo in :tipos
			  and e.inicio < :fim and e.fim > :inicio
			  and e.id <> :idIgnorado
			""")
	boolean existeConflito(@Param("idArea") Long idArea, @Param("situacao") SituacaoEventoEnum situacao,
			@Param("tipos") Collection<TipoEventoEnum> tipos, @Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim, @Param("idIgnorado") Long idIgnorado);

}
