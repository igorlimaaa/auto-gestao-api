package br.com.gestaocondial.autogestao.dto;

import org.hibernate.validator.constraints.br.CPF;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PessoaDto {

	private Long id;
	private String nomeCompleto;
	private Long numeroUnidade;
	private Boolean envioTaxaEmail;
	private Boolean envioImpresso;
	private Boolean isSindico;
	private CondominioDto condominio;
	private EnderecoDto endereco;

	@CPF
	private String cpf;
}
