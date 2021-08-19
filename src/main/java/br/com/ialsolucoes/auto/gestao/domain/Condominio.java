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
@Table(name = "tb_condominio")
public class Condominio {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private Long nr_ddd;
	private Long nr_telefone;
	private Double vl_taxa;
	

}
