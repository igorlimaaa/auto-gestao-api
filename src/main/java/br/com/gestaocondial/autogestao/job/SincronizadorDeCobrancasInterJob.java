package br.com.gestaocondial.autogestao.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.gestaocondial.autogestao.service.SincronizacaoBancariaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Agenda a sincronização com o Banco Inter, que é a única origem das cobranças. A lógica vive em
 * {@link SincronizacaoBancariaService}; aqui só o horário.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SincronizadorDeCobrancasInterJob {

	private final SincronizacaoBancariaService sincronizacao;

	/**
	 * De hora em hora: primeiro importa o que o banco emitiu (cria o registro e o vínculo com a
	 * unidade; uma chamada por condomínio), depois reconsulta, uma a uma, as ainda em aberto — que
	 * cobre também as que já saíram da janela da listagem. Uma falha não impede a outra etapa.
	 */
	@Scheduled(cron = "0 0 * * * *")
	public void sincronizar() {
		try {
			sincronizacao.importar(null);
		} catch (RuntimeException e) {
			log.error("Importação do Banco Inter falhou.", e);
		}
		try {
			sincronizacao.sincronizarPendentes(null);
		} catch (RuntimeException e) {
			log.error("Sincronização das cobranças em aberto com o Banco Inter falhou.", e);
		}
	}

}
