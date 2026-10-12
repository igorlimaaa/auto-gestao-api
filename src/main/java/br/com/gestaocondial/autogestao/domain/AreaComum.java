package br.com.gestaocondial.autogestao.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Área comum do condomínio. O catálogo é do síndico: o morador nunca digita uma área, só escolhe
 * entre as {@link #reservavel reserváveis} e {@link #ativa ativas}.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_area_comum")
public class AreaComum {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_area_comum")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_condominio", nullable = false)
	private Condominio condominio;

	@Column(name = "ds_nome", nullable = false, length = 60)
	private String nome;

	@Column(name = "ds_descricao", length = 300)
	private String descricao;

	@Column(name = "in_reservavel", nullable = false)
	private Boolean reservavel = false;

	@Column(name = "nr_capacidade")
	private Integer capacidade;

	/** Início do horário em que a área pode ser reservada; vazio = sem limite. */
	@Column(name = "hr_abertura")
	private LocalTime horaAbertura;

	@Column(name = "hr_fechamento")
	private LocalTime horaFechamento;

	@Column(name = "in_ativa", nullable = false)
	private Boolean ativa = true;

	@Column(name = "dt_cadastro", nullable = false, updatable = false)
	private LocalDateTime dataCadastro;

	@PrePersist
	void aoPersistir() {
		this.dataCadastro = LocalDateTime.now();
		if (this.reservavel == null) {
			this.reservavel = false;
		}
		if (this.ativa == null) {
			this.ativa = true;
		}
	}

}
