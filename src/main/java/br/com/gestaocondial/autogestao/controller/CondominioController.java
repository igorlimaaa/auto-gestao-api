package br.com.gestaocondial.autogestao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.CondominioApi;
import br.com.gestaocondial.autogestao.api.model.Condominio;
import br.com.gestaocondial.autogestao.api.model.TaxaExtra;
import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.dto.CondominioDto;
import br.com.gestaocondial.autogestao.dto.TaxaExtraDto;
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

	private final EscopoDoCondominio escopoDoCondominio;

	@Override
	@PreAuthorize("hasAuthority('CONDOMINIO_LER')")
	public ResponseEntity<List<Condominio>> listCondominios() {
		List<CondominioDto> condominios = condominioService.listCondominios();

		// Num perfil restrito a listagem traz só o condomínio da sessão. O filtro é aplicado
		// aqui, e não no service, porque a regra é de quem está pedindo, não do domínio.
		if (!escopoDoCondominio.ehGlobal()) {
			Long escopo = escopoDoCondominio.doPerfilAtivo();
			condominios = condominios.stream().filter(condominio -> escopo.equals(condominio.getId())).toList();
		}

		return ResponseEntity.ok(condominioApiMapper.toApiList(condominios));
	}

	@Override
	@PreAuthorize("hasAuthority('CONDOMINIO_LER')")
	public ResponseEntity<Condominio> findCondominio(Long idCondominio) {
		escopoDoCondominio.exigirAcesso(idCondominio);
		return ResponseEntity.ok(condominioApiMapper.toApi(condominioService.findCondominio(idCondominio)));
	}

	@Override
	@PreAuthorize("hasAuthority('CONDOMINIO_ESCREVER')")
	public ResponseEntity<Condominio> createCondominio(Condominio condominio) {
		var criado = condominioService.createNewCondominio(condominioApiMapper.toDto(condominio));
		return new ResponseEntity<>(condominioApiMapper.toApi(criado), HttpStatus.CREATED);
	}

	@Override
	@PreAuthorize("hasAuthority('CONDOMINIO_ESCREVER')")
	public ResponseEntity<Condominio> updateCondominio(Long idCondominio, Condominio condominio) {
		escopoDoCondominio.exigirAcesso(idCondominio);
		return ResponseEntity.ok(condominioApiMapper
				.toApi(condominioService.updateCondominio(idCondominio, condominioApiMapper.toDto(condominio))));
	}

	@Override
	@PreAuthorize("hasAuthority('CONDOMINIO_ESCREVER')")
	public ResponseEntity<Void> deleteCondominio(Long idCondominio) {
		escopoDoCondominio.exigirAcesso(idCondominio);
		condominioService.deleteCondominio(idCondominio);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAuthority('TAXA_LER')")
	public ResponseEntity<List<TaxaExtra>> listTaxasExtras(Long idCondominio) {
		Long escopo = escopoDoCondominio.restringir(idCondominio);
		return ResponseEntity.ok(taxaExtraApiMapper.toApiList(condominioService.listTaxaExtra(escopo)));
	}

	@Override
	@PreAuthorize("hasAuthority('TAXA_LER')")
	public ResponseEntity<List<TaxaExtra>> findTaxaExtraCondominio(Long idCondominio) {
		escopoDoCondominio.exigirAcesso(idCondominio);
		return ResponseEntity.ok(taxaExtraApiMapper.toApiList(condominioService.listTaxaExtra(idCondominio)));
	}

	@Override
	@PreAuthorize("hasAuthority('TAXA_ESCREVER')")
	public ResponseEntity<TaxaExtra> createTaxaExtra(TaxaExtra taxaExtra) {
		TaxaExtraDto dto = taxaExtraApiMapper.toDto(taxaExtra);
		escopoDoCondominio.exigirAcesso(dto.getCondominio() == null ? null : dto.getCondominio().getId());
		return new ResponseEntity<>(taxaExtraApiMapper.toApi(condominioService.createTaxaExtra(dto)),
				HttpStatus.CREATED);
	}

	@Override
	@PreAuthorize("hasAuthority('TAXA_ESCREVER')")
	public ResponseEntity<TaxaExtra> updateTaxaExtra(Long idTaxaExtra, TaxaExtra taxaExtra) {
		return ResponseEntity.ok(taxaExtraApiMapper
				.toApi(condominioService.updateTaxaExtra(idTaxaExtra, taxaExtraApiMapper.toDto(taxaExtra))));
	}

	@Override
	@PreAuthorize("hasAuthority('TAXA_ESCREVER')")
	public ResponseEntity<Void> deleteTaxaExtra(Long idTaxaExtra) {
		condominioService.deleteTaxaExtra(idTaxaExtra);
		return ResponseEntity.noContent().build();
	}

}
