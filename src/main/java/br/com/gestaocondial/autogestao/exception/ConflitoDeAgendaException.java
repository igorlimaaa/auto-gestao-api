package br.com.gestaocondial.autogestao.exception;

/** A área já está ocupada (reserva ou manutenção aprovada) no horário pedido. */
public class ConflitoDeAgendaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConflitoDeAgendaException(String message) {
		super(message);
	}

}
