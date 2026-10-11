package br.com.gestaocondial.autogestao.banking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cobrança como o Inter devolve, na listagem e no detalhe (mesmo envelope nos dois). Só os
 * campos usados — o pagador fica de fora de propósito: já temos isso em {@code Pessoa}, e uma
 * cópia aqui nasceria desatualizada. {@code valorNominal} chega como string na listagem e como
 * número no detalhe; {@link BigDecimal} aceita os dois.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InterCobranca(Cobranca cobranca, Boleto boleto, Pix pix) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Cobranca(String codigoSolicitacao, String seuNumero, String situacao, LocalDate dataSituacao,
			LocalDate dataEmissao, LocalDate dataVencimento, BigDecimal valorNominal,
			BigDecimal valorTotalRecebido, String origemRecebimento) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Boleto(String nossoNumero, String linhaDigitavel, String codigoBarras) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Pix(String txid, String pixCopiaECola) {
	}

	/** Uma página da listagem. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Pagina(Integer totalPaginas, Boolean ultimaPagina, List<InterCobranca> cobrancas) {

		public List<InterCobranca> cobrancasOuVazio() {
			return cobrancas == null ? List.of() : cobrancas;
		}

		public boolean ehUltima() {
			return Boolean.TRUE.equals(ultimaPagina) || cobrancas == null || cobrancas.isEmpty();
		}
	}

}
