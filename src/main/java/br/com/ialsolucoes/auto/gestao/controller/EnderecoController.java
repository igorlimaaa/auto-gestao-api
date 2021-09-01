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

import br.com.ialsolucoes.auto.gestao.dto.EnderecoDto;
import br.com.ialsolucoes.auto.gestao.service.EnderecoService;

@RestController
@RequestMapping("/endereco")
@Transactional
@ResponseBody
public class EnderecoController {
	
	@Autowired
	private EnderecoService enderecoService;
	
	@PostMapping
	public ResponseEntity<EnderecoDto> saveEndereco(@Valid @RequestBody EnderecoDto enderecoDto){
		return new ResponseEntity<>(enderecoService.createNewEndereco(enderecoDto), null, HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<List<EnderecoDto>> findEndereco(){
		return new ResponseEntity<>(enderecoService.getEnderecos(), null, HttpStatus.OK);
	}

}
