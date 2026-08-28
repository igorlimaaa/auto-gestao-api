package br.com.gestaocondial.autogestao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import br.com.gestaocondial.autogestao.domain.MeioContato;
import br.com.gestaocondial.autogestao.domain.Pessoa;
import br.com.gestaocondial.autogestao.dto.MeioContatoDto;
import br.com.gestaocondial.autogestao.dto.PessoaDto;
import br.com.gestaocondial.autogestao.mapper.PessoaMapper;
import br.com.gestaocondial.autogestao.repository.MeioContatoRepository;
import br.com.gestaocondial.autogestao.repository.PessoaRepository;
import br.com.gestaocondial.autogestao.service.PessoaService;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class PessoaImpl implements PessoaService {

	private final PessoaMapper mapper;

	private final PessoaRepository pessoaRepository;

	private final MeioContatoRepository meioContatoRepository;

	@Override
	@Transactional
	public PessoaDto createNewPessoa(PessoaDto pessoa) {
		Pessoa pessoaDomain = mapper.pessoaDtoToDomain(pessoa);
		pessoaDomain = pessoaRepository.save(pessoaDomain);
		return mapper.pessoaDomainToDto(pessoaDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PessoaDto> findPessoasEnvioEmail(Long idCondominio) {
		List<Pessoa> listPessoa = pessoaRepository.findByCondominioIdAndEnvioTaxaEmail(idCondominio, true);
		return mapper.listPessoaDomainToDto(listPessoa);
	}

	@Override
	@Transactional
	public MeioContatoDto createMeioContato(MeioContatoDto meioContato) {
		MeioContato meioDomain = mapper.meioContatoDtoToDomain(meioContato);
		meioDomain = meioContatoRepository.save(meioDomain);
		return mapper.meioContatoDomainToDto(meioDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PessoaDto> findPessoas() {
		List<Pessoa> listPessoa = pessoaRepository.findAll();
		if (!listPessoa.isEmpty()) {
			return mapper.listPessoaDomainToDto(listPessoa);
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public PessoaDto findPessoaId(Long idPessoa) {
		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);
		if (pessoa.isPresent()) {
			return mapper.pessoaDomainToDto(pessoa.get());
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PessoaDto> findPessoasCondominioId(Long idCondominio) {
		List<Pessoa> listPessoa = pessoaRepository.findByCondominioId(idCondominio);
		if (!listPessoa.isEmpty()) {
			return mapper.listPessoaDomainToDto(listPessoa);
		}
		return null;
	}

}
