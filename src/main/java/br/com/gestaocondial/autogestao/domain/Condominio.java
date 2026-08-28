package br.com.gestaocondial.autogestao.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_condominio")
public class Condominio {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_condominio")
	private Long id;
	
	@Column(name = "nr_ddd")
	private Long ddd;
	
	@Column(name = "nr_telefone")
	private Long numeroTelefone;
	
	@Column(name = "vl_taxa")
	private Double valorTaxaCondominial;
	
	@OneToOne
	@JoinColumn(name="id_endereco", nullable = false)
	private Endereco endereco;
	
	@Column(name = "vl_juros")
	private Double valorJuros;
	
	@Column(name = "vl_multa")
	private Double valorMulta;
	
	@Column(name = "in_taxa_extra", nullable = false, columnDefinition="boolean default false")
	@NotNull
	private Boolean possuiTaxaExtra;
	

}
