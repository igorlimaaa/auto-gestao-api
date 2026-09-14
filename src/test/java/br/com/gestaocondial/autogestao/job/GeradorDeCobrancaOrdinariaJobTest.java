package br.com.gestaocondial.autogestao.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

/**
 * Cobre a aritmética de datas do job — clamp do dia de vencimento ao tamanho do mês-alvo, e o
 * cálculo da competência vigente — a parte mais fácil de errar sutilmente. O restante da regra
 * (geração em si, idempotência) depende de repositórios e é melhor coberto por um teste de
 * integração, fora do escopo desta rodada.
 */
class GeradorDeCobrancaOrdinariaJobTest {

	@Test
	void mantemODiaQuandoOMesTemDiasSuficientes() {
		assertEquals(LocalDate.of(2026, 1, 10),
				GeradorDeCobrancaOrdinariaJob.diaNoMes(10, YearMonth.of(2026, 1)));
	}

	@Test
	void clampaParaOUltimoDiaEmFevereiroNaoBissexto() {
		assertEquals(LocalDate.of(2026, 2, 28),
				GeradorDeCobrancaOrdinariaJob.diaNoMes(31, YearMonth.of(2026, 2)));
	}

	@Test
	void clampaParaOUltimoDiaEmFevereiroBissexto() {
		assertEquals(LocalDate.of(2028, 2, 29),
				GeradorDeCobrancaOrdinariaJob.diaNoMes(31, YearMonth.of(2028, 2)));
	}

	@Test
	void clampaParaOUltimoDiaEmMesDeTrinta() {
		assertEquals(LocalDate.of(2026, 4, 30),
				GeradorDeCobrancaOrdinariaJob.diaNoMes(31, YearMonth.of(2026, 4)));
	}

	@Test
	void competenciaVigenteEhOMesAtualAntesDoGatilho() {
		// Vencimento dia 10, hoje dia 15: gatilho (10+10=20) ainda não chegou.
		assertEquals(YearMonth.of(2026, 9),
				GeradorDeCobrancaOrdinariaJob.competenciaVigente(10, LocalDate.of(2026, 9, 15)));
	}

	@Test
	void competenciaVigenteRolaParaOMesSeguinteExatamenteNoDiaDoGatilho() {
		// Vencimento dia 10 -> gatilho no dia 20: já é o mês seguinte a partir desse dia, não só depois dele.
		assertEquals(YearMonth.of(2026, 10),
				GeradorDeCobrancaOrdinariaJob.competenciaVigente(10, LocalDate.of(2026, 9, 20)));
	}

	@Test
	void competenciaVigenteNaoRetrocedeQuandoOCondominioEhConfiguradoAposOGatilhoJaTerPassado() {
		// Caso relatado: vencimento dia 3, condomínio configurado (ou job rodado pela 1a vez) no
		// dia 14 -- o gatilho de setembro (3+10=13) já passou, então a competência vigente já é
		// outubro, nunca a retroativa de setembro.
		assertEquals(YearMonth.of(2026, 10),
				GeradorDeCobrancaOrdinariaJob.competenciaVigente(3, LocalDate.of(2026, 9, 14)));
	}

	@Test
	void competenciaVigenteAvancaVariosMesesSeVariosGatilhosJaTiveremPassado() {
		// Vencimento dia 1: em 1o de dezembro, os gatilhos de setembro (11/09), outubro (11/10) e
		// novembro (11/11) já passaram -- a competência vigente é dezembro, não setembro.
		assertEquals(YearMonth.of(2026, 12),
				GeradorDeCobrancaOrdinariaJob.competenciaVigente(1, LocalDate.of(2026, 12, 1)));
	}

}
