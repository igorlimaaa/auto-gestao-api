package br.com.ialsolucoes.auto.gestao.dto;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Component
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
	private Double valorTaxasExtras;
	private Double valorTotalPorUnidade;

}
