package br.com.gestaocondial.autogestao.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Traduz os claims do token do oauth-service em authorities do Spring Security:
 * {@code papeis} vira {@code ROLE_<CODIGO>} (para {@code hasRole}) e {@code permissoes} vira a
 * authority de mesmo nome (para {@code hasAuthority}).
 *
 * <p>Um token de seleção de perfil ({@code escopo != ACESSO}) chega aqui sem papel nenhum: ele
 * autentica apenas o {@code /auth/trocar-perfil} do oauth-service, nunca uma API de domínio.</p>
 */
@Component
public class ConversorDeAutoridadesDoJwt implements Converter<Jwt, AbstractAuthenticationToken> {

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Collection<GrantedAuthority> autoridades = new ArrayList<>();

		if (ClaimsDoToken.ESCOPO_DE_ACESSO.equals(jwt.getClaimAsString(ClaimsDoToken.ESCOPO))) {
			for (String papel : listaDeTextos(jwt, ClaimsDoToken.PAPEIS)) {
				autoridades.add(new SimpleGrantedAuthority("ROLE_" + papel));
			}
			for (String permissao : listaDeTextos(jwt, ClaimsDoToken.PERMISSOES)) {
				autoridades.add(new SimpleGrantedAuthority(permissao));
			}
		}

		return new JwtAuthenticationToken(jwt, autoridades, jwt.getSubject());
	}

	private static List<String> listaDeTextos(Jwt jwt, String claim) {
		List<String> valores = jwt.getClaimAsStringList(claim);
		return valores == null ? List.of() : valores;
	}

}
