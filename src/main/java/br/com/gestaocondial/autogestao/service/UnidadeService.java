package br.com.gestaocondial.autogestao.service;

import java.util.List;

import br.com.gestaocondial.autogestao.dto.UnidadeDto;

import jakarta.validation.Valid;

public interface UnidadeService {

	List<UnidadeDto> listUnidades(Long idCondominio, Boolean ativa, String busca);

	UnidadeDto findUnidade(Long idUnidade);

	UnidadeDto createUnidade(@Valid UnidadeDto unidade);

	UnidadeDto updateUnidade(Long idUnidade, @Valid UnidadeDto unidade);

	void deleteUnidade(Long idUnidade);

}
