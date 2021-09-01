package br.com.ialsolucoes.auto.gestao.dto;

import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class TaxaExtraDto {
	
	private Long id;
	private CondominioDto condominio;
	private Double valorTaxaExtra;
	private Integer numeroParcelas;
	private String descricaoTaxa;
	private Date dataCadastro;

}
