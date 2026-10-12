package br.com.gestaocondial.autogestao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Saldo da conta-corrente do condomínio.
 *
 * @param disponivel quanto pode ser movimentado agora
 * @param bloqueado soma dos bloqueios (cheque, judicial, administrativo)
 * @param limite limite da conta
 * @param consultadoEm quando o banco foi consultado (o saldo vale para esse instante)
 */
public record SaldoBancarioDto(Long idCondominio, BigDecimal disponivel, BigDecimal bloqueado, BigDecimal limite,
		LocalDateTime consultadoEm) {

}
