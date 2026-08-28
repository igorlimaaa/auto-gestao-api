package br.com.gestaocondial.autogestao.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxaExtraDto {

	private Long id;
	private CondominioDto condominio;
	private Double valorTaxaExtra;
	private Integer numeroParcelas;
	private String descricaoTaxa;
	private Date dataCadastro;

}
