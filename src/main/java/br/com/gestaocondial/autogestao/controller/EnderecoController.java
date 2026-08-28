package br.com.gestaocondial.autogestao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.EnderecoApi;
import br.com.gestaocondial.autogestao.api.model.Endereco;
import br.com.gestaocondial.autogestao.mapper.EnderecoApiMapper;
import br.com.gestaocondial.autogestao.service.EnderecoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EnderecoController implements EnderecoApi {

	private final EnderecoService enderecoService;

	private final EnderecoApiMapper enderecoApiMapper;

	@Override
	public ResponseEntity<Endereco> saveEndereco(Endereco endereco) {
		var criado = enderecoService.createNewEndereco(enderecoApiMapper.toDto(endereco));
		return new ResponseEntity<>(enderecoApiMapper.toApi(criado), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<List<Endereco>> findEndereco() {
		return new ResponseEntity<>(enderecoService.getEnderecos().stream().map(enderecoApiMapper::toApi).toList(),
				HttpStatus.OK);
	}

}
