package br.com.gestaocondial.autogestao.service;

import java.util.List;

import br.com.gestaocondial.autogestao.dto.EnderecoDto;

public interface EnderecoService {

	public EnderecoDto createNewEndereco(EnderecoDto enderecoDto);

	public List<EnderecoDto> getEnderecos();
}
