package br.com.gestaocondial.autogestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.gestaocondial.autogestao.domain.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {

	List<Pessoa> findByCondominioIdAndEnvioTaxaEmail(Long condominioId, Boolean envioEmail);

	List<Pessoa> findByCondominioId(Long condominioId);

	long countByCondominioId(Long condominioId);

	long countByUnidadeId(Long unidadeId);
}
