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

	@Column(name = "ds_observacao", length = 300)
	private String observacao;

	/**
	 * Desconto sobre a taxa ordinária, de 0 a 100. 100 isenta a unidade (não há cobrança); 50 cobra
	 * metade. Decisão explícita por unidade — não é derivada de quem mora nela, para que a troca
	 * de síndico não mude a isenção sem ninguém decidir.
	 */
	@Column(name = "nr_percentual_desconto_taxa", nullable = false, precision = 5, scale = 2)
	private BigDecimal percentualDescontoTaxa;

	/** Por que a unidade tem desconto — ex.: "Unidade do síndico". */
	@Column(name = "ds_motivo_desconto_taxa", length = 100)
	private String motivoDescontoTaxa;

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
		if (this.percentualDescontoTaxa == null) {
			this.percentualDescontoTaxa = BigDecimal.ZERO;
		}
	}

	/** Desconto de 100%: a unidade não recebe cobrança da taxa ordinária. */
	public boolean isentaDaTaxaOrdinaria() {
		return percentualDescontoTaxa != null && percentualDescontoTaxa.compareTo(BigDecimal.valueOf(100)) >= 0;
	}

}
