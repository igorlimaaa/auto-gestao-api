package br.com.ialsolucoes.auto.gestao.dto;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class EnderecoDto {

	private Long id;
	private Long cep;
	private String endereco;
	private String complemento;
	
}
