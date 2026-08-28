package br.com.gestaocondial.autogestao.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
@Table(name = "TB_ENDERECO")
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
