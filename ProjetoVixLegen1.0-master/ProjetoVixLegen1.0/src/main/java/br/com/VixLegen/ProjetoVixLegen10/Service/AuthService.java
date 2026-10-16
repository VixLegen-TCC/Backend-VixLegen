package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.LoginRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.LoginResponse;
import br.com.VixLegen.ProjetoVixLegen10.Enums.PerfilAcesso;
import br.com.VixLegen.ProjetoVixLegen10.Exception.CredenciaisInvalidasException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String ISSUER = "vixlegen-api";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;
    private final String audiencia;
    private final ProtecaoLoginService protecao;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            ProtecaoLoginService protecao,
            @Value("${jwt.expiration-seconds:3600}") long expirationSeconds,
            @Value("${jwt.audience:vixlegen-web}") String audiencia) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
        this.audiencia = audiencia;
        this.protecao = protecao;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, String ipRemoto) {

        String email = request.getEmail().trim();

        protecao.verificar(email, ipRemoto);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario == null || !usuario.isAtivo()
                || usuario.getEmpresaOrganizacao() == null
                || usuario.getCategoria() == null
                || !passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            protecao.falha(email, ipRemoto);
            throw credenciaisInvalidas();
        }

        protecao.sucesso(email);

        Instant agora = Instant.now();
        Instant expiracao = agora.plus(
                expirationSeconds,
                ChronoUnit.SECONDS
        );

        Categoria categoria = usuario.getCategoria();
        PerfilAcesso perfilAcesso = categoria != null
                ? PerfilAcesso.porNivel(categoria.getNivelAcesso())
                : null;

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(audiencia))
                .issuedAt(agora)
                .expiresAt(expiracao)
                .subject(usuario.getIdUsuario().toString())
                .claim("scope", PermissoesJwt.escopos(categoria))
                .claim("authVersion", usuario.getAuthVersion() == null ? 0L : usuario.getAuthVersion());

        if (categoria != null) {
            claims.claim(
                    "codigoCategoria",
                    categoria.getCodigoCategoria()
            );
            claims.claim(
                    "nivelAcesso",
                    categoria.getNivelAcesso()
            );
        }

        if (perfilAcesso != null) {
            claims.claim(
                    "role",
                    perfilAcesso.name()
            );
        }

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(claims.build())
        ).getTokenValue();

        return new LoginResponse(
                token,
                "Bearer",
                expiracao,
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getPrimeiroNome()
                        + " "
                        + usuario.getUltimoNome(),
                categoria != null
                        ? categoria.getCodigoCategoria()
                        : null,
                categoria != null
                        ? categoria.getNivelAcesso()
                        : null,
                perfilAcesso != null
                        ? perfilAcesso.name()
                        : null
        );
    }

    @Transactional
    public void logout(Long idUsuario) {
        if (usuarioRepository.revogarSessoes(idUsuario) != 1) {
            throw credenciaisInvalidas();
        }
    }

    private CredenciaisInvalidasException credenciaisInvalidas() {
        return new CredenciaisInvalidasException(
                "E-mail ou senha inválidos"
        );
    }
}
