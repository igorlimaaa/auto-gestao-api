package br.com.gestaocondial.autogestao.dto;

import java.time.LocalDate;

import br.com.gestaocondial.autogestao.enumeration.SituacaoCobrancaEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CobrancaOrdinariaDto {

	private Long id;
	private UnidadeDto unidade;
	private LocalDate competencia;
	private LocalDate dataVencimento;
	private Double valor;
	private String pixCopiaCola;
	private String linhaDigitavel;
	private LocalDate dataPagamento;

	/** Calculada pelo service a partir de {@code dataPagamento}/{@code dataVencimento}. */
	private SituacaoCobrancaEnum situacao;

}
