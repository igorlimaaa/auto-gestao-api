package br.com.ialsolucoes.auto.gestao.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.ialsolucoes.auto.gestao.domain.Condominio;
import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;
import br.com.ialsolucoes.auto.gestao.mapper.CondominioMapper;
import br.com.ialsolucoes.auto.gestao.repository.CondominioRepository;

@RestController
@RequestMapping("/condominio")
@Transactional
@ResponseBody
public class CondominioController {
	
	@Autowired
	private CondominioMapper mapper;
	
	@Autowired
	private CondominioRepository condominioRepository;
	
	@GetMapping
	public ResponseEntity<List<CondominioDto>> listCondominios(){
		
		List<CondominioDto> listCondominio = new ArrayList<>();
		CondominioDto novo = new CondominioDto();
		Integer id = 1;
		novo.setId(id.longValue());
		novo.setNumeroTelefone(9999999L);
		novo.setDdd(81);
		
		Condominio cond = mapper.condominioDtoToDomain(novo);
		
		listCondominio.add(novo);
		
		List<Condominio> listCond = mapper.listCondominioDtoToListDomain(listCondominio);
		
		return new ResponseEntity<List<CondominioDto>>(listCondominio, null, HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<CondominioDto> createCondominio(@Valid @RequestBody CondominioDto condominioDTo){
		Condominio condominoDomain = mapper.condominioDtoToDomain(condominioDTo);
		condominoDomain = condominioRepository.save(condominoDomain);
		return new ResponseEntity<CondominioDto>(mapper.condominioDomainToDto(condominoDomain), null, HttpStatus.CREATED);
	}

	
}
