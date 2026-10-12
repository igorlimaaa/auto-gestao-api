package br.com.gestaocondial.autogestao.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import br.com.gestaocondial.autogestao.banking.InterCobrancaClient;
import br.com.gestaocondial.autogestao.banking.InterSaldo;
import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.dto.SaldoBancarioDto;
import br.com.gestaocondial.autogestao.exception.ContaBancariaNaoConfiguradaException;
import br.com.gestaocondial.autogestao.service.SaldoService;

import lombok.RequiredArgsConstructor;

/** Sem {@code @Transactional} de propósito: a chamada HTTP ao banco não deve segurar conexão do pool. */
@Service
@RequiredArgsConstructor
public class SaldoImpl implements SaldoService {

	private static final Duration VALIDADE_DA_CONSULTA = Duration.ofMinutes(1);

	private final InterCobrancaClient banco;

	private final EscopoDoCondominio escopo;

	private final Map<Long, SaldoBancarioDto> consultas = new ConcurrentHashMap<>();

	@Override
	public SaldoBancarioDto consultarSaldo(Long idPedido) {
		Long idCondominio = idPedido != null ? idPedido : escopo.doPerfilAtivo();
		if (idCondominio == null) {
			throw new IllegalArgumentException("Informe o condomínio do saldo.");
		}
		escopo.exigirAcesso(idCondominio);

		SaldoBancarioDto recente = consultas.get(idCondominio);
		if (recente != null && recente.consultadoEm().plus(VALIDADE_DA_CONSULTA).isAfter(LocalDateTime.now())) {
			return recente;
		}

		if (!banco.integrado(idCondominio)) {
			throw new ContaBancariaNaoConfiguradaException(
					"Este condomínio ainda não tem a conta do Banco Inter configurada.");
		}
		InterSaldo saldo = banco.consultarSaldo(idCondominio);
		SaldoBancarioDto consulta = new SaldoBancarioDto(idCondominio, saldo.disponivel(), saldo.bloqueadoTotal(),
				saldo.limite(), LocalDateTime.now());
		consultas.put(idCondominio, consulta);
		return consulta;
	}

}
