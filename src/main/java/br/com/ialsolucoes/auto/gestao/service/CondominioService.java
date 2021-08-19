package br.com.ialsolucoes.auto.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;

@Service
public interface CondominioService {
	
	public List<CondominioDto> listCondominios();
	
	public CondominioDto createNewCondominio(CondominioDto condominioDTo);

}
