package br.com.gestaocondial.autogestao.dto;

import br.com.gestaocondial.autogestao.enumeration.MeioContatoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MeioContatoDto {

	private Long id;
	private PessoaDto pessoa;
	private MeioContatoEnum tipoMeioContato;
	private String identificacao;

}
