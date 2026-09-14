package br.com.gestaocondial.autogestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gestaocondial.autogestao.domain.Condominio;

public interface CondominioRepository extends JpaRepository<Condominio, Long> {

	/** Usado pelo job de geração de cobranças: ignora condomínios sem dia de vencimento cadastrado. */
	List<Condominio> findByDiaVencimentoTaxaOrdinariaIsNotNull();

}
