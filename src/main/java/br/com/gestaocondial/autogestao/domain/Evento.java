package br.com.gestaocondial.autogestao.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import br.com.gestaocondial.autogestao.enumeration.SituacaoEventoEnum;
import br.com.gestaocondial.autogestao.enumeration.TipoEventoEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Item da agenda do condomínio. {@link #fim} é exclusivo (convenção do calendário); em dia inteiro
 * é a meia-noite do dia seguinte. Quem cadastrou fica registrado por e-mail e nome (retrato do
 * momento), porque o cadastro do morador pode mudar depois.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_evento")
public class Evento {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_evento")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_condominio", nullable = false)
	private Condominio condominio;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_area_comum")
	private AreaComum areaComum;

	@Enumerated(EnumType.STRING)
	@Column(name = "tp_evento", nullable = false, length = 20)
	private TipoEventoEnum tipo;

	@Column(name = "ds_titulo", nullable = false, length = 100)
	private String titulo;

	@Column(name = "ds_descricao", length = 500)
	private String descricao;

	@Column(name = "dt_inicio", nullable = false)
	private LocalDateTime inicio;

	@Column(name = "dt_fim", nullable = false)
	private LocalDateTime fim;

	@Column(name = "in_dia_inteiro", nullable = false)
	private Boolean diaInteiro = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "st_evento", nullable = false, length = 20)
	private SituacaoEventoEnum situacao;

	/** Unidade de quem cadastrou, quando é morador. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unidade")
	private Unidade unidade;

	@Column(name = "ds_solicitante_email", length = 200)
	private String solicitanteEmail;

	@Column(name = "ds_solicitante_nome", length = 200)
	private String solicitanteNome;

	@Column(name = "ds_motivo_decisao", length = 300)
	private String motivoDecisao;

	@Column(name = "dt_decisao")
	private LocalDateTime dataDecisao;

	@Column(name = "ds_decidido_por", length = 200)
	private String decididoPor;

	@Column(name = "dt_cadastro", nullable = false, updatable = false)
	private LocalDateTime dataCadastro;

	@PrePersist
	void aoPersistir() {
		this.dataCadastro = LocalDateTime.now();
		if (this.diaInteiro == null) {
			this.diaInteiro = false;
		}
	}

}
