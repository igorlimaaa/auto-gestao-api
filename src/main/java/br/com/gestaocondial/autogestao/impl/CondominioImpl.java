package br.com.gestaocondial.autogestao.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Endereco;
import br.com.gestaocondial.autogestao.domain.TaxaExtra;
import br.com.gestaocondial.autogestao.dto.CondominioDto;
import br.com.gestaocondial.autogestao.dto.TaxaExtraDto;
import br.com.gestaocondial.autogestao.exception.CondominioNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.TaxaExtraNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.RecursoEmUsoException;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.repository.PessoaRepository;
import br.com.gestaocondial.autogestao.exception.EnderecoNaoEncontradoException;
import br.com.gestaocondial.autogestao.mapper.CondominioMapper;
import br.com.gestaocondial.autogestao.mapper.TaxaExtraMapper;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EnderecoRepository;
import br.com.gestaocondial.autogestao.repository.TaxaExtraRepository;
import br.com.gestaocondial.autogestao.service.CondominioService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CondominioImpl implements CondominioService {

	private static final Double DOUBLE_ZERO = 0.00;

	private final CondominioRepository condominioRepository;

	private final UnidadeRepository unidadeRepository;

	private final PessoaRepository pessoaRepository;

	private final EnderecoRepository enderecoRepository;

	private final TaxaExtraRepository taxaRepository;

	private final CondominioMapper mapper;

	private final TaxaExtraMapper mapperTaxa;

	@Override
	@Transactional(readOnly = true)
	public List<CondominioDto> listCondominios() {
		List<Condominio> listCondDomain = condominioRepository.findAll();
		List<TaxaExtra> listTaxaExtra = taxaRepository.findAll();
		List<CondominioDto> listDto = mapper.listCondominioDomainToDto(listCondDomain);
		preencherListValorTaxasExtras(listDto, listTaxaExtra);
		preencherListValorFinal(listDto);
		return listDto;
	}

	@Override
	@Transactional
	public CondominioDto createNewCondominio(CondominioDto condominioDTo) {
		if (condominioDTo.getEndereco() != null && condominioDTo.getEndereco().getId() != null) {
			Optional<Endereco> hasEndereco = enderecoRepository.findById(condominioDTo.getEndereco().getId());
			if (!hasEndereco.isPresent()) {
				throw new EnderecoNaoEncontradoException(
						"Endereco id \"" + condominioDTo.getEndereco().getId() + "não encontrado ou não existe");
			}
		}
		condominioDTo = validarMultaEjuros(condominioDTo);
		Condominio condominoDomain = mapper.condominioDtoToDomain(condominioDTo);
		condominoDomain = condominioRepository.save(condominoDomain);
		Optional<Endereco> enderecoDomain = enderecoRepository.findById(condominoDomain.getEndereco().getId());
		condominoDomain.setEndereco(enderecoDomain.get());
		return mapper.condominioDomainToDto(condominoDomain);
	}

	@Override
	@Transactional
	public CondominioDto updateCondominio(Long idCondominio, CondominioDto condominioDto) {
		Condominio condominio = condominioPorId(idCondominio);

		if (condominioDto.getEndereco() != null && condominioDto.getEndereco().getId() != null) {
			Long idEndereco = condominioDto.getEndereco().getId();
			Endereco endereco = enderecoRepository.findById(idEndereco)
					.orElseThrow(() -> new EnderecoNaoEncontradoException(
							"Endereco id \"" + idEndereco + "\" nao encontrado ou nao existe"));
			condominio.setEndereco(endereco);
		}

		condominioDto = validarMultaEjuros(condominioDto);
		condominio.setDdd(condominioDto.getDdd() == null ? null : condominioDto.getDdd().longValue());
		condominio.setNumeroTelefone(condominioDto.getNumeroTelefone());
		condominio.setValorTaxaCondominial(condominioDto.getValorTaxaCondominial());
		condominio.setValorJuros(condominioDto.getValorJuros());
		condominio.setValorMulta(condominioDto.getValorMulta());

		condominio = condominioRepository.save(condominio);

		CondominioDto salvo = mapper.condominioDomainToDto(condominio);
		preencherValorTaxasExtras(salvo, taxaRepository.findAllByCondominioId(idCondominio));
		return preencherValorFinal(salvo);
	}

	/**
	 * Exclusao recusada enquanto houver unidade, morador ou taxa vinculados. Apagar em cascata
	 * levaria junto o cadastro de quem mora la, sem deixar rastro do porque.
	 */
	@Override
	@Transactional
	public void deleteCondominio(Long idCondominio) {
		Condominio condominio = condominioPorId(idCondominio);

		long unidades = unidadeRepository.countByCondominioId(idCondominio);
		long moradores = pessoaRepository.countByCondominioId(idCondominio);
		long taxas = taxaRepository.countByCondominioId(idCondominio);

		if (unidades + moradores + taxas > 0) {
			throw new RecursoEmUsoException("O condominio ainda tem " + unidades + " unidade(s), " + moradores
					+ " morador(es) e " + taxas + " taxa(s) vinculados. Remova esses registros antes de exclui-lo.");
		}

		condominioRepository.delete(condominio);
	}

	@Override
	@Transactional
	public TaxaExtraDto updateTaxaExtra(Long idTaxaExtra, TaxaExtraDto taxaDto) {
		TaxaExtra taxa = taxaRepository.findById(idTaxaExtra)
				.orElseThrow(() -> new TaxaExtraNaoEncontradaException(
						"Taxa extra " + idTaxaExtra + " nao encontrada."));

		taxa.setValorTaxaExtra(taxaDto.getValorTaxaExtra());
		taxa.setNumeroParcelas(taxaDto.getNumeroParcelas());
		taxa.setDescricaoTaxa(taxaDto.getDescricaoTaxa());

		return mapperTaxa.taxaExtraDomainToDto(taxaRepository.save(taxa));
	}

	@Override
	@Transactional
	public void deleteTaxaExtra(Long idTaxaExtra) {
		TaxaExtra taxa = taxaRepository.findById(idTaxaExtra)
				.orElseThrow(() -> new TaxaExtraNaoEncontradaException(
						"Taxa extra " + idTaxaExtra + " nao encontrada."));

		Long idCondominio = taxa.getCondominio().getId();
		taxaRepository.delete(taxa);

		// Sem isso o condominio continuaria marcado como "possui taxa extra" tendo zero taxas,
		// e o calculo do valor final somaria um extra que nao existe mais.
		if (taxaRepository.countByCondominioId(idCondominio) == 0) {
			condominioRepository.findById(idCondominio).ifPresent(condominio -> {
				condominio.setPossuiTaxaExtra(false);
				condominioRepository.save(condominio);
			});
		}
	}

	private Condominio condominioPorId(Long idCondominio) {
		return condominioRepository.findById(idCondominio)
				.orElseThrow(() -> new CondominioNaoEncontradoException(
						"Condominio " + idCondominio + " nao encontrado."));
	}

	private CondominioDto validarMultaEjuros(CondominioDto condominioDTo) {
		if (condominioDTo.getValorJuros() == null) {
			condominioDTo.setValorJuros(DOUBLE_ZERO);
		}
		if (condominioDTo.getValorMulta() == null) {
			condominioDTo.setValorMulta(DOUBLE_ZERO);
		}
		return condominioDTo;
	}

	@Override
	@Transactional
	public TaxaExtraDto createTaxaExtra(TaxaExtraDto taxaDto) {
		TaxaExtra taxaDomain = mapperTaxa.taxaExtraDtoToDomain(taxaDto);
		Optional<Condominio> cond = condominioRepository.findById(taxaDomain.getCondominio().getId());
		taxaDomain = taxaRepository.save(taxaDomain);
		if (taxaDomain != null && cond.isPresent() && !cond.get().getPossuiTaxaExtra()) {
			cond.get().setPossuiTaxaExtra(true);
			condominioRepository.save(cond.get());
		}
		taxaDomain.setCondominio(cond.get());
		return mapperTaxa.taxaExtraDomainToDto(taxaDomain);
	}

	private List<CondominioDto> preencherListValorTaxasExtras(List<CondominioDto> listDto,
			List<TaxaExtra> listTaxaExtra) {
		listDto.stream().forEach(listDtoCond -> {
			preencherValorTaxasExtras(listDtoCond, listTaxaExtra);
		});
		return listDto;
	}

	private CondominioDto preencherValorTaxasExtras(CondominioDto condominioDto, List<TaxaExtra> listTaxaExtra) {
		// Zera antes de somar mesmo quando possuiTaxaExtra e true: um condominio marcado como
		// tendo taxa extra mas sem nenhuma taxa cadastrada deixava o valor nulo, e o calculo do
		// valor final estourava NullPointerException.
		condominioDto.setValorTaxasExtras(DOUBLE_ZERO);

		if (Boolean.TRUE.equals(condominioDto.getPossuiTaxaExtra())) {
			listTaxaExtra.stream()
					// Objects.equals, nao ==: comparar dois Long por identidade so funciona por
					// acidente ate 127 (cache de boxing) e devolve falso a partir dai, somando
					// taxa extra de menos sem erro nenhum.
					.filter(taxa -> Objects.equals(condominioDto.getId(), taxa.getCondominio().getId()))
					.forEach(taxa -> {
						Double valor = condominioDto.getValorTaxasExtras() + taxa.getValorTaxaExtra();
						condominioDto.setValorTaxasExtras(valor);
					});
		}
		return condominioDto;
	}

	private List<CondominioDto> preencherListValorFinal(List<CondominioDto> listDto) {
		listDto.stream().forEach(listDtoCond -> {
			preencherValorFinal(listDtoCond);
		});
		return listDto;
	}

	private CondominioDto preencherValorFinal(CondominioDto condDto) {
		Double taxaCondominial = condDto.getValorTaxaCondominial() != null
				? condDto.getValorTaxaCondominial()
				: DOUBLE_ZERO;
		Double taxasExtras = Boolean.TRUE.equals(condDto.getPossuiTaxaExtra()) && condDto.getValorTaxasExtras() != null
				? condDto.getValorTaxasExtras()
				: DOUBLE_ZERO;

		condDto.setValorTotalPorUnidade(taxaCondominial + taxasExtras);
		return condDto;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TaxaExtraDto> listTaxaExtra(Long idCondominio) {
		// idCondominio nulo = todas as taxas. Antes, o metodo so aceitava um id, e chamar sem
		// filtro devolvia lista vazia — o que impedia a listagem global.
		List<TaxaExtra> listTaxaExtra = idCondominio == null
				? taxaRepository.findAll()
				: taxaRepository.findAllByCondominioId(idCondominio);

		// O condominio permanece no DTO: a listagem global precisa dizer a que condominio cada
		// taxa pertence. Lista vazia sai como [], nunca null — null vira corpo vazio no HTTP,
		// que o cliente nao consegue distinguir de uma falha.
		return mapperTaxa.listTaxaExtraDomainToDto(listTaxaExtra);
	}

	@Override
	@Transactional(readOnly = true)
	public CondominioDto findCondominio(Long idCondominio) {
		Optional<Condominio> cond = condominioRepository.findById(idCondominio);
		if (cond.isPresent()) {
			CondominioDto condominioDomainToDto = mapper.condominioDomainToDto(cond.get());
			List<TaxaExtra> listTaxaExtra = taxaRepository.findAllByCondominioId(condominioDomainToDto.getId());
			preencherValorTaxasExtras(condominioDomainToDto, listTaxaExtra);
			preencherValorFinal(condominioDomainToDto);
			return condominioDomainToDto;
		}
		return null;
	}

}
