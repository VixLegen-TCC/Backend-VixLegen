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
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.BiConsumer;
import java.io.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.type.TypeReference;

@Component
public class RagHttpClient {
    private static final ParameterizedTypeReference<Map<String, Object>> OBJETO =
            new ParameterizedTypeReference<>() {};
    private final RestClient client;
    private final JsonMapper json=JsonMapper.builder().build();
    private static final TypeReference<Map<String,Object>> SSE_MAP=new TypeReference<>() {};
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
    public Map<String, Object> perguntarHistorico(UUID id, String mensagem, List<Map<String,Object>> historico) {
        return executar(() -> client.post().uri("/chat/historico").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("conversa_id",id.toString(),"mensagem",mensagem,"historico",historico))
                .retrieve().body(OBJETO));
    }
    public Map<String,Object> extrairAnexo(String nome,String base64){
        return executar(() -> client.post().uri("/anexos/extrair").contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("arquivo",nome,"conteudo_base64",base64)).retrieve().body(OBJETO));
    }
    public Map<String,Object> perguntarAnexo(UUID id,String mensagem,String anexoId,Object doc,List<Map<String,Object>> historico){
        return executar(() -> client.post().uri("/chat/anexo").contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("conversa_id",id.toString(),"mensagem",mensagem,"anexo_id",anexoId,"documento",doc,"historico",historico)).retrieve().body(OBJETO));
    }
    public Map<String,Object> perguntarProcesso(UUID id,String mensagem,Map<String,Object> processo,List<Map<String,Object>> historico){
        return executar(() -> client.post().uri("/chat/processo").contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("conversa_id",id.toString(),"mensagem",mensagem,"processo",processo,"historico",historico)).retrieve().body(OBJETO));
    }
    public Map<String,Object> stream(String path,Map<String,Object> body,BiConsumer<String,Map<String,Object>> eventos){
        return executar(() -> client.post().uri(path).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM).body(body).exchange((req,res)->{
            int status=res.getStatusCode().value();
            if(status!=200)throw statusErro(status);
            var tipo=res.getHeaders().getContentType();
            if(tipo==null||!MediaType.TEXT_EVENT_STREAM.isCompatibleWith(tipo))throw new ServicoIAIndisponivelException("Formato de stream inválido");
            try(var in=res.getBody()){
                return RagSseReader.read(in,(event,data)->{
                    if("ping".equals(event)){eventos.accept("ping",Map.of());return null;}
                    Map<String,Object> obj=json.readValue(data,SSE_MAP);
                    if("resultado".equals(event))return obj;
                    if("erro".equals(event))throw statusErro(obj.get("status") instanceof Number n?n.intValue():503);
                    if("inicio".equals(event)||"delta".equals(event)||"progresso".equals(event))eventos.accept(event,obj);
                    return null;
                });
            }catch(IOException ex){throw new ServicoIAIndisponivelException("Stream interrompido",ex);}
        }));
    }
    private RuntimeException statusErro(int status){
        return switch(status){
            case 404 -> new RagApiException(HttpStatus.GONE,"IA_SESSAO_EXPIRADA","Sessão da IA expirada.");
            case 409 -> new RagApiException(HttpStatus.CONFLICT,"IA_CONVERSA_OCUPADA","Conversa ocupada.");
            case 429 -> new RagApiException(HttpStatus.TOO_MANY_REQUESTS,"IA_OCUPADA","IA ocupada. Aguarde.");
            case 400,422 -> new RagApiException(HttpStatus.BAD_REQUEST,"IA_ENTRADA_INVALIDA","Mensagem ou parâmetros inválidos.");
            case 504 -> new TempoLimiteIAException("Tempo limite da IA excedido");
            default -> new ServicoIAIndisponivelException("Serviço RAG indisponível ou stream inválido");
        };
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
