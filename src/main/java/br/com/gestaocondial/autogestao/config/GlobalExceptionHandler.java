package br.com.gestaocondial.autogestao.config;

import br.com.gestaocondial.autogestao.banking.InterApiException;
import br.com.gestaocondial.autogestao.exception.BoletoIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.gestaocondial.autogestao.dto.ListaDeErrosOutputDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import br.com.gestaocondial.autogestao.exception.AcessoNegadoException;
import br.com.gestaocondial.autogestao.exception.AreaComumJaExisteException;
import br.com.gestaocondial.autogestao.exception.AreaComumNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.ConflitoDeAgendaException;
import br.com.gestaocondial.autogestao.exception.ContaBancariaNaoConfiguradaException;
import br.com.gestaocondial.autogestao.exception.EventoNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.SituacaoDoEventoInvalidaException;
import br.com.gestaocondial.autogestao.exception.CobrancaOrdinariaNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.CondominioNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.EnderecoNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.PessoaNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.RecursoEmUsoException;
import br.com.gestaocondial.autogestao.exception.TaxaExtraNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.UnidadeJaExisteException;
import br.com.gestaocondial.autogestao.exception.UnidadeNaoEncontradaException;

import lombok.extern.slf4j.Slf4j;

/**
 * Tratamento centralizado de exceções da API. Converte falhas de bean
 * validation (@Valid nos endpoints gerados a partir do contrato OpenAPI) e as
 * exceptions de negócio conhecidas em respostas padronizadas usando
 * {@link ListaDeErrosOutputDto}.
 */
@Slf4j
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

	@ExceptionHandler({ CondominioNaoEncontradoException.class, UnidadeNaoEncontradaException.class,
			PessoaNaoEncontradaException.class, TaxaExtraNaoEncontradaException.class,
			CobrancaOrdinariaNaoEncontradaException.class, AreaComumNaoEncontradaException.class,
			EventoNaoEncontradoException.class })
	public ResponseEntity<ListaDeErrosOutputDto> handleNaoEncontrado(RuntimeException ex) {
		return resposta(null, ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	/**
	 * Validacao disparada na camada de service (metodos anotados com {@code @Valid} em classes
	 * {@code @Validated}). Sem este handler ela sai como 500, e nao como o 400 que a tela usa
	 * para marcar o campo errado.
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleConstraintViolation(ConstraintViolationException ex) {
		ListaDeErrosOutputDto listaDeErros = new ListaDeErrosOutputDto();
		for (ConstraintViolation<?> violacao : ex.getConstraintViolations()) {
			// O propertyPath vem como "createUnidade.unidade.numero"; a tela so precisa do campo.
			String caminho = violacao.getPropertyPath().toString();
			String campo = caminho.substring(caminho.lastIndexOf('.') + 1);
			listaDeErros.adicionaErroEmParametro(campo, violacao.getMessage());
		}
		return new ResponseEntity<>(listaDeErros, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UnidadeJaExisteException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleUnidadeJaExiste(UnidadeJaExisteException ex) {
		return resposta("numero", ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(AreaComumJaExisteException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleAreaComumJaExiste(AreaComumJaExisteException ex) {
		return resposta("nome", ex.getMessage(), HttpStatus.CONFLICT);
	}

	/** Área ocupada no horário, ou operação que não cabe na situação atual do evento. */
	@ExceptionHandler({ ConflitoDeAgendaException.class, SituacaoDoEventoInvalidaException.class,
			ContaBancariaNaoConfiguradaException.class })
	public ResponseEntity<ListaDeErrosOutputDto> handleConflitoDeAgenda(RuntimeException ex) {
		return resposta(null, ex.getMessage(), HttpStatus.CONFLICT);
	}

	/** Exclusao recusada porque o registro ainda esta vinculado a outros. */
	@ExceptionHandler(RecursoEmUsoException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleRecursoEmUso(RecursoEmUsoException ex) {
		return resposta(null, ex.getMessage(), HttpStatus.CONFLICT);
	}

	/**
	 * 403, nao 401: a sessao e valida e tem a permissao: o que falta e alcance sobre aquele
	 * condominio. Devolver 401 aqui faria o frontend mandar o usuario refazer login a toa.
	 */
	@ExceptionHandler(AcessoNegadoException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleAcessoNegado(AcessoNegadoException ex) {
		return resposta(null, ex.getMessage(), HttpStatus.FORBIDDEN);
	}

	/** {@code @PreAuthorize} negado. Sem este handler a resposta sairia como 500. */
	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleAutorizacaoNegada(AuthorizationDeniedException ex) {
		return resposta(null, "O perfil ativo desta sessao nao tem permissao para esta operacao.",
				HttpStatus.FORBIDDEN);
	}

	/** Cobrança sem boleto a mostrar (sem vínculo com o banco, ou já paga/cancelada). */
	@ExceptionHandler(BoletoIndisponivelException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleBoletoIndisponivel(BoletoIndisponivelException ex) {
		return resposta(null, ex.getMessage(), HttpStatus.CONFLICT);
	}

	/**
	 * Falha ao falar com o Banco Inter. 502, e não 500: o erro não é nosso, e a mensagem é fixa de
	 * propósito — a causa (certificado, rede, token) fica no log, nunca na resposta.
	 */
	@ExceptionHandler(InterApiException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleInterApi(InterApiException ex) {
		log.error("Falha no Banco Inter: {}", ex.getMessage(), ex);
		if (ex.getStatusHttp() != null && ex.getStatusHttp() == 404) {
			return resposta(null, "Cobrança não encontrada no banco.", HttpStatus.NOT_FOUND);
		}
		return resposta(null, "Não foi possível consultar o banco agora. Tente novamente em instantes.",
				HttpStatus.BAD_GATEWAY);
	}

	/** Regra de coerencia do proprio payload (ex.: unidade sem condominio). */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleArgumentoInvalido(IllegalArgumentException ex) {
		return resposta(null, ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<ListaDeErrosOutputDto> resposta(String parametro, String mensagem, HttpStatus status) {
		ListaDeErrosOutputDto listaDeErros = new ListaDeErrosOutputDto();
		listaDeErros.adicionaErroEmParametro(parametro, mensagem);
		return new ResponseEntity<>(listaDeErros, status);
	}

	@ExceptionHandler(EnderecoNaoEncontradoException.class)
	public ResponseEntity<ListaDeErrosOutputDto> handleEnderecoNaoEncontrado(EnderecoNaoEncontradoException ex) {
		ListaDeErrosOutputDto listaDeErros = new ListaDeErrosOutputDto();
		listaDeErros.adicionaErroEmParametro("endereco", ex.getMessage());
		return new ResponseEntity<>(listaDeErros, HttpStatus.NOT_FOUND);
	}

}
