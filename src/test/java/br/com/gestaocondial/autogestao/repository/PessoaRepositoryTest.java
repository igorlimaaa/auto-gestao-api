package br.com.gestaocondial.autogestao.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.gestaocondial.autogestao.domain.Pessoa;

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
