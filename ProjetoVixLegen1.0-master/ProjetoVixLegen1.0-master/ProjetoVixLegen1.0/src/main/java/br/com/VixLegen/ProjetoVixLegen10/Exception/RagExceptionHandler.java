package br.com.VixLegen.ProjetoVixLegen10.Exception;

import br.com.VixLegen.ProjetoVixLegen10.Controller.ChatIAController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ChatIAController.class)
public class RagExceptionHandler {
    private final ApiErroFactory erros;
    public RagExceptionHandler(ApiErroFactory erros) { this.erros = erros; }
    @ExceptionHandler(RagApiException.class)
    public ResponseEntity<Map<String, Object>> tratar(RagApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.status()).body(erros.criar(ex.status(), ex.codigo(), ex.getMessage(), request, ex, Map.of()));
    }
}
