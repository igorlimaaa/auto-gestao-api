package br.com.ialsolucoes.auto.gestao.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.com.ialsolucoes.auto.gestao.domain.Endereco;
import br.com.ialsolucoes.auto.gestao.dto.EnderecoDto;

class EnderecoMapperTest {
	
	private final Long CEP = 50000000L;
	private final String DESCRICAO = "Teste descricao";
	private final String COMPLEMENTO = "Teste complemento";

	@Test
	void mapperDomainToDto() {
		Endereco end = new Endereco(null, CEP, DESCRICAO, COMPLEMENTO);
		EnderecoDto endDto = new EnderecoMapperImpl().enderecoDomainToDto(end);
		
		assertEquals(CEP, endDto.getCep());
		assertEquals(DESCRICAO, endDto.getEndereco());
		assertEquals(COMPLEMENTO, endDto.getComplemento());
	}

	@Test
	void mapperDtoToDomain() {
		EnderecoDto endDto = new EnderecoDto(null, CEP, DESCRICAO, COMPLEMENTO);
		Endereco end = new EnderecoMapperImpl().enderecoDtoToDomain(endDto);
		
		assertEquals(CEP, end.getCep());
		assertEquals(DESCRICAO, end.getEndereco());
		assertEquals(COMPLEMENTO, end.getComplemento());
	}
}
