package br.com.ialsolucoes.auto.gestao.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.ialsolucoes.auto.gestao.domain.Pessoa;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles(value = "test")
public class PessoaRepositoryTest {

	@Autowired
	private PessoaRepository repository;
	
	@Test
	void findPessoa() {
		Optional<Pessoa> pes = repository.findById(1L);
		if(pes.isPresent()) {
			assertEquals("", "");
		}
		assertEquals("", "");
	}

}
