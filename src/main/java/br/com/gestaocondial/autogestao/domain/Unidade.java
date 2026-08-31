package br.com.gestaocondial.autogestao.domain;

import java.math.BigDecimal;

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
 * Unidade autônoma de um condomínio — apartamento, sala, loja.
 *
 * <p>Existe por si só: uma unidade vazia é um cadastro válido, e o morador é ligado a ela
 * depois (ou nunca). Ver {@link Pessoa#getUnidade()}.</p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_unidade")
public class Unidade {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_unidade")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_condominio", nullable = false)
	private Condominio condominio;

	/** Vazio, e nao nulo, quando o condominio nao usa bloco — ver a migration V3. */
	@Column(name = "ds_bloco", nullable = false, length = 30)
	private String bloco;

	/** Texto, não número: "101", "A-3" e "Loja 2" são identificações válidas. */
	@Column(name = "nr_unidade", nullable = false, length = 20)
	private String numero;

	@Column(name = "nr_andar")
	private Integer andar;

	/** Participação da unidade no rateio das despesas do condomínio. */
	@Column(name = "vl_fracao_ideal", precision = 10, scale = 6)
	private BigDecimal fracaoIdeal;

	@Column(name = "ds_observacao", length = 300)
	private String observacao;

	@Column(name = "in_ativa", nullable = false)
	private Boolean ativa;

	@PrePersist
	protected void aoPersistir() {
		if (this.ativa == null) {
			this.ativa = true;
		}
		if (this.bloco == null) {
			this.bloco = "";
		}
	}

}
