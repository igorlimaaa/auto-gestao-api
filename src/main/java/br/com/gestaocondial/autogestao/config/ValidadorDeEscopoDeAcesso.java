package br.com.gestaocondial.autogestao.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Rejeita tokens que não sejam de acesso.
 *
 * <p>O oauth-service emite dois tipos de JWT com a MESMA chave e o mesmo issuer: o de acesso e
 * o de seleção de perfil (o de vida curta devolvido quando o usuário tem mais de um perfil e
 * ainda não escolheu). Assinatura e issuer são idênticos nos dois, então nada além do claim
 * {@code escopo} os distingue — sem esta validação, um token de seleção passaria por
 * {@code authenticated()} e abriria as APIs de domínio a uma sessão que ainda nem escolheu em
 * nome de qual perfil está agindo.</p>
 */
public class ValidadorDeEscopoDeAcesso implements OAuth2TokenValidator<Jwt> {

	private static final OAuth2Error ERRO = new OAuth2Error("invalid_token",
			"O token não é um token de acesso: conclua a seleção de perfil antes de chamar as APIs de domínio.",
			null);

	@Override
	public OAuth2TokenValidatorResult validate(Jwt token) {
		String escopo = token.getClaimAsString(ClaimsDoToken.ESCOPO);
		return ClaimsDoToken.ESCOPO_DE_ACESSO.equals(escopo)
				? OAuth2TokenValidatorResult.success()
				: OAuth2TokenValidatorResult.failure(ERRO);
	}

}
