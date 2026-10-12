package br.com.gestaocondial.autogestao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Boleto em PDF, buscado no banco no momento do pedido. */
@Getter
@AllArgsConstructor
public class BoletoPdfDto {

	private final byte[] conteudo;

	/** Ex.: {@code boleto-1203-2026-10.pdf}. */
	private final String nomeDoArquivo;

}
