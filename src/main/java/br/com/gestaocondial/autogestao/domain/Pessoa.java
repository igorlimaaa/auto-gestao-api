package br.com.gestaocondial.autogestao.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_pessoa")
public class Pessoa {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_pessoa")
	private Long id;
	
	@Column(name = "ds_pessoa")
	private String nomeCompleto;
	
	
	@Column(name = "nr_cpf")
	private String cpf;
	
	@Column(name = "in_envio_email", columnDefinition="boolean default false")
	private Boolean envioTaxaEmail;
	
	@Column(name = "in_envio_impresso", columnDefinition="boolean default false")
	private Boolean envioImpresso;
	
	@Column(name = "in_sindico", columnDefinition="boolean default false")
	private Boolean isSindico;
	
	/** ManyToOne, nao OneToOne: um condominio tem muitos moradores. Ver migration V4. */
	@ManyToOne(optional = false)
	@JoinColumn(name="id_condominio", nullable = false)
	private Condominio condominio;
	
	/**
	 * Endereco proprio da pessoa. Opcional — exigir um endereco completo para cadastrar quem
	 * mora no condominio contraria o cadastro independente de morador e unidade.
	 */
	@ManyToOne
	@JoinColumn(name="id_endereco")
	private Endereco endereco;

	/**
	 * Unidade onde a pessoa mora. Opcional nos dois sentidos: uma unidade pode existir sem
	 * morador e uma pessoa pode ser cadastrada antes de se saber onde ela vai morar. A FK
	 * garante apenas que, havendo vínculo, a unidade exista de fato.
	 */
	@ManyToOne
	@JoinColumn(name = "id_unidade")
	private Unidade unidade;
	
	
	
	
	

}
