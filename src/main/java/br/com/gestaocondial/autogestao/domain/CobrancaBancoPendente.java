package br.com.gestaocondial.autogestao.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.gestaocondial.autogestao.enumeration.MotivoDePendenciaEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cobrança do banco que a importação não conseguiu transformar em cobrança do sistema (V13). */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_cobranca_banco_pendente")
public class CobrancaBancoPendente {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id_cobranca_banco_pendente")
	private Long id;

	/** Referência ao condomínio; só o id, pois ninguém navega daqui até ele. */
	@Column(name = "id_condominio", nullable = false)
	private Long idCondominio;

	@Column(name = "ds_codigo_solicitacao", nullable = false, length = 40)
	private String codigoSolicitacao;

	@Column(name = "ds_seu_numero", length = 30)
	private String seuNumero;

	@Column(name = "dt_vencimento")
	private LocalDate dataVencimento;

	@Column(name = "vl_cobranca")
	private Double valor;

	@Enumerated(EnumType.STRING)
	@Column(name = "ds_motivo", nullable = false, length = 30)
	private MotivoDePendenciaEnum motivo;

	@Column(name = "dt_deteccao", nullable = false)
	private LocalDateTime dataDeteccao;

	@PrePersist
	protected void aoPersistir() {
		if (this.dataDeteccao == null) {
			this.dataDeteccao = LocalDateTime.now();
		}
	}

}
