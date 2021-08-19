package br.com.ialsolucoes.auto.gestao.dto;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class CondominioDto {
	
	private Long id;
	private Integer ddd;
	private Long numeroTelefone;
	private Double valorTaxa;

}
