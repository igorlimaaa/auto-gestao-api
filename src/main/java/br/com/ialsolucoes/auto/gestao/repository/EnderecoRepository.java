package br.com.ialsolucoes.auto.gestao.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ialsolucoes.auto.gestao.domain.Endereco;

@Configuration
@EnableAutoConfiguration
@ComponentScan("br.com.ialsolucoes.gestao.repository")
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

}
