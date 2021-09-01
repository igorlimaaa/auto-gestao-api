package br.com.ialsolucoes.auto.gestao.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum MeioContatoEnum {
	
	EMAIL("EMAIL"),
	TELEF("TELEFONE");
	
	private String label;

}
