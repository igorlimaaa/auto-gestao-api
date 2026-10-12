package br.com.gestaocondial.autogestao.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnidadeDto {

	private Long id;

	@NotNull(message = "Informe o condomínio da unidade.")
	private CondominioDto condominio;

	@Size(max = 30)
	private String bloco;

	@NotBlank(message = "Informe o número ou a identificação da unidade.")
	@Size(max = 20)
	private String numero;

	private Integer andar;

	private BigDecimal fracaoIdeal;

	@Size(max = 300)
	private String observacao;

	/** De 0 a 100; 100 isenta a unidade da taxa ordinária. Ausente mantém o valor atual. */
	@DecimalMin(value = "0", message = "O desconto não pode ser negativo.")
	@DecimalMax(value = "100", message = "O desconto não pode passar de 100%.")
	private BigDecimal percentualDescontoTaxa;

	@Size(max = 100)
	private String motivoDescontoTaxa;

	private Boolean ativa;

	/** Calculado na consulta; zero é condição para poder excluir a unidade. */
	private Integer quantidadeDeMoradores;

}
