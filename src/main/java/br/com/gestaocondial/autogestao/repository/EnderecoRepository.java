package br.com.gestaocondial.autogestao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gestaocondial.autogestao.domain.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

}
