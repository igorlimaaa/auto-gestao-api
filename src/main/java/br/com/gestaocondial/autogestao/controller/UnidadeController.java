package br.com.gestaocondial.autogestao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.UnidadeApi;
import br.com.gestaocondial.autogestao.api.model.Unidade;
import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.mapper.UnidadeApiMapper;
import br.com.gestaocondial.autogestao.service.UnidadeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UnidadeController implements UnidadeApi {

	private final UnidadeService unidadeService;

	private final UnidadeApiMapper unidadeApiMapper;

	private final EscopoDoCondominio escopoDoCondominio;

	@Override
	@PreAuthorize("hasAuthority('UNIDADE_LER')")
	public ResponseEntity<List<Unidade>> listUnidades(Long idCondominio, Boolean ativa, String busca) {
		Long escopo = escopoDoCondominio.restringir(idCondominio);
		return ResponseEntity.ok(unidadeApiMapper.toApiList(unidadeService.listUnidades(escopo, ativa, busca)));
	}

	@Override
	@PreAuthorize("hasAuthority('UNIDADE_LER')")
	public ResponseEntity<Unidade> findUnidade(Long idUnidade) {
		var unidade = unidadeService.findUnidade(idUnidade);
		escopoDoCondominio.exigirAcesso(unidade.getCondominio().getId());
		return ResponseEntity.ok(unidadeApiMapper.toApi(unidade));
	}

	@Override
	@PreAuthorize("hasAuthority('UNIDADE_ESCREVER')")
	public ResponseEntity<Unidade> createUnidade(Unidade unidade) {
		var dto = unidadeApiMapper.toDto(unidade);
		escopoDoCondominio.exigirAcesso(dto.getCondominio() == null ? null : dto.getCondominio().getId());
		return new ResponseEntity<>(unidadeApiMapper.toApi(unidadeService.createUnidade(dto)), HttpStatus.CREATED);
	}

	@Override
	@PreAuthorize("hasAuthority('UNIDADE_ESCREVER')")
	public ResponseEntity<Unidade> updateUnidade(Long idUnidade, Unidade unidade) {
		escopoDoCondominio.exigirAcesso(unidadeService.findUnidade(idUnidade).getCondominio().getId());
		return ResponseEntity.ok(unidadeApiMapper.toApi(unidadeService.updateUnidade(idUnidade,
				unidadeApiMapper.toDto(unidade))));
	}

	@Override
	@PreAuthorize("hasAuthority('UNIDADE_ESCREVER')")
	public ResponseEntity<Void> deleteUnidade(Long idUnidade) {
		escopoDoCondominio.exigirAcesso(unidadeService.findUnidade(idUnidade).getCondominio().getId());
		unidadeService.deleteUnidade(idUnidade);
		return ResponseEntity.noContent().build();
	}

}
