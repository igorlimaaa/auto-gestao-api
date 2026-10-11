package br.com.gestaocondial.autogestao.banking;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração da integração com o Banco Inter. As credenciais NÃO ficam aqui nem no banco de
 * dados: cada condomínio tem a sua própria conta no Inter, e os segredos vivem em arquivos sob
 * {@code diretorioCredenciais}/&lt;idCondominio&gt;/ (ver {@link CredenciaisInterProvider}).
 */
@ConfigurationProperties(prefix = "banking.inter")
public record InterProperties(String urlBase, String diretorioCredenciais, String escopos) {

}
