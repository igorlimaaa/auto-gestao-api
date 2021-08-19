package br.com.ialsolucoes.auto.gestao.controller;

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

import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;
import br.com.ialsolucoes.auto.gestao.service.CondominioService;

@RestController
@RequestMapping("/condominio")
@Transactional
@ResponseBody
public class CondominioController {
	
	@Autowired
	private CondominioService condominioService;
	
	@GetMapping
	public ResponseEntity<List<CondominioDto>> listCondominios(){
		return new ResponseEntity<List<CondominioDto>>(condominioService.listCondominios(), null, HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<CondominioDto> createCondominio(@Valid @RequestBody CondominioDto condominioDTo){
		return new ResponseEntity<CondominioDto>(condominioService.createNewCondominio(condominioDTo), null, HttpStatus.CREATED);
	}

	
}
