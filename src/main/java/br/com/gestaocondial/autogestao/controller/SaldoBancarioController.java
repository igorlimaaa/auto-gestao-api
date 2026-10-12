package br.com.gestaocondial.autogestao.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.SaldoBancarioApi;
import br.com.gestaocondial.autogestao.api.model.SaldoBancario;
import br.com.gestaocondial.autogestao.mapper.SaldoBancarioApiMapper;
import br.com.gestaocondial.autogestao.service.SaldoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SaldoBancarioController implements SaldoBancarioApi {

	private final SaldoService saldoService;

	private final SaldoBancarioApiMapper apiMapper;

	@Override
	@PreAuthorize("hasAuthority('SALDO_LER')")
	public ResponseEntity<SaldoBancario> findSaldoBancario(Long idCondominio) {
		return ResponseEntity.ok()
				// Dado financeiro: nada de cache no navegador nem em intermediários.
				.cacheControl(CacheControl.noStore())
				.body(apiMapper.toApi(saldoService.consultarSaldo(idCondominio)));
	}

}
