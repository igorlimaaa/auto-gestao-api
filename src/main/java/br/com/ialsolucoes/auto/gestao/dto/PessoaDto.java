package br.com.ialsolucoes.auto.gestao.dto;

import org.hibernate.validator.constraints.br.CPF;
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
