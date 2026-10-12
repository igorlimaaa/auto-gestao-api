package br.com.gestaocondial.autogestao.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import br.com.gestaocondial.autogestao.domain.Pessoa;
import br.com.gestaocondial.autogestao.repository.PessoaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Quem pode abrir o boleto (PDF) de uma cobrança. O PDF traz nome, CPF e endereço do pagador,
 * então a regra é mais estreita que a de ver a lista:
 *
 * <ul>
 * <li><b>Visão ampla</b> — administrador e síndico: o boleto de qualquer unidade do alcance deles
 * (o recorte por condomínio continua sendo do {@link EscopoDoCondominio});</li>
 * <li><b>Demais perfis</b> — só o da própria unidade, achada pelo e-mail do token (o {@code sub})
 * cruzado com o cadastro de moradores.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AcessoAoBoleto {

	/** Tipos de perfil com visão ampla. */
	static final Set<String> TIPOS_DE_PERFIL_AMPLOS = Set.of("ADMINISTRADORA", "SINDICO");

	/** Papéis com visão ampla (mesmo código nos dois sentidos: o do oauth-service). */
	static final Set<String> PAPEIS_AMPLOS = Set.of("ADMINISTRADOR", "SINDICO");

	private final PessoaRepository pessoaRepository;

	/** Resolve uma vez por requisição; consulta o cadastro só se a visão não for ampla. */
	public Alcance alcance() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt token)) {
			return new Alcance(false, Set.of());
		}
		if (ehAmplo(token)) {
			return new Alcance(true, Set.of());
		}
		return new Alcance(false, unidadesDe(token));
	}

	private static boolean ehAmplo(Jwt token) {
		String tipo = token.getClaimAsString(ClaimsDoToken.TIPO_PERFIL);
		if (tipo != null && TIPOS_DE_PERFIL_AMPLOS.contains(tipo)) {
			return true;
		}
		List<String> papeis = token.getClaimAsStringList(ClaimsDoToken.PAPEIS);
		return papeis != null && papeis.stream().anyMatch(PAPEIS_AMPLOS::contains);
	}

	private Set<Long> unidadesDe(Jwt token) {
		String email = token.getSubject();
		if (email == null || email.isBlank()) {
			return Set.of();
		}
		Number condominio = token.getClaim(ClaimsDoToken.CONDOMINIO);
		Collection<Pessoa> pessoas = condominio == null
				? pessoaRepository.findByEmailIgnoreCase(email)
				: pessoaRepository.findByEmailIgnoreCaseAndCondominioId(email, condominio.longValue());
		return pessoas.stream()
				.map(Pessoa::getUnidade)
				.filter(unidade -> unidade != null)
				.map(unidade -> unidade.getId())
				.collect(Collectors.toSet());
	}

	/** O que o perfil da requisição alcança: tudo, ou só as unidades listadas. */
	public record Alcance(boolean amplo, Set<Long> unidades) {

		public boolean podeVer(Long idUnidade) {
			return amplo || (idUnidade != null && unidades.contains(idUnidade));
		}

	}

}
