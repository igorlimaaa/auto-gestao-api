package br.com.gestaocondial.autogestao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.PessoaApi;
import br.com.gestaocondial.autogestao.api.model.MeioContato;
import br.com.gestaocondial.autogestao.api.model.Pessoa;
import br.com.gestaocondial.autogestao.mapper.PessoaApiMapper;
import br.com.gestaocondial.autogestao.service.PessoaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PessoaController implements PessoaApi {

	private final PessoaService pessoaService;

	private final PessoaApiMapper pessoaApiMapper;

	@Override
	public ResponseEntity<Pessoa> savePessoa(Pessoa pessoa) {
		var criada = pessoaService.createNewPessoa(pessoaApiMapper.toDto(pessoa));
		return new ResponseEntity<>(pessoaApiMapper.toApi(criada), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<List<Pessoa>> findEnvioPorEmailPorCondominio(Long id) {
		return new ResponseEntity<>(pessoaApiMapper.toApiList(pessoaService.findPessoasEnvioEmail(id)), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<MeioContato> saveMeioContato(MeioContato meioContato) {
		var criado = pessoaService.createMeioContato(pessoaApiMapper.toDto(meioContato));
		return new ResponseEntity<>(pessoaApiMapper.toApi(criado), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<List<Pessoa>> findAll() {
		return new ResponseEntity<>(pessoaApiMapper.toApiList(pessoaService.findPessoas()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Pessoa> findPessoaId(Long idPessoa) {
		return new ResponseEntity<>(pessoaApiMapper.toApi(pessoaService.findPessoaId(idPessoa)), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Pessoa>> findPessoasCondominio(Long idCondominio) {
		return new ResponseEntity<>(pessoaApiMapper.toApiList(pessoaService.findPessoasCondominioId(idCondominio)),
				HttpStatus.OK);
	}

}
