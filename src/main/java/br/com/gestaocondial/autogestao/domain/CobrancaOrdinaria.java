package br.com.gestaocondial.autogestao.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Cobrança mensal da taxa ordinária de uma {@link Unidade} — nunca de uma {@link Pessoa}, para
 * que a troca de morador não afete o histórico de pagamento. Nasce pelo job agendado
 * ({@code GeradorDeCobrancaOrdinariaJob}); não há criação nem exclusão manual.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_cobranca_ordinaria")
public class CobrancaOrdinaria {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_cobranca_ordinaria")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_unidade", nullable = false)
	private Unidade unidade;

	/** Sempre o dia 1 do mês a que a cobrança se refere. */
	@Column(name = "dt_competencia", nullable = false)
	private LocalDate competencia;

	@Column(name = "dt_vencimento", nullable = false)
	private LocalDate dataVencimento;

	/** Snapshot de {@code condominio.valorTaxaCondominial} no momento da geração. */
	@Column(name = "vl_cobranca")
	private Double valor;

	@Column(name = "ds_pix_copia_cola", columnDefinition = "text")
	private String pixCopiaCola;

	@Column(name = "ds_linha_digitavel", length = 100)
	private String linhaDigitavel;

	@Column(name = "dt_pagamento")
	private LocalDate dataPagamento;

	@Column(name = "dt_cadastro", nullable = false)
	private LocalDateTime dataCadastro;

	@PrePersist
	protected void aoPersistir() {
		if (this.dataCadastro == null) {
			this.dataCadastro = LocalDateTime.now();
		}
	}

}
