package br.com.ialsolucoes.auto.gestao.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.ialsolucoes.auto.gestao.domain.Condominio;
import br.com.ialsolucoes.auto.gestao.domain.Endereco;
import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;
import br.com.ialsolucoes.auto.gestao.dto.EnderecoDto;

class CondominioMapperTest {

	private final Integer DDD = 81;
	private final Long TELEFONE = 99999999L;
	private final Double VALORTAXA = 550.20;
	private final Long ID_ENDERECO = 1L;
	private final Double VALORJUROS = 1.0;
	private final Double VALORMULTA = 2.0;
	private final Boolean POSSUITAXA = false;
	
	CondominioDto createCondominioDto(Long id) {
		EnderecoDto end = new EnderecoDto();
		end.setId(ID_ENDERECO);
		CondominioDto cond = new CondominioDto(id, DDD, TELEFONE, end, VALORJUROS, VALORMULTA, POSSUITAXA, VALORTAXA, null, null);
		return cond;
	}
	
	Condominio createCondominioDomain(Long id) {
		Endereco end = new Endereco();
		end.setId(ID_ENDERECO);
		Condominio cond = new Condominio(id, DDD.longValue(), TELEFONE, VALORTAXA, end, VALORJUROS, VALORMULTA, POSSUITAXA);
		return cond;
	}
	
	@Test
	void mapperDtoToDomain() {
		CondominioDto cond = createCondominioDto(1L);
		Condominio condDomain = new CondominioMapperImpl().condominioDtoToDomain(cond);
		
		assertEquals(DDD, condDomain.getDdd().intValue());
		assertEquals(TELEFONE, condDomain.getNumeroTelefone());
		assertEquals(VALORTAXA, condDomain.getValorTaxaCondominial());
		assertEquals(ID_ENDERECO, condDomain.getEndereco().getId());
	}
	
	@Test
	void mapperListDtoToDomain() {
		List<CondominioDto> listDto = new ArrayList<CondominioDto>();
		listDto.add(createCondominioDto(2L));
		listDto.add(createCondominioDto(3L));
		List<Condominio> listDomain = new CondominioMapperImpl().listCondominioDtoToListDomain(listDto);
		
		assertEquals(2, listDomain.size());
		Condominio condDomain = listDomain.get(0);
		assertEquals(DDD, condDomain.getDdd().intValue());
		assertEquals(TELEFONE, condDomain.getNumeroTelefone());
		assertEquals(VALORTAXA, condDomain.getValorTaxaCondominial());
		assertEquals(ID_ENDERECO, condDomain.getEndereco().getId());
	}
	
	@Test
	void mapperDomainToDto() {
		Condominio condDomain = createCondominioDomain(1L);
		CondominioDto cond = new CondominioMapperImpl().condominioDomainToDto(condDomain);
		
		assertEquals(DDD, cond.getDdd());
		assertEquals(TELEFONE, cond.getNumeroTelefone());
		assertEquals(VALORTAXA, cond.getValorTaxaCondominial());
		assertEquals(ID_ENDERECO, cond.getEndereco().getId());
	}
	
	@Test
	void mapperListDomainToDto() {
		List<Condominio> listCondDomain = new ArrayList<Condominio>();
		listCondDomain.add(createCondominioDomain(2L));
		listCondDomain.add(createCondominioDomain(3L));
		List<CondominioDto> listDto = new CondominioMapperImpl().listCondominioDomainToDto(listCondDomain);
		
		assertEquals(2, listDto.size());
		CondominioDto cond = listDto.get(0);
		assertEquals(DDD, cond.getDdd());
		assertEquals(TELEFONE, cond.getNumeroTelefone());
		assertEquals(VALORTAXA, cond.getValorTaxaCondominial());
		assertEquals(ID_ENDERECO, cond.getEndereco().getId());
	}
	

}
