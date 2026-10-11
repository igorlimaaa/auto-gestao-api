package br.com.gestaocondial.autogestao.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.gestaocondial.autogestao.service.SincronizacaoBancariaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Agenda a sincronização com o Banco Inter. A lógica vive em {@link SincronizacaoBancariaService};
 * aqui só os horários.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SincronizadorDeCobrancasInterJob {

	private final SincronizacaoBancariaService sincronizacao;

	/** De hora em hora, reconsulta as cobranças vindas do banco que ainda não foram pagas. */
	@Scheduled(cron = "0 0 * * * *")
	public void sincronizarPendentes() {
		try {
			sincronizacao.sincronizarPendentes(null);
		} catch (RuntimeException e) {
			log.error("Sincronização horária com o Banco Inter falhou.", e);
		}
	}

	/** Uma vez por dia, depois da geração local (03:00), traz do banco as cobranças novas da janela. */
	@Scheduled(cron = "0 30 3 * * *")
	public void importar() {
		try {
			sincronizacao.importar(null);
		} catch (RuntimeException e) {
			log.error("Importação diária do Banco Inter falhou.", e);
		}
	}

}
