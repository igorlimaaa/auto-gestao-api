package br.com.gestaocondial.autogestao.dto;

import java.time.LocalDate;

import br.com.gestaocondial.autogestao.enumeration.MotivoDePendenciaEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CobrancaBancoPendenteDto {

	private Long idCondominio;
	private String codigoSolicitacao;
	private String seuNumero;
	private LocalDate dataVencimento;
	private Double valor;
	private MotivoDePendenciaEnum motivo;

}
