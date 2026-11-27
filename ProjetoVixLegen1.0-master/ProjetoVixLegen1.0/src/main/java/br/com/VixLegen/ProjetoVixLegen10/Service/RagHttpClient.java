package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RagApiException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ServicoIAIndisponivelException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.TempoLimiteIAException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class RagHttpClient {
    private static final ParameterizedTypeReference<Map<String, Object>> OBJETO =
            new ParameterizedTypeReference<>() {};
    private final RestClient client;
    public RagHttpClient(@Value("${rag.url:http://127.0.0.1:8000}") String url,
                         @Value("${rag.api-key:${RAG_API_KEY:}}") String chave,
                         @Value("${rag.timeout-segundos:660}") long timeout) {
        URI uri = URI.create(url);
        if (!"http".equals(uri.getScheme()) ||
                !("127.0.0.1".equals(uri.getHost()) || "localhost".equals(uri.getHost())) ||
                uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null ||
                !(uri.getPath().isEmpty() || "/".equals(uri.getPath())) || timeout <= 0)
            throw new IllegalArgumentException("Configure uma URL HTTP local e timeout positivo para o RAG");
        var http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(timeout));
        var builder = RestClient.builder().baseUrl(url).requestFactory(factory);
        if (!chave.isBlank()) builder.defaultHeader("X-API-Key", chave);
        client = builder.build();
    }
    public Map<String, Object> health() {
        return executar(() -> client.get().uri("/health").retrieve().body(OBJETO));
    }
    public Map<String, Object> criar() {
        return executar(() -> client.post().uri("/sessions").retrieve().body(OBJETO));
    }
    public Map<String, Object> perguntar(UUID id, String mensagem) {
        return executar(() -> client.post().uri("/chat").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("conversa_id", id.toString(), "mensagem", mensagem))
                .retrieve().body(OBJETO));
    }
    public Map<String, Object> resetar(UUID id) {
        return executar(() -> client.post().uri("/sessions/{id}/reset", id).retrieve().body(OBJETO));
    }
    public void encerrar(UUID id) {
        executar(() -> { client.delete().uri("/sessions/{id}", id).retrieve().toBodilessEntity(); return Boolean.TRUE; });
    }
    private <T> T executar(Supplier<T> chamada) {
        try {
            T valor = chamada.get();
            if (valor == null) throw new ServicoIAIndisponivelException("Resposta vazia do RAG");
            return valor;
        } catch (RestClientResponseException ex) {
            throw switch (ex.getStatusCode().value()) {
                case 404 -> new RagApiException(HttpStatus.GONE, "IA_SESSAO_EXPIRADA", "Sessão da IA expirada. Crie outra conversa.");
                case 409 -> new RagApiException(HttpStatus.CONFLICT, "IA_CONVERSA_OCUPADA", "Conversa ocupada.");
                case 429 -> new RagApiException(HttpStatus.TOO_MANY_REQUESTS, "IA_OCUPADA", "IA ocupada ou limite de sessões atingido.");
                case 400, 422 -> new RagApiException(HttpStatus.BAD_REQUEST, "IA_ENTRADA_INVALIDA", "Mensagem ou parâmetros inválidos.");
                case 504 -> new TempoLimiteIAException("Tempo limite da IA excedido");
                default -> new ServicoIAIndisponivelException("Serviço RAG indisponível ou configuração inválida");
            };
        } catch (RestClientException ex) {
            for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
                if (causa instanceof HttpTimeoutException || causa instanceof SocketTimeoutException)
                    throw new TempoLimiteIAException("Tempo limite da IA excedido");
            }
            throw new ServicoIAIndisponivelException("Não foi possível comunicar com o RAG");
        }
    }
}
