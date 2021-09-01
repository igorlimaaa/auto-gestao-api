package br.com.ialsolucoes.auto.gestao.dto;

import org.springframework.stereotype.Component;

import br.com.ialsolucoes.auto.gestao.enumeration.MeioContatoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MeioContatoDto {
	
	private Long id;
	private PessoaDto pessoa;
	private MeioContatoEnum tipoMeioContato;
	private String identificacao;

}
