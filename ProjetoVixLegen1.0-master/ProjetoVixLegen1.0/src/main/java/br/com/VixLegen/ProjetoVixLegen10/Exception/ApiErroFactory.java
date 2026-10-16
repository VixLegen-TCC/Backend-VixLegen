package br.com.VixLegen.ProjetoVixLegen10.Exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resposta unificada para falhas do MVC e filtros de segurança.
 * Nunca expõe valores de parâmetros, caminhos com CPF/e-mail ou detalhes internos.
 */
@Component
public class ApiErroFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErroFactory.class);

    public Map<String, Object> criar(HttpStatus status, String codigo, String mensagem,
                                      HttpServletRequest request, Exception causa,
                                      Map<String, String> campos) {
        String erroId = UUID.randomUUID().toString();
        String rota = rotaSegura(request);
        String usuarioId = usuarioId();

        if (status.is5xxServerError()) {
            // Stack trace apenas no log do servidor, nunca na resposta HTTP.
            LOG.error("erroId={} usuarioId={} status={} codigo={} metodo={} rota={} tipo={}",
                    erroId, usuarioId, status.value(), codigo, request.getMethod(), rota,
                    causa.getClass().getSimpleName(), causa);
        } else {
            LOG.warn("erroId={} usuarioId={} status={} codigo={} metodo={} rota={} tipo={}",
                    erroId, usuarioId, status.value(), codigo, request.getMethod(), rota,
                    causa.getClass().getSimpleName());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("codigo", codigo);
        body.put("mensagem", mensagem);
        body.put("erroId", erroId);
        body.put("rota", rota);
        if (campos != null && !campos.isEmpty()) {
            body.put("campos", campos);
        }
        return body;
    }

    public void escreverSeguranca(HttpServletRequest request, HttpServletResponse response,
                                   HttpStatus status, String codigo, String mensagem, Exception causa)
            throws IOException {
        Map<String, Object> body = criar(status, codigo, mensagem, request, causa, Map.of());
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // Sem dependência de uma versão específica do Jackson nos filtros do Spring Security.
        response.getWriter().write("{\"timestamp\":\"" + escapar((String) body.get("timestamp"))
                + "\",\"status\":" + body.get("status")
                + ",\"codigo\":\"" + escapar((String) body.get("codigo"))
                + "\",\"mensagem\":\"" + escapar((String) body.get("mensagem"))
                + "\",\"erroId\":\"" + escapar((String) body.get("erroId"))
                + "\",\"rota\":\"" + escapar((String) body.get("rota")) + "\"}");
    }

    public static String usuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            return subject != null && subject.matches("[0-9]{1,19}") ? subject : "indisponivel";
        }
        return "anonimo";
    }

    public static String rotaSegura(HttpServletRequest request) {
        // O padrão registrado pelo Spring não contém os valores de IDs/CPFs.
        Object padrao = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (padrao instanceof String nome && !nome.isBlank()) {
            return nome;
        }
        // Falhas antes do Controller: registrar somente o segmento do módulo.
        String caminho = request.getRequestURI();
        if (caminho == null || !caminho.startsWith("/")) return "/**";
        String[] partes = caminho.split("/");
        String modulo = partes.length > 1 ? partes[1] : "";
        if (!modulo.matches("[a-zA-Z-]{1,50}")) return "/**";
        return "/" + modulo + "/**";
    }

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
