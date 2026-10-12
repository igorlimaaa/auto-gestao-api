package br.com.gestaocondial.autogestao.enumeration;

/** Como uma área comum é reservada. */
public enum ModoDeReservaEnum {

	/** Faixa de horário dentro do funcionamento da área; várias reservas no mesmo dia, se não se cruzarem. */
	POR_HORARIO,

	/** Dia inteiro, uso exclusivo: uma reserva por dia, porque a área ainda precisa de limpeza e arrumação. */
	DIA_INTEIRO

}
