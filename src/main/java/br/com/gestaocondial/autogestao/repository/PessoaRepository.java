package br.com.gestaocondial.autogestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gestaocondial.autogestao.domain.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

	List<Pessoa> findByCondominioIdAndEnvioTaxaEmail(Long condominioId, Boolean envioEmail);

	List<Pessoa> findByCondominioId(Long condominioId);
}
