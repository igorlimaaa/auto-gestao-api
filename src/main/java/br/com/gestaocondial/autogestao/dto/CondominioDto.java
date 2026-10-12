package br.com.gestaocondial.autogestao.dto;

import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CondominioDto {

	private Long id;
	private Integer ddd;
	private Long numeroTelefone;
	private EnderecoDto endereco;
	private Double valorJuros;
	private Double valorMulta;
	private Boolean possuiTaxaExtra = false;
	private Double valorTaxaCondominial;
	private Integer diaVencimentoTaxaOrdinaria;

	@Min(value = 1, message = "A quantidade de unidades deve ser de pelo menos 1.")
	private Integer quantidadeUnidades;

	private Double valorTaxasExtras;
	private Double valorTotalPorUnidade;

}
