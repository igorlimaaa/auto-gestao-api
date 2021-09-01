package br.com.ialsolucoes.auto.gestao.domain;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.com.ialsolucoes.auto.gestao.enumeration.MeioContatoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_meio_contato")
public class MeioContato {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_meio_contato")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="id_pessoa", nullable = false)
	private Pessoa pessoa;
	
	@Column(name = "cd_tipo_meio_contato", columnDefinition = "varchar(5)", nullable = false, length = 5)
	@Enumerated(EnumType.STRING)
	private MeioContatoEnum tipoMeioContato;
	
	@Column(name = "ds_identificacao")
	private String identificacao;

}
