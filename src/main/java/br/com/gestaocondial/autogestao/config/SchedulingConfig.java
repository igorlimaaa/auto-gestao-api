package br.com.gestaocondial.autogestao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita {@code @Scheduled} — usado por {@code job.GeradorDeCobrancaOrdinariaJob}. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
