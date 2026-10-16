package br.com.VixLegen.ProjetoVixLegen10.Exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new ApiErroFactory());
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/chat/contexto/arquivo");

    @Test
    void arquivoInvalidoEh422() {
        var response = handler.arquivoInvalido(new ArquivoInvalidoException("Formato inválido"), request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("ARQUIVO_INVALIDO", response.getBody().get("codigo"));
        assertNotNull(response.getBody().get("erroId"));
    }

    @Test
    void arquivoMuitoGrandeEh413() {
        var response = handler.arquivoGrande(new ArquivoMuitoGrandeException("Limite ultrapassado"), request);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
    }

    @Test
    void conflitoEh409() {
        var response = handler.conflitoRecurso(new ConflitoRecursoException("Registro já existe"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void cpfDuplicadoRetornaMensagemCurtaSemDetalhesDoBanco() {
        var response = handler.conflitoRecurso(
                new ConflitoRecursoException("CPF já cadastrado", "cpf"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CPF já cadastrado", response.getBody().get("mensagem"));
        assertEquals("DADO_JA_CADASTRADO", response.getBody().get("codigo"));
        Map<String, String> campos = (Map<String, String>) response.getBody().get("campos");
        assertEquals("CPF já cadastrado", campos.get("cpf"));
        assertFalse(response.getBody().toString().contains("SQL"));
    }

    @Test
    void emailDuplicadoRetornaMensagemEspecifica() {
        var response = handler.conflitoRecurso(
                new ConflitoRecursoException("E-mail já cadastrado", "email"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("E-mail já cadastrado", response.getBody().get("mensagem"));
    }

    @Test
    void falhaInternaNaoExpoeCausa() {
        var response = handler.inesperado(new IllegalStateException("detalhe interno"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("ERRO_INTERNO", body.get("codigo"));
        assertFalse(body.toString().contains("detalhe interno"));
    }
}
