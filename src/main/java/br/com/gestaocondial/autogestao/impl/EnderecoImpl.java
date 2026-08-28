package br.com.gestaocondial.autogestao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.dto.EnderecoDto;
import br.com.gestaocondial.autogestao.mapper.EnderecoMapper;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.service.EnderecoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnderecoImpl implements EnderecoService {

	private final EnderecoMapper mapper;

	private final EnderecoRepository enderecoRepository;

	@Override
	@Transactional
	public EnderecoDto createNewEndereco(EnderecoDto enderecoDto) {
		Endereco enderecoDomain = mapper.enderecoDtoToDomain(enderecoDto);
		enderecoDomain = enderecoRepository.save(enderecoDomain);
		return mapper.enderecoDomainToDto(enderecoDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EnderecoDto> getEnderecos() {
		Optional<List<Endereco>> end = Optional.of(enderecoRepository.findAll());
		if (end.isPresent()) {
			return mapper.listEnderecoDomainToDto(end.get());
		}
		return null;
	}

}
