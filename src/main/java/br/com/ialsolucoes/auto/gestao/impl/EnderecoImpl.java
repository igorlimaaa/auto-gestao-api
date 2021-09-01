package br.com.ialsolucoes.auto.gestao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.domain.Endereco;
import br.com.ialsolucoes.auto.gestao.dto.EnderecoDto;
import br.com.ialsolucoes.auto.gestao.mapper.EnderecoMapper;
import br.com.ialsolucoes.auto.gestao.repository.EnderecoRepository;
import br.com.ialsolucoes.auto.gestao.service.EnderecoService;

@Service
public class EnderecoImpl implements EnderecoService{
	
	@Autowired
	private EnderecoMapper mapper;
	
	@Autowired
	private EnderecoRepository enderecoRepository;

	@Override
	public EnderecoDto createNewEndereco(EnderecoDto enderecoDto) {
		Endereco enderecoDomain = mapper.enderecoDtoToDomain(enderecoDto);
		enderecoDomain = enderecoRepository.save(enderecoDomain);
		return mapper.enderecoDomainToDto(enderecoDomain);
	}

	@Override
	public List<EnderecoDto> getEnderecos() {
		Optional<List<Endereco>> end = Optional.of(enderecoRepository.findAll());
		if(end.isPresent()) {
			return mapper.listEnderecoDomainToDto(end.get());
		}
		return null;
	}

	
}
