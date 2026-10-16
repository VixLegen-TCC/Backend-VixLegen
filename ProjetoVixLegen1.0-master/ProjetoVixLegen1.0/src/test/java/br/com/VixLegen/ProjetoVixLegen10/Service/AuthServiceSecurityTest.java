package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.LoginRequest;
import br.com.VixLegen.ProjetoVixLegen10.Exception.CredenciaisInvalidasException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import br.com.VixLegen.ProjetoVixLegen10.Model.Empresa;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import java.time.Clock;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceSecurityTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtEncoder jwtEncoder = mock(JwtEncoder.class);
    private final ProtecaoLoginService rateLimit = new ProtecaoLoginService(Clock.systemUTC());
    private final AuthService auth = new AuthService(usuarios, encoder, jwtEncoder, rateLimit,
            3600L, "vixlegen-web");

    private LoginRequest credenciais() {
        LoginRequest req = new LoginRequest();
        req.setEmail("teste@exemplo.com");
        req.setSenha("senha-correta");
        return req;
    }

    private Usuario usuarioAtivo() {
        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(3L);
        Categoria cat = new Categoria();
        cat.setCodigoCategoria(3L);
        cat.setNivelAcesso(3);
        cat.setPermissaoVisualizar(true);
        cat.setPermissaoEditar(true);
        Usuario u = new Usuario();
        u.setIdUsuario(10L);
        u.setEmail("teste@exemplo.com");
        u.setPrimeiroNome("Usuario");
        u.setUltimoNome("Teste");
        u.setSenhaHash("hash-bcrypt");
        u.setAtivo(true);
        u.setAuthVersion(2L);
        u.setCategoria(cat);
        u.setEmpresaOrganizacao(empresa);
        return u;
    }

    @Test
    void senhaErradaRetornaCredenciaisInvalidas() {
        when(usuarios.findByEmailIgnoreCase("teste@exemplo.com")).thenReturn(Optional.of(usuarioAtivo()));
        when(encoder.matches(any(), any())).thenReturn(false);
        assertThrows(CredenciaisInvalidasException.class, () -> auth.login(credenciais(), "127.0.0.1"));
        verifyNoInteractions(jwtEncoder);
    }

    @Test
    void contaInativaNaoRecebeJwt() {
        Usuario usuario = usuarioAtivo();
        usuario.setAtivo(false);
        when(usuarios.findByEmailIgnoreCase("teste@exemplo.com")).thenReturn(Optional.of(usuario));
        assertThrows(CredenciaisInvalidasException.class, () -> auth.login(credenciais(), "127.0.0.2"));
        verifyNoInteractions(jwtEncoder);
    }

    @Test
    void loginAtivoEmiteJwt() {
        when(usuarios.findByEmailIgnoreCase("teste@exemplo.com")).thenReturn(Optional.of(usuarioAtivo()));
        when(encoder.matches("senha-correta", "hash-bcrypt")).thenReturn(true);
        when(jwtEncoder.encode(any())).thenReturn(Jwt.withTokenValue("jwt-test")
                .header("alg", "HS256").subject("10").build());

        var resposta = auth.login(credenciais(), "127.0.0.3");
        assertEquals("jwt-test", resposta.getToken());
        assertEquals("ADVOGADO_JUNIOR", resposta.getPerfilAcesso());
    }

    @Test
    void logoutRevogaSessoesDoUsuarioNoBanco() {
        when(usuarios.revogarSessoes(10L)).thenReturn(1);
        auth.logout(10L);
        verify(usuarios).revogarSessoes(10L);
    }
}
