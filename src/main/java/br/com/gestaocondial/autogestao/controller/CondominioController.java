package br.com.gestaocondial.autogestao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.CondominioApi;
import br.com.gestaocondial.autogestao.api.model.Condominio;
import br.com.gestaocondial.autogestao.api.model.TaxaExtra;
import br.com.gestaocondial.autogestao.mapper.CondominioApiMapper;
import br.com.gestaocondial.autogestao.mapper.TaxaExtraApiMapper;
import br.com.gestaocondial.autogestao.service.CondominioService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CondominioController implements CondominioApi {

	private final CondominioService condominioService;

	private final CondominioApiMapper condominioApiMapper;

	private final TaxaExtraApiMapper taxaExtraApiMapper;

	@Override
	public ResponseEntity<List<Condominio>> listCondominios() {
		return new ResponseEntity<>(condominioApiMapper.toApiList(condominioService.listCondominios()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Condominio> createCondominio(Condominio condominio) {
		var criado = condominioService.createNewCondominio(condominioApiMapper.toDto(condominio));
		return new ResponseEntity<>(condominioApiMapper.toApi(criado), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<TaxaExtra> createTaxaExtra(TaxaExtra taxaExtra) {
		var criada = condominioService.createTaxaExtra(taxaExtraApiMapper.toDto(taxaExtra));
		return new ResponseEntity<>(taxaExtraApiMapper.toApi(criada), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<List<TaxaExtra>> findTaxaExtraCondominio(Long idCondominio) {
		return new ResponseEntity<>(taxaExtraApiMapper.toApiList(condominioService.listTaxaExtra(idCondominio)),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Condominio> findCondominio(Long idCondominio) {
		return new ResponseEntity<>(condominioApiMapper.toApi(condominioService.findCondominio(idCondominio)),
				HttpStatus.OK);
	}

}
