package br.com.gestaocondial.autogestao.config;

/**
 * Nomes dos claims proprietários dos JWTs emitidos pelo
 * {@code gestao-condial-oauth-service}.
 *
 * <p>Cópia deliberada da classe de mesmo nome daquele serviço: os microsserviços de domínio
 * não compartilham biblioteca com o oauth-service — o contrato entre eles é o próprio token.
 * Alterar um nome de claim aqui exige alterá-lo lá junto.</p>
 */
public final class ClaimsDoToken {

	/** Id do usuário (o {@code sub} carrega o email). */
	public static final String ID_USUARIO = "uid";

	/** {@code ACESSO} ou {@code SELECAO_DE_PERFIL}. */
	public static final String ESCOPO = "escopo";

	/** Id do perfil ativo da sessão. */
	public static final String PERFIL_ATIVO = "perfilAtivo";

	/** Tipo do perfil ativo ({@code SINDICO}, {@code MORADOR}, ...). */
	public static final String TIPO_PERFIL = "tipoPerfil";

	/** Id do condomínio do perfil ativo; ausente em perfis globais. */
	public static final String CONDOMINIO = "condominio";

	/** Códigos dos papéis do perfil ativo. */
	public static final String PAPEIS = "papeis";

	/** Códigos das permissões resolvidas a partir dos papéis do perfil ativo. */
	public static final String PERMISSOES = "permissoes";

	/** Único escopo que autentica um endpoint de domínio. */
	public static final String ESCOPO_DE_ACESSO = "ACESSO";

	private ClaimsDoToken() {
	}

}
