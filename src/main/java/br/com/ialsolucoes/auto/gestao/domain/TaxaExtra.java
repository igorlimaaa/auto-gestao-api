package br.com.ialsolucoes.auto.gestao.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_taxa_extra")
public class TaxaExtra {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_taxa_extra")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="id_condominio", nullable = false)
	private Condominio condominio;
	
	@Column(name = "vl_taxa_extra")
	private Double valorTaxaExtra;
	
	@Column(name = "nr_parcelas")
	private Integer numeroParcelas;
	
	@Column(name = "ds_taxa_extra")
	private String descricaoTaxa;
	
	@Column(name = "dt_cadastro")
	private Date dataCadastro;

}
