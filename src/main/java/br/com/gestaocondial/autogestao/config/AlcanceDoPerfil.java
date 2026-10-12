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
 * O que o perfil da requisição alcança além do recorte por condomínio (que é do
 * {@link EscopoDoCondominio}).
 *
 * <ul>
 * <li><b>Boleto (PDF)</b> — traz nome, CPF e endereço do pagador, então a regra é estreita:
 * administrador e síndico abrem o de qualquer unidade; os demais, só o da própria
 * ({@link Alcance#amplo()}).</li>
 * <li><b>Listas e detalhes de unidades, moradores e cobranças</b> — um perfil do tipo
 * {@code MORADOR} só vê o que é seu: a própria unidade, o próprio cadastro e as cobranças da
 * própria unidade ({@link Alcance#apenasProprio()}). Os demais perfis (síndico, subsíndico,
 * financeiro, portaria, ...) continuam vendo o condomínio inteiro.</li>
 * </ul>
 *
 * <p>"Próprio" é achado pelo e-mail do token (o {@code sub}) cruzado com o cadastro de
 * moradores.</p>
 */
@Component
@RequiredArgsConstructor
public class AlcanceDoPerfil {

	/** Tipos de perfil com visão ampla. */
	static final Set<String> TIPOS_DE_PERFIL_AMPLOS = Set.of("ADMINISTRADORA", "SINDICO");

	/** Papéis com visão ampla (mesmo código nos dois sentidos: o do oauth-service). */
	/** Tipo de perfil do morador. */
	static final String TIPO_MORADOR = "MORADOR";

	/** Permissão que dá poder de aprovar/recusar eventos da agenda. */
	static final String AUTORIDADE_DE_APROVACAO = "EVENTO_APROVAR";

	static final Set<String> PAPEIS_AMPLOS = Set.of("ADMINISTRADOR", "SINDICO");

	private final PessoaRepository pessoaRepository;

	/** Resolve uma vez por requisição; consulta o cadastro só se a visão não for ampla. */
	public Alcance alcance() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt token)) {
			// Sem token é chamada interna (job, teste): nenhum endpoint chega aqui sem JWT, porque a
			// cadeia de segurança barra antes. O boleto continua fechado (amplo = false).
			return new Alcance(false, false, Set.of(), Set.of(), null, false);
		}
		boolean amplo = ehAmplo(token);
		boolean apenasProprio = ehMorador(token);
		String email = token.getSubject();
		boolean aprova = autenticacao.getAuthorities().stream()
				.anyMatch(autoridade -> AUTORIDADE_DE_APROVACAO.equals(autoridade.getAuthority()));
		if (amplo && !apenasProprio) {
			return new Alcance(true, false, Set.of(), Set.of(), email, aprova);
		}

		Collection<Pessoa> cadastros = cadastrosDe(token);
		Set<Long> unidades = cadastros.stream()
				.map(Pessoa::getUnidade)
				.filter(unidade -> unidade != null)
				.map(unidade -> unidade.getId())
				.collect(Collectors.toSet());
		Set<Long> pessoas = cadastros.stream().map(Pessoa::getId).collect(Collectors.toSet());
		return new Alcance(amplo, apenasProprio, unidades, pessoas, email, aprova);
	}

	private static boolean ehAmplo(Jwt token) {
		String tipo = token.getClaimAsString(ClaimsDoToken.TIPO_PERFIL);
		if (tipo != null && TIPOS_DE_PERFIL_AMPLOS.contains(tipo)) {
			return true;
		}
		return temPapelAmplo(token);
	}

	/** Perfil de morador, sem nenhum papel de gestão que lhe dê visão ampla. */
	private static boolean ehMorador(Jwt token) {
		return TIPO_MORADOR.equals(token.getClaimAsString(ClaimsDoToken.TIPO_PERFIL)) && !temPapelAmplo(token);
	}

	private static boolean temPapelAmplo(Jwt token) {
		List<String> papeis = token.getClaimAsStringList(ClaimsDoToken.PAPEIS);
		return papeis != null && papeis.stream().anyMatch(PAPEIS_AMPLOS::contains);
	}

	private Collection<Pessoa> cadastrosDe(Jwt token) {
		String email = token.getSubject();
		if (email == null || email.isBlank()) {
			return List.of();
		}
		Number condominio = token.getClaim(ClaimsDoToken.CONDOMINIO);
		return condominio == null
				? pessoaRepository.findByEmailIgnoreCase(email)
				: pessoaRepository.findByEmailIgnoreCaseAndCondominioId(email, condominio.longValue());
	}

	/**
	 * O que o perfil da requisição alcança.
	 *
	 * @param amplo vê o boleto de qualquer unidade
	 * @param apenasProprio é um morador: só vê a própria unidade, o próprio cadastro e as cobranças
	 *            da própria unidade
	 * @param unidades unidades do próprio usuário (vazio quando {@code amplo} e não morador)
	 * @param pessoas cadastros de morador do próprio usuário (idem)
	 * @param email e-mail do login (o {@code sub} do token); nulo em chamada interna
	 * @param aprova tem a permissão de decidir sobre eventos pendentes
	 */
	public record Alcance(boolean amplo, boolean apenasProprio, Set<Long> unidades, Set<Long> pessoas, String email,
			boolean aprova) {

		public boolean podeVer(Long idUnidade) {
			return amplo || (idUnidade != null && unidades.contains(idUnidade));
		}

		/** Pode ver a unidade/cobrança de {@code idUnidade} numa lista ou detalhe. */
		public boolean podeVerUnidade(Long idUnidade) {
			return !apenasProprio || (idUnidade != null && unidades.contains(idUnidade));
		}

		/** Pode ver o cadastro de morador {@code idPessoa} numa lista ou detalhe. */
		public boolean podeVerPessoa(Long idPessoa) {
			return !apenasProprio || (idPessoa != null && pessoas.contains(idPessoa));
		}

	}

}
