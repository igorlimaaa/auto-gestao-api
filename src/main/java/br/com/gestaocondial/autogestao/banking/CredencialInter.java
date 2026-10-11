package br.com.gestaocondial.autogestao.banking;

import java.nio.file.Path;

/** Credenciais OAuth + certificado de cliente (mTLS) de uma conta Inter. */
public record CredencialInter(String clientId, String clientSecret, Path certificado, Path chave) {

	/** Nunca imprime o segredo em log. */
	@Override
	public String toString() {
		return "CredencialInter[clientId=" + clientId + "]";
	}

}
