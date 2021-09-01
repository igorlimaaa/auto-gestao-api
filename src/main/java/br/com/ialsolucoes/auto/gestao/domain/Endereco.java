package br.com.ialsolucoes.auto.gestao.domain;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
@Table(name = "tb_endereco")
public class Endereco {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_endereco")
	private Long id;
	
	@Column(name = "nr_cep")
	private Long cep;
	
	@Column(name = "ds_endereco")
	private String endereco;
	
	@Column(name = "ds_complemento")
	private String complemento;

}
