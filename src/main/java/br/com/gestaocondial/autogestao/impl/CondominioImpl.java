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
		List<TaxaExtra> listTaxaExtra = taxaRepository.findAllByCondominioId(idCondominio);
		if (!listTaxaExtra.isEmpty() && listTaxaExtra != null) {
			List<TaxaExtraDto> listTaxaExtraDomainToDto = mapperTaxa.listTaxaExtraDomainToDto(listTaxaExtra);
			for (TaxaExtraDto forList : listTaxaExtraDomainToDto) {
				forList.setCondominio(null);
			}
			return listTaxaExtraDomainToDto;
		}
		return null;
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
