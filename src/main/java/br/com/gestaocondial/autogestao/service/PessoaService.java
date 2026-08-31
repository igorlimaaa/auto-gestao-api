package br.com.gestaocondial.autogestao.service;

import java.util.List;

import jakarta.validation.Valid;

import br.com.gestaocondial.autogestao.dto.MeioContatoDto;
import br.com.gestaocondial.autogestao.dto.PessoaDto;

public interface PessoaService {

	public PessoaDto createNewPessoa(@Valid PessoaDto pessoa);

	public List<PessoaDto> findPessoasEnvioEmail(Long idCondominio);

	public MeioContatoDto createMeioContato(MeioContatoDto meioContato);

	public List<PessoaDto> findPessoas();

	public PessoaDto findPessoaId(Long idPessoa);

	public List<PessoaDto> findPessoasCondominioId(Long idCondominio);

	public List<PessoaDto> findPessoas(Long idCondominio, Long idUnidade, Boolean semUnidade, String busca);

	public PessoaDto updatePessoa(Long idPessoa, @Valid PessoaDto pessoa);

	public void deletePessoa(Long idPessoa);

}
