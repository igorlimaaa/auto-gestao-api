package br.com.gestaocondial.autogestao.banking;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Localiza as credenciais Inter de um condomínio. Layout esperado, montado como volume na VPS:
 *
 * <pre>
 * &lt;diretorio-credenciais&gt;/&lt;idCondominio&gt;/credenciais.properties   (client-id, client-secret)
 * &lt;diretorio-credenciais&gt;/&lt;idCondominio&gt;/certificado.crt
 * &lt;diretorio-credenciais&gt;/&lt;idCondominio&gt;/chave.key                  (PKCS#8, PEM)
 * </pre>
 *
 * Condomínio sem essa pasta simplesmente não está integrado: {@link #buscar(Long)} devolve vazio.
 */
@Component
@RequiredArgsConstructor
public class CredenciaisInterProvider {

	private final InterProperties propriedades;

	public Optional<CredencialInter> buscar(Long idCondominio) {
		Path pasta = Path.of(propriedades.diretorioCredenciais()).resolve(String.valueOf(idCondominio));
		Path arquivoDeCredenciais = pasta.resolve("credenciais.properties");
		Path certificado = pasta.resolve("certificado.crt");
		Path chave = pasta.resolve("chave.key");
		if (!Files.isRegularFile(arquivoDeCredenciais) || !Files.isRegularFile(certificado)
				|| !Files.isRegularFile(chave)) {
			return Optional.empty();
		}
		Properties credenciais = new Properties();
		try (InputStream entrada = Files.newInputStream(arquivoDeCredenciais)) {
			credenciais.load(entrada);
		} catch (IOException e) {
			throw new InterApiException("Não foi possível ler as credenciais do condomínio " + idCondominio, e);
		}
		String clientId = credenciais.getProperty("client-id");
		String clientSecret = credenciais.getProperty("client-secret");
		if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
			throw new InterApiException("credenciais.properties do condomínio " + idCondominio
					+ " precisa de client-id e client-secret.");
		}
		return Optional.of(new CredencialInter(clientId.trim(), clientSecret.trim(), certificado, chave));
	}

}
