package br.com.gestaocondial.autogestao.banking;

import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Monta o {@link SSLContext} de cliente (mTLS) direto dos PEM que o Inter entrega — certificado
 * X.509 e chave PKCS#8 —, sem converter para {@code .p12}. O SDK oficial exige o {@code .p12};
 * aqui o resultado é o mesmo keystore, só que montado em memória.
 */
final class InterSslContexts {

	private static final char[] SENHA_EM_MEMORIA = new char[0];

	private InterSslContexts() {
	}

	static SSLContext criar(CredencialInter credencial) {
		try {
			X509Certificate certificado = lerCertificado(credencial);
			if (certificado.getNotAfter().before(new Date())) {
				throw new InterApiException("Certificado do Banco Inter expirado em " + certificado.getNotAfter());
			}
			PrivateKey chave = lerChave(credencial);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			keyStore.setKeyEntry("inter", chave, SENHA_EM_MEMORIA, new java.security.cert.Certificate[] { certificado });

			KeyManagerFactory fabrica = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			fabrica.init(keyStore, SENHA_EM_MEMORIA);
			SSLContext contexto = SSLContext.getInstance("TLSv1.2");
			contexto.init(fabrica.getKeyManagers(), null, new java.security.SecureRandom());
			return contexto;
		} catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
			throw new InterApiException("Não foi possível montar o certificado de cliente do Banco Inter.", e);
		}
	}

	private static X509Certificate lerCertificado(CredencialInter credencial) throws IOException, GeneralSecurityException {
		try (var entrada = Files.newInputStream(credencial.certificado())) {
			return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(entrada);
		}
	}

	private static PrivateKey lerChave(CredencialInter credencial) throws IOException, GeneralSecurityException {
		String pem = Files.readString(credencial.chave());
		if (!pem.contains("BEGIN PRIVATE KEY")) {
			throw new InterApiException("A chave do Banco Inter precisa estar em PKCS#8 (BEGIN PRIVATE KEY).");
		}
		String base64 = pem.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
		byte[] der = Base64.getDecoder().decode(base64);
		PKCS8EncodedKeySpec especificacao = new PKCS8EncodedKeySpec(der);
		try {
			return KeyFactory.getInstance("RSA").generatePrivate(especificacao);
		} catch (GeneralSecurityException e) {
			return KeyFactory.getInstance("EC").generatePrivate(especificacao);
		}
	}

}
