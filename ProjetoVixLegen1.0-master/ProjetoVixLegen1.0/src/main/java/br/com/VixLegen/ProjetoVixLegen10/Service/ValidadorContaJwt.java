package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Revalida conta e permissões NO BANCO em toda chamada autenticada.
 * Tokens anteriores à implantação, sem authVersion/audience, exigem novo login.
 */
@Component
public class ValidadorContaJwt implements OAuth2TokenValidator<Jwt> {
    private final UsuarioRepository usuarios;
    private final String audiencia;
    private static final OAuth2Error ERRO = new OAuth2Error(
            "invalid_token", "Sessão expirada ou alterada. Faça login novamente", null);

    public ValidadorContaJwt(UsuarioRepository usuarios,
            @Value("${jwt.audience:vixlegen-web}") String audiencia) {
        this.usuarios = usuarios;
        this.audiencia = audiencia;
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2TokenValidatorResult validate(Jwt token) {
        try {
            if (token.getAudience() == null || !token.getAudience().contains(audiencia)) return invalido();
            String sub = token.getSubject();
            if (sub == null || !sub.matches("[0-9]{1,19}")) return invalido();
            Usuario usuario = usuarios.findById(Long.valueOf(sub)).orElse(null);
            if (usuario == null || !usuario.isAtivo()
                    || usuario.getEmpresaOrganizacao() == null || usuario.getCategoria() == null) return invalido();

            Object versaoToken = token.getClaims().get("authVersion");
            long versaoAtual = usuario.getAuthVersion() == null ? 0L : usuario.getAuthVersion();
            if (!(versaoToken instanceof Number v) || v.longValue() != versaoAtual) return invalido();

            Categoria categoria = usuario.getCategoria();
            String perfil = PermissoesJwt.perfil(categoria);
            if (perfil == null || !Objects.equals(perfil, token.getClaimAsString("role"))
                    || !Objects.equals(PermissoesJwt.escopos(categoria), token.getClaimAsString("scope"))) {
                return invalido();
            }
            return OAuth2TokenValidatorResult.success();
        } catch (RuntimeException erro) {
            // Não vazar informações sobre usuários ou a estrutura do banco ao cliente.
            return invalido();
        }
    }

    private OAuth2TokenValidatorResult invalido() {
        return OAuth2TokenValidatorResult.failure(ERRO);
    }
}
