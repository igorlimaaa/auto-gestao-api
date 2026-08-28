package br.com.gestaocondial.autogestao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnderecoDto {

	private Long id;
	private Long cep;
	private String endereco;
	private String complemento;

}
