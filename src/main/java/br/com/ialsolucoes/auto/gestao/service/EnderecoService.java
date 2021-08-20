package br.com.ialsolucoes.auto.gestao.service;

import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.dto.EnderecoDto;

@Service
public interface EnderecoService {

	public EnderecoDto createNewEndereco(EnderecoDto enderecoDto);
}
