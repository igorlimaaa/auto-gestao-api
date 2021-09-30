package br.com.ialsolucoes.auto.gestao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.ialsolucoes.auto.gestao.domain.Condominio;
import br.com.ialsolucoes.auto.gestao.domain.Endereco;
import br.com.ialsolucoes.auto.gestao.domain.TaxaExtra;
import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;
import br.com.ialsolucoes.auto.gestao.dto.TaxaExtraDto;
import br.com.ialsolucoes.auto.gestao.exception.EnderecoNaoEncontradoException;
import br.com.ialsolucoes.auto.gestao.mapper.CondominioMapper;
import br.com.ialsolucoes.auto.gestao.mapper.TaxaExtraMapper;
import br.com.ialsolucoes.auto.gestao.repository.CondominioRepository;
import br.com.ialsolucoes.auto.gestao.repository.EnderecoRepository;
import br.com.ialsolucoes.auto.gestao.repository.TaxaExtraRepository;
import br.com.ialsolucoes.auto.gestao.service.CondominioService;

@Service
public class CondominioImpl implements CondominioService {
	
	private static final Double DOUBLE_ZERO = 0.00;
	
	@Autowired
	private CondominioRepository condominioRepository;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	@Autowired 
	private TaxaExtraRepository taxaRepository;
	
	@Autowired
	private CondominioMapper mapper;
	
	@Autowired
	private TaxaExtraMapper mapperTaxa;

	@Override
	public List<CondominioDto> listCondominios() {
		List<Condominio> listCondDomain = condominioRepository.findAll();
		List<TaxaExtra> listTaxaExtra = taxaRepository.findAll();
		List<CondominioDto> listDto = mapper.listCondominioDomainToDto(listCondDomain);		
		preencherListValorTaxasExtras(listDto, listTaxaExtra);
		preencherListValorFinal(listDto);
		return listDto;
	}

	@Override
	public CondominioDto createNewCondominio(CondominioDto condominioDTo) throws Exception{
		if(condominioDTo.getEndereco() != null && condominioDTo.getEndereco().getId() != null) {
			Optional<Endereco> hasEndereco = enderecoRepository.findById(condominioDTo.getEndereco().getId());
			if(!hasEndereco.isPresent()) {
				throw new EnderecoNaoEncontradoException("Endereco id \"" + condominioDTo.getEndereco().getId() + "não encontrado ou não existe");
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
		if(condominioDTo.getValorJuros() == null) {
			condominioDTo.setValorJuros(DOUBLE_ZERO);
		}
		if(condominioDTo.getValorMulta() == null) {
			condominioDTo.setValorMulta(DOUBLE_ZERO);
		}
		return condominioDTo;
	}

	@Override
	public TaxaExtraDto createTaxaExtra(TaxaExtraDto taxaDto) {
		TaxaExtra taxaDomain = mapperTaxa.taxaExtraDtoToDomain(taxaDto);
		Optional<Condominio> cond = condominioRepository.findById(taxaDomain.getCondominio().getId());
		taxaDomain = taxaRepository.save(taxaDomain);
		if(taxaDomain != null && cond.isPresent() && !cond.get().getPossuiTaxaExtra()) {
			cond.get().setPossuiTaxaExtra(true);
			condominioRepository.save(cond.get());
		}
		taxaDomain.setCondominio(cond.get());
		return mapperTaxa.taxaExtraDomainToDto(taxaDomain);
	}
	
	private List<CondominioDto> preencherListValorTaxasExtras(List<CondominioDto> listDto, List<TaxaExtra> listTaxaExtra){
		listDto.stream().forEach(listDtoCond -> {
			preencherValorTaxasExtras(listDtoCond, listTaxaExtra);
		});
		return listDto;
	}
	
	private CondominioDto preencherValorTaxasExtras(CondominioDto condominioDto, List<TaxaExtra> listTaxaExtra) {
		if (condominioDto.getPossuiTaxaExtra()) {
			listTaxaExtra.stream().filter(listTaxa -> condominioDto.getId() == listTaxa.getCondominio().getId())
					.forEach(condDto -> {
						Double valor = condominioDto.getValorTaxasExtras() != null ? condominioDto.getValorTaxasExtras()
								: DOUBLE_ZERO;
						valor += condDto.getValorTaxaExtra();
						condominioDto.setValorTaxasExtras(valor);
					});
		} else {
			condominioDto.setValorTaxasExtras(DOUBLE_ZERO);
		}
		return condominioDto;
	}
	
	private List<CondominioDto> preencherListValorFinal(List<CondominioDto> listDto){
		listDto.stream().forEach(listDtoCond -> {
			preencherValorFinal(listDtoCond);
		});
		return listDto;
	}
	
	private CondominioDto preencherValorFinal(CondominioDto condDto) {
		if(condDto.getPossuiTaxaExtra()) {
			condDto.setValorTotalPorUnidade(condDto.getValorTaxasExtras() + condDto.getValorTaxaCondominial());
		}else {
			condDto.setValorTotalPorUnidade(condDto.getValorTaxaCondominial());
		}
		return condDto;
	}

	@Override
	public List<TaxaExtraDto> listTaxaExtra(Long idCondominio) {
		List<TaxaExtra> listTaxaExtra = taxaRepository.findAllByCondominioId(idCondominio);
		if(!listTaxaExtra.isEmpty() && listTaxaExtra != null) {
			List<TaxaExtraDto> listTaxaExtraDomainToDto = mapperTaxa.listTaxaExtraDomainToDto(listTaxaExtra);
			for(TaxaExtraDto forList : listTaxaExtraDomainToDto) {
				forList.setCondominio(null);
			}
			return listTaxaExtraDomainToDto;
		}
		return null;
	}

	@Override
	public CondominioDto findCondominio(Long idCondominio) {
		Optional<Condominio> cond = condominioRepository.findById(idCondominio);
		if(cond.isPresent()) {
			CondominioDto condominioDomainToDto = mapper.condominioDomainToDto(cond.get());
			List<TaxaExtra> listTaxaExtra = taxaRepository.findAllByCondominioId(condominioDomainToDto.getId());
			preencherValorTaxasExtras(condominioDomainToDto, listTaxaExtra);
			preencherValorFinal(condominioDomainToDto);
			return condominioDomainToDto;
		}
		return null;
	}
	
}
