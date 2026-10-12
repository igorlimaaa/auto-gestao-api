package br.com.gestaocondial.autogestao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.gestaocondial.autogestao.domain.AreaComum;

public interface AreaComumRepository extends JpaRepository<AreaComum, Long>, JpaSpecificationExecutor<AreaComum> {

	boolean existsByCondominioIdAndNomeIgnoreCase(Long idCondominio, String nome);

	boolean existsByCondominioIdAndNomeIgnoreCaseAndIdNot(Long idCondominio, String nome, Long id);

}
