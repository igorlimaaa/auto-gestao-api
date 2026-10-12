package br.com.gestaocondial.autogestao.banking;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resposta de {@code GET /banking/v2/saldo}. Os bloqueios podem vir ausentes, e valem zero nesse caso.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InterSaldo(BigDecimal disponivel, BigDecimal bloqueadoCheque, BigDecimal bloqueadoJudicialmente,
		BigDecimal bloqueadoAdministrativo, BigDecimal limite) {

	/** Soma dos três tipos de bloqueio que o banco informa. */
	public BigDecimal bloqueadoTotal() {
		return zeroSeNulo(bloqueadoCheque).add(zeroSeNulo(bloqueadoJudicialmente))
				.add(zeroSeNulo(bloqueadoAdministrativo));
	}

	private static BigDecimal zeroSeNulo(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}

}
