package br.com.gestaocondial.autogestao.enumeration;

/** O que é o evento na agenda do condomínio. */
public enum TipoEventoEnum {

	REUNIAO,

	/** Uso exclusivo de uma área comum por uma unidade (salão de festas, churrasqueira...). */
	RESERVA_AREA,

	/** Confraternização do condomínio — Dia das Crianças, festa junina. */
	SOCIAL,

	/** Serviço ou vistoria; quando ligada a uma área, fecha a área para reservas nesse horário. */
	MANUTENCAO,

	OUTRO;

	/** Tipos que ocupam a área: dois deles, aprovados, não podem se sobrepor na mesma área. */
	public boolean ocupaArea() {
		return this == RESERVA_AREA || this == MANUTENCAO;
	}

}
