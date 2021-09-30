package br.com.ialsolucoes.auto.gestao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.dto.MeioContatoDto;
import br.com.ialsolucoes.auto.gestao.dto.PessoaDto;

@Service
public interface PessoaService {
	
	public PessoaDto createNewPessoa (PessoaDto pessoa);
	
	public List<PessoaDto> findPessoasEnvioEmail(Long idCondominio);
	
	public MeioContatoDto createMeioContato(MeioContatoDto meioContato);
	
	public List<PessoaDto> findPessoas();
	
	public PessoaDto findPessoaId(Long idPessoa);
	
	public List<PessoaDto> findPessoasCondominioId(Long idCondominio);

}
