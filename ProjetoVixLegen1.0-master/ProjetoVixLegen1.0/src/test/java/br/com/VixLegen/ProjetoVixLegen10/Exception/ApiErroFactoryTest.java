package br.com.VixLegen.ProjetoVixLegen10.Exception;

import br.com.VixLegen.ProjetoVixLegen10.Config.ApiSegurancaHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ApiErroFactoryTest {
    private final ApiErroFactory factory = new ApiErroFactory();

    @Test
    void naoExpoeParametrosDaRota() {
        var req = new MockHttpServletRequest("GET", "/usuarios/cpf/valor-privado");
        var erro = factory.criar(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "Parâmetro inválido", req, new IllegalArgumentException(), Map.of());
        assertEquals(400, erro.get("status"));
        assertEquals("/usuarios/**", erro.get("rota"));
        assertFalse(erro.toString().contains("valor-privado"));
        assertTrue(((String) erro.get("erroId")).length() >= 32);
    }

    @Test
    void loginNaoAutenticadoProduz401Json() throws Exception {
        var handler = new ApiSegurancaHandler(factory);
        var response = new MockHttpServletResponse();
        handler.commence(new MockHttpServletRequest("GET", "/processos/18"),
                response, new BadCredentialsException("diagnostico interno"));
        assertEquals(401, response.getStatus());
        assertEquals("Bearer", response.getHeader("WWW-Authenticate"));
        assertTrue(response.getContentAsString().contains("AUTENTICACAO_NECESSARIA"));
        assertTrue(response.getContentAsString().contains("erroId"));
        assertFalse(response.getContentAsString().contains("diagnostico interno"));
    }

    @Test
    void permissaoNegadaProduz403Json() throws Exception {
        var handler = new ApiSegurancaHandler(factory);
        var response = new MockHttpServletResponse();
        handler.handle(new MockHttpServletRequest("DELETE", "/tarefas/18"),
                response, new AccessDeniedException("detalhe interno"));
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ACESSO_NEGADO"));
        assertTrue(response.getContentAsString().contains("erroId"));
        assertFalse(response.getContentAsString().contains("detalhe interno"));
    }
}
