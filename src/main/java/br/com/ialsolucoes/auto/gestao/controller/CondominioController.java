package br.com.ialsolucoes.auto.gestao.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.ialsolucoes.auto.gestao.dto.CondominioDto;
import br.com.ialsolucoes.auto.gestao.dto.PessoaDto;
import br.com.ialsolucoes.auto.gestao.dto.TaxaExtraDto;
import br.com.ialsolucoes.auto.gestao.service.CondominioService;

@RestController
@RequestMapping("/condominio")
@Transactional
@ResponseBody
public class CondominioController {

	@Autowired
	private CondominioService condominioService;

	@GetMapping
	public ResponseEntity<List<CondominioDto>> listCondominios() {
		return new ResponseEntity<List<CondominioDto>>(condominioService.listCondominios(), null, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<CondominioDto> createCondominio(@Valid @RequestBody CondominioDto condominioDTo) {
		return new ResponseEntity<CondominioDto>(condominioService.createNewCondominio(condominioDTo), null,
				HttpStatus.CREATED);
	}

	@PostMapping("/taxaExtra")
	public ResponseEntity<TaxaExtraDto> createTaxa(@Valid @RequestBody TaxaExtraDto taxaDto) {
		return new ResponseEntity<TaxaExtraDto>(condominioService.createTaxaExtra(taxaDto), null, HttpStatus.CREATED);
	}

	@GetMapping("/taxaExtraPorCondominio/{idCondominio}")
	public ResponseEntity<List<TaxaExtraDto>> findTaxaExtraCondominio(@PathVariable Long idCondominio) {
		return new ResponseEntity<>(condominioService.listTaxaExtra(idCondominio), null, HttpStatus.OK);
	}

	@GetMapping("/{idCondominio}")
	public ResponseEntity<CondominioDto> findCondominio(@PathVariable Long idCondominio) {
		return new ResponseEntity<>(condominioService.findCondominio(idCondominio), null, HttpStatus.OK);
	}

}
