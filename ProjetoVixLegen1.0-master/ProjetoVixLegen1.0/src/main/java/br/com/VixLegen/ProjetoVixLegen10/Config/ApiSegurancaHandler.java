package br.com.VixLegen.ProjetoVixLegen10.Config;

import br.com.VixLegen.ProjetoVixLegen10.Exception.ApiErroFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tratamento de 401/403 negados pelo Spring Security antes de chegar aos controllers.
 * Mantém o formato dos erros JSON e inclui o erroId para suporte.
 */
@Component
public class ApiSegurancaHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ApiErroFactory erros;

    public ApiSegurancaHandler(ApiErroFactory erros) {
        this.erros = erros;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        erros.escreverSeguranca(request, response, HttpStatus.UNAUTHORIZED,
                "AUTENTICACAO_NECESSARIA", "Autenticação necessária ou token inválido", ex);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        erros.escreverSeguranca(request, response, HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO", "Você não tem permissão para esta operação", ex);
    }
}
