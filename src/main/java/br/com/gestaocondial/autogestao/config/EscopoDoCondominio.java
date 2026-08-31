package br.com.gestaocondial.autogestao.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import br.com.gestaocondial.autogestao.exception.AcessoNegadoException;

/**
 * Isolamento por condomínio.
 *
 * <p>Ter a permissão {@code MORADOR_LER} não deveria significar "ver os moradores de todos os
 * condomínios": o síndico do Residencial A não tem nada que ver o cadastro do Edifício B. O
 * recorte vem do claim {@code condominio} do token, que carrega o condomínio do perfil ativo
 * da sessão.</p>
 *
 * <p>Perfis globais (ex.: {@code ADMINISTRADORA}) não têm esse claim e enxergam tudo — é
 * justamente o que os torna globais.</p>
 */
@Component
public class EscopoDoCondominio {

	/** Condomínio do perfil ativo, ou {@code null} quando o perfil é global. */
	public Long doPerfilAtivo() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt token)) {
			return null;
		}
		Number idCondominio = token.getClaim(ClaimsDoToken.CONDOMINIO);
		return idCondominio == null ? null : idCondominio.longValue();
	}

	public boolean ehGlobal() {
		return doPerfilAtivo() == null;
	}

	/**
	 * Resolve o filtro de condomínio de uma listagem. Num perfil restrito, o filtro é imposto
	 * mesmo que o cliente não peça — e pedir outro condomínio é recusado, em vez de silenciosamente
	 * devolver o próprio (que esconderia o erro do chamador).
	 */
	public Long restringir(Long idCondominioPedido) {
		Long escopo = doPerfilAtivo();
		if (escopo == null) {
			return idCondominioPedido;
		}
		if (idCondominioPedido != null && !idCondominioPedido.equals(escopo)) {
			throw new AcessoNegadoException(
					"O perfil ativo desta sessão só enxerga o condomínio #" + escopo + ".");
		}
		return escopo;
	}

	/** Barra o acesso a um registro que pertence a outro condomínio. */
	public void exigirAcesso(Long idCondominioDoRegistro) {
		Long escopo = doPerfilAtivo();
		if (escopo == null) {
			return;
		}
		if (idCondominioDoRegistro == null || !idCondominioDoRegistro.equals(escopo)) {
			throw new AcessoNegadoException(
					"Este registro pertence a outro condomínio. O perfil ativo desta sessão só enxerga o condomínio #"
							+ escopo + ".");
		}
	}

}
