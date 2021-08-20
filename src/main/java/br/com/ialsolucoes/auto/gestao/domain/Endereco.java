package br.com.ialsolucoes.auto.gestao.domain;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tb_endereco")
public class Endereco {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private Long nr_cep;
	private String ds_endereco;
	private String ds_complemento;

}
