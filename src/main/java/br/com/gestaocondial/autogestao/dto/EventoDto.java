package br.com.gestaocondial.autogestao.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;
import br.com.gestaocondial.autogestao.enumeration.TipoEventoEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventoDto {

	private Long id;

	private Long idCondominio;

	private Long idAreaComum;

	private String nomeDaAreaComum;

	@NotNull(message = "Informe o tipo do evento.")
	private TipoEventoEnum tipo;

	@NotBlank(message = "Informe o título do evento.")
	@Size(max = 100)
	private String titulo;

	@Size(max = 500)
	private String descricao;

	@NotNull(message = "Informe quando o evento começa.")
	private LocalDateTime inicio;

	@NotNull(message = "Informe quando o evento termina.")
	private LocalDateTime fim;

	private Boolean diaInteiro;

	private SituacaoEventoEnum situacao;

	private Long idUnidade;

	private String rotuloDaUnidade;

	private String solicitanteNome;

	private String solicitanteEmail;

	private String motivoDaDecisao;

	private LocalDateTime dataDaDecisao;

	private Boolean ehMeu;

	private Boolean podeEditar;

	private Boolean podeCancelar;

	private Boolean podeDecidir;

}
