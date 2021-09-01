package br.com.ialsolucoes.auto.gestao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.domain.MeioContato;
import br.com.ialsolucoes.auto.gestao.domain.Pessoa;
import br.com.ialsolucoes.auto.gestao.dto.MeioContatoDto;
import br.com.ialsolucoes.auto.gestao.dto.PessoaDto;
import br.com.ialsolucoes.auto.gestao.mapper.PessoaMapper;
import br.com.ialsolucoes.auto.gestao.repository.MeioContatoRepository;
import br.com.ialsolucoes.auto.gestao.repository.PessoaRepository;
import br.com.ialsolucoes.auto.gestao.service.PessoaService;

@Service
public class PessoaImpl implements PessoaService{

	@Autowired
	private PessoaMapper mapper;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private MeioContatoRepository meioContatoRepository;
	
	@Override
	public PessoaDto createNewPessoa(PessoaDto pessoa) {
		Pessoa pessoaDomain = mapper.pessoaDtoToDomain(pessoa);
		pessoaDomain = pessoaRepository.save(pessoaDomain);
		return mapper.pessoaDomainToDto(pessoaDomain);
	}

	@Override
	public List<PessoaDto> findPessoasEnvioEmail(Long idCondominio) {
		List<Pessoa> listPessoa = pessoaRepository.findByCondominioIdAndEnvioTaxaEmail(idCondominio, true);
		return mapper.listPessoaDomainToDto(listPessoa);
	}

	@Override
	public MeioContatoDto createMeioContato(MeioContatoDto meioContato) {
		MeioContato meioDomain = mapper.meioContatoDtoToDomain(meioContato);
		meioDomain = meioContatoRepository.save(meioDomain);
		return mapper.meioContatoDomainToDto(meioDomain);
	}

}
