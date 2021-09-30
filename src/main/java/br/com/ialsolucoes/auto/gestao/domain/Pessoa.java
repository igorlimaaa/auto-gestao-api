package br.com.ialsolucoes.auto.gestao.domain;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
@Table(name = "tb_pessoa")
public class Pessoa {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_pessoa")
	private Long id;
	
	@Column(name = "ds_pessoa")
	private String nomeCompleto;
	
	@Column(name = "nr_unidade")
	private Long numeroUnidade;
	
	@Column(name = "nr_cpf")
	private String cpf;
	
	@Column(name = "in_envio_email", columnDefinition="boolean default false")
	private Boolean envioTaxaEmail;
	
	@Column(name = "in_envio_impresso", columnDefinition="boolean default false")
	private Boolean envioImpresso;
	
	@Column(name = "in_sindico", columnDefinition="boolean default false")
	private Boolean isSindico;
	
	@OneToOne
	@JoinColumn(name="id_condominio", nullable = false)
	private Condominio condominio;
	
	@OneToOne
	@JoinColumn(name="id_endereco", nullable = false)
	private Endereco endereco;
	
	
	
	
	

}
