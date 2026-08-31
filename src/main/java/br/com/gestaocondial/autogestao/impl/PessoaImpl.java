package br.com.gestaocondial.autogestao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import br.com.gestaocondial.autogestao.domain.MeioContato;
import br.com.gestaocondial.autogestao.domain.Pessoa;
import org.springframework.data.jpa.domain.Specification;

import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.exception.PessoaNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.UnidadeNaoEncontradaException;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.repository.specification.PessoaSpecifications;

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

	private final UnidadeRepository unidadeRepository;

	@Override
	@Transactional
	public PessoaDto createNewPessoa(PessoaDto pessoa) {
		Pessoa pessoaDomain = mapper.pessoaDtoToDomain(pessoa);
		pessoaDomain.setUnidade(unidadeDe(pessoa));
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
		return mapper.listPessoaDomainToDto(pessoaRepository.findAll());
	}

	@Override
	@Transactional(readOnly = true)
	public PessoaDto findPessoaId(Long idPessoa) {
		// Lanca em vez de devolver null: quem chama precisa do condominio do morador para a
		// checagem de escopo, e um null ali viraria NullPointerException em vez de 404.
		return mapper.pessoaDomainToDto(pessoaPorId(idPessoa));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PessoaDto> findPessoas(Long idCondominio, Long idUnidade, Boolean semUnidade, String busca) {
		Specification<Pessoa> filtro = Specification.allOf(
				PessoaSpecifications.doCondominio(idCondominio),
				PessoaSpecifications.daUnidade(idUnidade),
				PessoaSpecifications.semUnidade(semUnidade),
				PessoaSpecifications.comBusca(busca));

		return mapper.listPessoaDomainToDto(pessoaRepository.findAll(filtro));
	}

	@Override
	@Transactional
	public PessoaDto updatePessoa(Long idPessoa, PessoaDto pessoaDto) {
		Pessoa pessoa = pessoaPorId(idPessoa);

		pessoa.setNomeCompleto(pessoaDto.getNomeCompleto());
		pessoa.setCpf(pessoaDto.getCpf());
		pessoa.setEnvioTaxaEmail(pessoaDto.getEnvioTaxaEmail());
		pessoa.setEnvioImpresso(pessoaDto.getEnvioImpresso());
		pessoa.setIsSindico(pessoaDto.getIsSindico());
		pessoa.setUnidade(unidadeDe(pessoaDto));

		return mapper.pessoaDomainToDto(pessoaRepository.save(pessoa));
	}

	@Override
	@Transactional
	public void deletePessoa(Long idPessoa) {
		Pessoa pessoa = pessoaPorId(idPessoa);
		// Meios de contato apontam para a pessoa; apagar so a pessoa deixaria FK quebrada.
		meioContatoRepository.deleteByPessoaId(idPessoa);
		pessoaRepository.delete(pessoa);
	}

	/**
	 * Unidade informada, ou {@code null} para desvincular. Nulo e resposta valida: morador sem
	 * unidade e um estado previsto, nao um erro.
	 */
	private Unidade unidadeDe(PessoaDto pessoaDto) {
		Long idUnidade = pessoaDto.getUnidade() == null ? null : pessoaDto.getUnidade().getId();
		if (idUnidade == null) {
			return null;
		}
		return unidadeRepository.findById(idUnidade)
				.orElseThrow(() -> new UnidadeNaoEncontradaException("Unidade " + idUnidade + " nao encontrada."));
	}

	private Pessoa pessoaPorId(Long idPessoa) {
		return pessoaRepository.findById(idPessoa)
				.orElseThrow(() -> new PessoaNaoEncontradaException("Morador " + idPessoa + " nao encontrado."));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PessoaDto> findPessoasCondominioId(Long idCondominio) {
		return mapper.listPessoaDomainToDto(pessoaRepository.findByCondominioId(idCondominio));
	}

}
