package br.com.VixLegen.ProjetoVixLegen10.Config;

import br.com.VixLegen.ProjetoVixLegen10.Exception.ApiErroFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Set;

/**
 * Auditoria de operações concluídas e alerta de lentidão, sem corpo, query string,
 * tokens, CPF, nomes de pessoas ou texto jurídico.
 */
@Component
public class AuditoriaHttpInterceptor implements HandlerInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaHttpInterceptor.class);
    private static final String INICIO = AuditoriaHttpInterceptor.class.getName() + ".inicio";
    private static final Set<String> MODULOS = Set.of("clientes", "processos", "tarefas", "documentos");

    private final long limiteLentoMs;

    public AuditoriaHttpInterceptor(@Value("${app.logs.slow-ms:2000}") long limiteLentoMs) {
        this.limiteLentoMs = limiteLentoMs;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(INICIO, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Object inicio = request.getAttribute(INICIO);
        if (!(inicio instanceof Long tempoInicial)) return;
        long duracaoMs = Math.max(0L, (System.nanoTime() - tempoInicial) / 1_000_000);
        String rota = ApiErroFactory.rotaSegura(request);
        String usuarioId = ApiErroFactory.usuarioId();
        String metodo = request.getMethod();

        String[] partes = rota.split("/");
        String modulo = partes.length > 1 ? partes[1] : "";
        String acao = switch (metodo) {
            case "POST" -> "CRIAR";
            case "PUT", "PATCH" -> "ALTERAR";
            case "DELETE" -> "EXCLUIR";
            default -> "";
        };

        if (!acao.isEmpty() && MODULOS.contains(modulo) && response.getStatus() >= 200
                && response.getStatus() < 300) {
            String id = "-";
            Object variaveis = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (variaveis instanceof Map<?, ?> map && map.get("id") != null) {
                String candidato = String.valueOf(map.get("id"));
                if (candidato.matches("[0-9]{1,19}")) id = candidato;
            }
            LOG.info("auditoria=OPERACAO_CONCLUIDA usuarioId={} recurso={} acao={} recursoId={} status={} duracaoMs={}",
                    usuarioId, modulo, acao, id, response.getStatus(), duracaoMs);
        }

        if (duracaoMs >= limiteLentoMs) {
            LOG.warn("requisicao_lenta usuarioId={} metodo={} rota={} status={} duracaoMs={}",
                    usuarioId, metodo, rota, response.getStatus(), duracaoMs);
        }
    }
}
