package br.com.gestaocondial.autogestao.banking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Chama o Banco Inter DE VERDADE (somente GET). Só roda com {@code INTER_SMOKE_DIR} apontando
 * para uma pasta de credenciais no layout de {@link CredenciaisInterProvider}, com o id do
 * condomínio em {@code INTER_SMOKE_ID_CONDOMINIO} (padrão 1). Imprime só contagens e situações —
 * nunca dados de pagadores.
 */
@EnabledIfEnvironmentVariable(named = "INTER_SMOKE_DIR", matches = ".+")
class InterCobrancaClientSmokeTest {

	@Test
	void listaERecuperaCobrancasReais() {
		Long idCondominio = Long.valueOf(System.getenv().getOrDefault("INTER_SMOKE_ID_CONDOMINIO", "1"));
		InterProperties props = new InterProperties("https://cdpj.partners.bancointer.com.br",
				System.getenv("INTER_SMOKE_DIR"), "boleto-cobranca.read extrato.read rec.read");
		InterCobrancaClient cliente = new InterCobrancaClient(props, new CredenciaisInterProvider(props));

		LocalDate hoje = LocalDate.now();
		List<InterCobranca> lista = cliente.listar(idCondominio, hoje.minusDays(30), hoje.plusDays(60));
		Map<String, Long> porSituacao = lista.stream()
				.collect(Collectors.groupingBy(c -> c.cobranca().situacao(), Collectors.counting()));
		System.out.println("SMOKE listagem: " + lista.size() + " cobranca(s) " + porSituacao);
		assertFalse(lista.isEmpty());

		InterCobranca primeira = lista.get(0);
		InterCobranca detalhe = cliente.recuperar(idCondominio, primeira.cobranca().codigoSolicitacao());
		System.out.println("SMOKE detalhe: situacao=" + detalhe.cobranca().situacao() + " seuNumero="
				+ detalhe.cobranca().seuNumero() + " valor=" + detalhe.cobranca().valorNominal()
				+ " temPix=" + (detalhe.pix() != null && detalhe.pix().pixCopiaECola() != null)
				+ " temLinha=" + (detalhe.boleto() != null && detalhe.boleto().linhaDigitavel() != null));
		assertNotNull(detalhe.cobranca().codigoSolicitacao());
	}

}
