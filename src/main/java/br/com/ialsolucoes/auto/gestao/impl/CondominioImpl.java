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
		preencherValorTaxasExtras(listDto, listTaxaExtra);
		preencherValorFinal(listDto);
		return listDto;
	}

	@Override
	public CondominioDto createNewCondominio(CondominioDto condominioDTo) {
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
	
	private List<CondominioDto> preencherValorTaxasExtras(List<CondominioDto> listDto, List<TaxaExtra> listTaxaExtra){
		listDto.stream().forEach(listDtoCond -> {
			if(listDtoCond.getPossuiTaxaExtra()) {
				listTaxaExtra.stream().filter(listTaxa -> listDtoCond.getId() == listTaxa.getCondominio().getId()).forEach(condDto -> {
					Double valor = listDtoCond.getValorTaxasExtras() != null ? listDtoCond.getValorTaxasExtras() : DOUBLE_ZERO;
					valor += condDto.getValorTaxaExtra();
					listDtoCond.setValorTaxasExtras(valor);
				});				
			}else {
				listDtoCond.setValorTaxasExtras(DOUBLE_ZERO);
			}
		});
		return listDto;
	}
	
	private List<CondominioDto> preencherValorFinal(List<CondominioDto> listDto){
		listDto.stream().forEach(listDtoCond -> {
			if(listDtoCond.getPossuiTaxaExtra()) {
				listDtoCond.setValorTotalPorUnidade(listDtoCond.getValorTaxasExtras() + listDtoCond.getValorTaxaCondominial());
			}else {
				listDtoCond.setValorTotalPorUnidade(listDtoCond.getValorTaxaCondominial());
			}
		});
		return listDto;
	}
	
}
