package br.com.gestaocondial.autogestao.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.gestaocondial.autogestao.dto.ListaDeErrosOutputDto;
import br.com.gestaocondial.autogestao.exception.EnderecoNaoEncontradoException;

/**
 * Tratamento centralizado de exceções da API. Converte falhas de bean
 * validation (@Valid nos endpoints gerados a partir do contrato OpenAPI) e as
 * exceptions de negócio conhecidas em respostas padronizadas usando
 * {@link ListaDeErrosOutputDto}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleValidationErrors(MethodArgumentNotValidException ex) {
		ListaDeErrosOutputDto listaDeErros = new ListaDeErrosOutputDto();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			listaDeErros.adicionaErroEmParametro(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return new ResponseEntity<>(listaDeErros, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(EnderecoNaoEncontradoException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleEnderecoNaoEncontrado(EnderecoNaoEncontradoException ex) {
		ListaDeErrosOutputDto listaDeErros = new ListaDeErrosOutputDto();
		listaDeErros.adicionaErroEmParametro("endereco", ex.getMessage());
		return new ResponseEntity<>(listaDeErros, HttpStatus.NOT_FOUND);
	}

}
