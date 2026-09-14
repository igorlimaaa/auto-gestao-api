package br.com.gestaocondial.autogestao.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.CobrancaOrdinariaApi;
import br.com.gestaocondial.autogestao.api.model.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.api.model.DadosDePagamentoCobranca;
import br.com.gestaocondial.autogestao.api.model.RegistroDePagamento;
import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.dto.CobrancaOrdinariaDto;
import br.com.gestaocondial.autogestao.job.GeradorDeCobrancaOrdinariaJob;
import br.com.gestaocondial.autogestao.mapper.CobrancaOrdinariaApiMapper;
import br.com.gestaocondial.autogestao.service.CobrancaOrdinariaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CobrancaOrdinariaController implements CobrancaOrdinariaApi {

	private final CobrancaOrdinariaService cobrancaService;

	private final CobrancaOrdinariaApiMapper cobrancaApiMapper;

	private final EscopoDoCondominio escopoDoCondominio;

	private final GeradorDeCobrancaOrdinariaJob geradorDeCobrancaOrdinariaJob;

	@Override
	@PreAuthorize("hasAuthority('COBRANCA_LER')")
	public ResponseEntity<List<CobrancaOrdinaria>> listCobrancasOrdinarias(Long idCondominio, Long idUnidade,
			LocalDate competencia) {
		Long escopo = escopoDoCondominio.restringir(idCondominio);
		return ResponseEntity
				.ok(cobrancaApiMapper.toApiList(cobrancaService.listCobrancas(escopo, idUnidade, competencia)));
	}

	@Override
	@PreAuthorize("hasAuthority('COBRANCA_LER')")
	public ResponseEntity<CobrancaOrdinaria> findCobrancaOrdinaria(Long idCobrancaOrdinaria) {
		CobrancaOrdinariaDto cobranca = cobrancaService.findCobranca(idCobrancaOrdinaria);
		escopoDoCondominio.exigirAcesso(cobranca.getUnidade().getCondominio().getId());
		return ResponseEntity.ok(cobrancaApiMapper.toApi(cobranca));
	}

	@Override
	@PreAuthorize("hasAuthority('COBRANCA_ESCREVER')")
	public ResponseEntity<CobrancaOrdinaria> updateDadosDePagamentoCobrancaOrdinaria(Long idCobrancaOrdinaria,
			DadosDePagamentoCobranca dadosDePagamentoCobranca) {
		escopoDoCondominio.exigirAcesso(
				cobrancaService.findCobranca(idCobrancaOrdinaria).getUnidade().getCondominio().getId());
		return ResponseEntity.ok(cobrancaApiMapper.toApi(cobrancaService.atualizarDadosDePagamento(idCobrancaOrdinaria,
				dadosDePagamentoCobranca.getPixCopiaCola(), dadosDePagamentoCobranca.getLinhaDigitavel())));
	}

	@Override
	@PreAuthorize("hasAuthority('COBRANCA_ESCREVER')")
	public ResponseEntity<CobrancaOrdinaria> registrarPagamentoCobrancaOrdinaria(Long idCobrancaOrdinaria,
			RegistroDePagamento registroDePagamento) {
		escopoDoCondominio.exigirAcesso(
				cobrancaService.findCobranca(idCobrancaOrdinaria).getUnidade().getCondominio().getId());
		LocalDate dataPagamento = registroDePagamento == null ? null : registroDePagamento.getDataPagamento();
		return ResponseEntity
				.ok(cobrancaApiMapper.toApi(cobrancaService.registrarPagamento(idCobrancaOrdinaria, dataPagamento)));
	}

	@Override
	@PreAuthorize("hasAuthority('COBRANCA_ESCREVER')")
	public ResponseEntity<Void> gerarCobrancasOrdinarias(Long idCondominio) {
		Long escopo = escopoDoCondominio.restringir(idCondominio);
		if (escopo == null) {
			geradorDeCobrancaOrdinariaJob.gerar();
		} else {
			geradorDeCobrancaOrdinariaJob.gerarParaCondominio(escopo);
		}
		return ResponseEntity.noContent().build();
	}

}
