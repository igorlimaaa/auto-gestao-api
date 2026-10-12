package br.com.gestaocondial.autogestao.impl;

import org.springframework.stereotype.Service;

import br.com.gestaocondial.autogestao.banking.InterCobrancaClient;
import br.com.gestaocondial.autogestao.config.AcessoAoBoleto;
import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.dto.BoletoPdfDto;
import br.com.gestaocondial.autogestao.enumeration.SituacaoCobrancaEnum;
import br.com.gestaocondial.autogestao.exception.AcessoNegadoException;
import br.com.gestaocondial.autogestao.exception.BoletoIndisponivelException;
import br.com.gestaocondial.autogestao.exception.CobrancaOrdinariaNaoEncontradaException;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.service.BoletoService;

import lombok.RequiredArgsConstructor;

/** Sem {@code @Transactional} de propósito: a chamada HTTP ao banco não deve segurar conexão do pool. */
@Service
@RequiredArgsConstructor
public class BoletoImpl implements BoletoService {

	private final CobrancaOrdinariaRepository cobrancaRepository;

	private final InterCobrancaClient banco;

	private final AcessoAoBoleto acessoAoBoleto;

	private final EscopoDoCondominio escopoDoCondominio;

	@Override
	public BoletoPdfDto obterPdf(Long idCobrancaOrdinaria) {
		CobrancaOrdinaria cobranca = cobrancaRepository.findById(idCobrancaOrdinaria)
				.orElseThrow(() -> new CobrancaOrdinariaNaoEncontradaException(
						"Cobrança " + idCobrancaOrdinaria + " não encontrada."));
		Unidade unidade = cobranca.getUnidade();

		escopoDoCondominio.exigirAcesso(unidade.getCondominio().getId());
		if (!acessoAoBoleto.alcance().podeVer(unidade.getId())) {
			throw new AcessoNegadoException("Você só pode ver o boleto da sua própria unidade.");
		}
		if (cobranca.getCodigoSolicitacao() == null) {
			throw new BoletoIndisponivelException("Esta cobrança não tem boleto emitido no banco.");
		}
		SituacaoCobrancaEnum situacao = CobrancaOrdinariaImpl.calcularSituacao(cobranca);
		if (!CobrancaOrdinariaImpl.boletoDisponivel(cobranca, situacao)) {
			throw new BoletoIndisponivelException("O boleto só está disponível para cobranças em aberto.");
		}

		byte[] pdf = banco.baixarPdf(unidade.getCondominio().getId(), cobranca.getCodigoSolicitacao());
		return new BoletoPdfDto(pdf, nomeDoArquivo(cobranca));
	}

	private static String nomeDoArquivo(CobrancaOrdinaria cobranca) {
		String unidade = cobranca.getUnidade().getNumero().replaceAll("[^A-Za-z0-9]", "");
		return "boleto-" + unidade + "-" + String.format("%04d-%02d", cobranca.getCompetencia().getYear(),
				cobranca.getCompetencia().getMonthValue()) + ".pdf";
	}

}
