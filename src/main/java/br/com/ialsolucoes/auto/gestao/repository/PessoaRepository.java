package br.com.ialsolucoes.auto.gestao.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ialsolucoes.auto.gestao.domain.Pessoa;

@Configuration
@EnableAutoConfiguration
@ComponentScan("br.com.ialsolucoes.gestao.repository")
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
 
	List<Pessoa> findByCondominioIdAndEnvioTaxaEmail(Long condominioId, Boolean envioEmail);
	
	List<Pessoa> findByCondominioId(Long condominioId);
}
