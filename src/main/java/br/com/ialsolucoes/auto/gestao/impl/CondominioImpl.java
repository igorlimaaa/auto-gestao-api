package br.com.ialsolucoes.auto.gestao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.domain.Condominio;
import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;
import br.com.ialsolucoes.auto.gestao.mapper.CondominioMapper;
import br.com.ialsolucoes.auto.gestao.repository.CondominioRepository;
import br.com.ialsolucoes.auto.gestao.service.CondominioService;

@Service
public class CondominioImpl implements CondominioService {
	
	@Autowired
	private CondominioRepository condominioRepository;
	
	@Autowired
	private CondominioMapper mapper;

	@Override
	public List<CondominioDto> listCondominios() {
		List<Condominio> listCondDomain = condominioRepository.findAll();
		return mapper.listCondominioDomainToDto(listCondDomain);
	}

	@Override
	public CondominioDto createNewCondominio(CondominioDto condominioDTo) {
		Condominio condominoDomain = mapper.condominioDtoToDomain(condominioDTo);
		condominoDomain = condominioRepository.save(condominoDomain);
		return mapper.condominioDomainToDto(condominoDomain);
	}
	
}
