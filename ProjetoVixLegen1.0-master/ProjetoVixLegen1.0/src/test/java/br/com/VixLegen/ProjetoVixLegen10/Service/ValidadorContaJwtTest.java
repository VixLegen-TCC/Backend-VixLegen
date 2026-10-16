package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import br.com.VixLegen.ProjetoVixLegen10.Model.Empresa;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidadorContaJwtTest {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final ValidadorContaJwt validador = new ValidadorContaJwt(repository, "vixlegen-web");

    private Usuario usuario() {
        Categoria cat = new Categoria();
        cat.setCodigoCategoria(3L);
        cat.setNivelAcesso(3);
        cat.setPermissaoVisualizar(true);
        cat.setPermissaoEditar(true);
        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(1L);
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setAtivo(true);
        usuario.setCategoria(cat);
        usuario.setEmpresaOrganizacao(empresa);
        usuario.setAuthVersion(0L);
        return usuario;
    }

    private Jwt token(long versao, String escopo, String role) {
        return Jwt.withTokenValue("token-dummy")
                .header("alg", "HS256")
                .issuer("vixlegen-api")
                .subject("10")
                .audience(List.of("vixlegen-web"))
                .claim("codigoCategoria", 3L)
                .claim("authVersion", versao)
                .claim("scope", escopo)
                .claim("role", role)
                .build();
    }

    @Test
    void tokenValidoEhAceito() {
        when(repository.findById(10L)).thenReturn(Optional.of(usuario()));
        assertFalse(validador.validate(token(0, "visualizar editar", "ADVOGADO_JUNIOR")).hasErrors());
    }

    @Test
    void usuarioDesativadoPerdeAcessoImediatamente() {
        Usuario u = usuario();
        u.setAtivo(false);
        when(repository.findById(10L)).thenReturn(Optional.of(u));
        assertTrue(validador.validate(token(0, "visualizar editar", "ADVOGADO_JUNIOR")).hasErrors());
    }

    @Test
    void logoutRevogaTokenAnterior() {
        Usuario u = usuario();
        u.setAuthVersion(1L);
        when(repository.findById(10L)).thenReturn(Optional.of(u));
        assertTrue(validador.validate(token(0, "visualizar editar", "ADVOGADO_JUNIOR")).hasErrors());
        assertFalse(validador.validate(token(1, "visualizar editar", "ADVOGADO_JUNIOR")).hasErrors());
    }

    @Test
    void rebaixamentoDePermissaoInvalidaToken() {
        Usuario u = usuario();
        u.getCategoria().setPermissaoEditar(false);
        when(repository.findById(10L)).thenReturn(Optional.of(u));
        assertTrue(validador.validate(token(0, "visualizar editar", "ADVOGADO_JUNIOR")).hasErrors());
    }

    @Test
    void outroPerfilNoTokenEhRecusado() {
        when(repository.findById(10L)).thenReturn(Optional.of(usuario()));
        assertTrue(validador.validate(token(0, "visualizar editar", "ADMIN")).hasErrors());
    }

    @Test
    void audienciaErradaEhRecusada() {
        when(repository.findById(10L)).thenReturn(Optional.of(usuario()));
        Jwt jwt = Jwt.withTokenValue("token-dummy").header("alg", "HS256")
                .issuer("vixlegen-api").subject("10").audience(List.of("outro-servico"))
                .claim("authVersion", 0L).claim("scope", "visualizar editar")
                .claim("role", "ADVOGADO_JUNIOR").claim("codigoCategoria", 3L).build();
        assertTrue(validador.validate(jwt).hasErrors());
    }
}
