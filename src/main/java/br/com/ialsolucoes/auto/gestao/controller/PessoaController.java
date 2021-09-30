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

import br.com.ialsolucoes.auto.gestao.dto.MeioContatoDto;
import br.com.ialsolucoes.auto.gestao.dto.PessoaDto;
import br.com.ialsolucoes.auto.gestao.service.PessoaService;

@RestController
@RequestMapping("/pessoa")
@Transactional
@ResponseBody
public class PessoaController {

	@Autowired
	private PessoaService pessoaService;

	@PostMapping
	public ResponseEntity<PessoaDto> savePessoa(@Valid @RequestBody PessoaDto pessoaDto) {
		return new ResponseEntity<>(pessoaService.createNewPessoa(pessoaDto), null, HttpStatus.CREATED);
	}

	@GetMapping("/findEnvioPorEmailPorCondominio/{id}")
	public ResponseEntity<List<PessoaDto>> findEnvioPorEmailPorCondominio(@PathVariable Long id) {
		return new ResponseEntity<>(pessoaService.findPessoasEnvioEmail(id), null, HttpStatus.OK);
	}

	@PostMapping("/meioContatoPessoa")
	public ResponseEntity<MeioContatoDto> saveMeioContato(@Valid @RequestBody MeioContatoDto meioContatoDto) {
		return new ResponseEntity<>(pessoaService.createMeioContato(meioContatoDto), null, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<PessoaDto>> findAll() {
		return new ResponseEntity<>(pessoaService.findPessoas(), null, HttpStatus.OK);
	}

	@GetMapping("/{idPessoa}")
	public ResponseEntity<PessoaDto> findPessoaId(@PathVariable Long idPessoa) {
		return new ResponseEntity<>(pessoaService.findPessoaId(idPessoa), null, HttpStatus.OK);
	}

	@GetMapping("/listPessoasCondominio/{idCondominio}")
	public ResponseEntity<List<PessoaDto>> findPessoasCondominio(@PathVariable Long idCondominio) {
		return new ResponseEntity<>(pessoaService.findPessoasCondominioId(idCondominio), null, HttpStatus.OK);
	}

}
