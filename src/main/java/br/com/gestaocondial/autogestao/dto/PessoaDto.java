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
	/**
	 * Unidade onde a pessoa mora. Nulo é válido: o cadastro de morador e o de unidade são
	 * independentes, e o vínculo pode ser feito depois — ou nunca.
	 */
	private UnidadeDto unidade;
	private Boolean envioTaxaEmail;
	private Boolean envioImpresso;
	private Boolean isSindico;
	private CondominioDto condominio;
	private EnderecoDto endereco;

	@CPF
	private String cpf;
}
