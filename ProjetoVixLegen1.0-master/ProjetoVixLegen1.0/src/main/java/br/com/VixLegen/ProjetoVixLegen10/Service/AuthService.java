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
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private static final String ISSUER = "vixlegen-api";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${jwt.expiration-seconds:3600}") long expirationSeconds) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail().trim();

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(this::credenciaisInvalidas);

        if (!usuario.isAtivo()
                || !passwordEncoder.matches(
                        request.getSenha(),
                        usuario.getSenhaHash())) {

            throw credenciaisInvalidas();
        }

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
                .issuedAt(agora)
                .expiresAt(expiracao)
                .subject(usuario.getIdUsuario().toString())
                .claim("email", usuario.getEmail())
                .claim(
                        "nome",
                        usuario.getPrimeiroNome()
                                + " "
                                + usuario.getUltimoNome()
                )
                .claim("scope", montarEscopos(categoria));

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

    private String montarEscopos(Categoria categoria) {

        if (categoria == null) {
            return "";
        }

        List<String> escopos = new ArrayList<>();

        if (categoria.isPermissaoVisualizar()) {
            escopos.add("visualizar");
        }

        if (categoria.isPermissaoEditar()) {
            escopos.add("editar");
        }

        if (categoria.isPermissaoExcluir()) {
            escopos.add("excluir");
        }

        return String.join(" ", escopos);
    }

    private CredenciaisInvalidasException credenciaisInvalidas() {
        return new CredenciaisInvalidasException(
                "E-mail ou senha inválidos"
        );
    }
}
