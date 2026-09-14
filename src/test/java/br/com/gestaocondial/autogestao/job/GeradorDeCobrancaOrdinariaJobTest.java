package br.com.gestaocondial.autogestao.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

/**
 * Cobre só a aritmética de datas do job (clamp do dia de vencimento ao tamanho do mês-alvo) —
 * a parte mais fácil de errar sutilmente. O restante da regra (bootstrap vs. gatilho de 10 dias)
 * depende de repositórios e é melhor coberto por um teste de integração, fora do escopo desta
 * rodada.
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

}
