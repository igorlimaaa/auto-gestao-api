package br.com.gestaocondial.autogestao.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AreaComumDto {

	private Long id;

	/** Ausente na criação: vale o condomínio do perfil ativo. */
	private Long idCondominio;

	@NotBlank(message = "Informe o nome da área.")
	@Size(max = 60)
	private String nome;

	@Size(max = 300)
	private String descricao;

	private Boolean reservavel;

	@Min(value = 1, message = "A capacidade deve ser de pelo menos 1 pessoa.")
	private Integer capacidade;

	private LocalTime horaAbertura;

	private LocalTime horaFechamento;

	private Boolean ativa;

}
