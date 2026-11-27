package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.ChatIAMensagemRequest;
import br.com.VixLegen.ProjetoVixLegen10.Service.ChatIAService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;
import br.com.VixLegen.ProjetoVixLegen10.Exception.*;

@RestController
@RequestMapping("/chat/ia")
public class ChatIAController {
    private final ChatIAService chat;
    private final JsonMapper json=JsonMapper.builder().build();
    public ChatIAController(ChatIAService chat) { this.chat = chat; }
    @GetMapping("/health")
    public Map<String, Object> health() { return chat.health(); }
    @GetMapping("/sessoes")
    public List<Map<String,Object>> listar() { return chat.listar(); }
    @GetMapping("/sessoes/{id}")
    public Map<String,Object> detalhes(@PathVariable UUID id) { return chat.detalhes(id); }
    public record TituloRequest(@NotBlank @Size(max=120) String titulo) {}
    @PostMapping("/sessoes/{id}/titulo")
    public Map<String,Object> renomear(@PathVariable UUID id,@Valid @RequestBody TituloRequest pedido) {
        return chat.renomear(id,pedido.titulo());
    }
    @PostMapping("/sessoes")
    public ResponseEntity<Map<String, Object>> criar() {
        return ResponseEntity.status(HttpStatus.CREATED).body(chat.criar());
    }
    @PostMapping("/sessoes/{id}/mensagens")
    public Map<String, Object> perguntar(@PathVariable UUID id, @Valid @RequestBody ChatIAMensagemRequest pedido) {
        return chat.perguntar(id, pedido.mensagem());
    }
    @PostMapping(value="/sessoes/{id}/mensagens/stream", produces="text/event-stream")
    public void perguntarStream(@PathVariable UUID id,@Valid @RequestBody ChatIAMensagemRequest pedido,HttpServletResponse response) throws IOException {
        // Thread MVC original: SecurityContext e lock permanecem válidos até salvar.
        // A resposta só é comprometida ao emitir o primeiro evento autorizado.
        BiConsumer<String,Map<String,Object>> emit=(event,data)->{
            try{
                if(!response.isCommitted()){
                    response.setContentType("text/event-stream;charset=UTF-8");
                    response.setHeader("Cache-Control","no-cache, no-transform");
                    response.setHeader("X-Accel-Buffering","no");
                }
                String frame="ping".equals(event)?": heartbeat\n\n":"event: "+event+"\ndata: "+json.writeValueAsString(data)+"\n\n";
                response.getOutputStream().write(frame.getBytes(StandardCharsets.UTF_8));response.flushBuffer();
            }catch(IOException ex){throw new UncheckedIOException(ex);}
        };
        try{
            var result=chat.perguntarStream(id,pedido.mensagem(),emit);
            emit.accept("resultado",result); // apenas depois do commit do histórico
        }catch(UncheckedIOException ex){ /* navegador desconectou; não reenvia nem inventa resultado */ }
        catch(RuntimeException ex){
            if(!response.isCommitted())throw ex; // handlers normais antes do primeiro evento
            int status=ex instanceof RagApiException r?r.status().value():ex instanceof TempoLimiteIAException?504:ex instanceof RecursoNaoEncontradoException?404:503;
            try{emit.accept("erro",Map.of("status",status,"mensagem","Não foi possível confirmar a resposta. Recarregue o histórico."));}catch(UncheckedIOException ignored){}
        }
    }
    public record AnexoRequest(@NotBlank @Size(max=240) String arquivo,
        @NotBlank @Size(max=6990508) String conteudo_base64) {}
    @PostMapping("/sessoes/{id}/anexo")
    public ResponseEntity<Map<String,Object>> anexar(@PathVariable UUID id,@Valid @RequestBody AnexoRequest pedido){
        return ResponseEntity.status(HttpStatus.CREATED).body(chat.anexar(id,pedido.arquivo(),pedido.conteudo_base64()));
    }
    @PostMapping("/sessoes/{id}/anexo/remover")
    public ResponseEntity<Void> removerAnexo(@PathVariable UUID id){chat.removerAnexo(id);return ResponseEntity.noContent().build();}
    @GetMapping("/processos")
    public List<Map<String,Object>> processos(){return chat.processosDisponiveis();}
    public record ProcessoRequest(@NotBlank @jakarta.validation.constraints.Pattern(regexp="[1-9][0-9]{0,18}") String processo_id){}
    @PostMapping("/sessoes/{id}/processo")
    public Map<String,Object> vincularProcesso(@PathVariable UUID id,@Valid @RequestBody ProcessoRequest pedido){
        long pid;
        try{pid=Long.parseLong(pedido.processo_id());}catch(NumberFormatException ex){throw new br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException("ID de processo inválido");}
        return chat.vincularProcesso(id,pid);
    }
    @PostMapping("/sessoes/{id}/reset")
    public Map<String, Object> resetar(@PathVariable UUID id) { return chat.resetar(id); }
    @PostMapping("/sessoes/{id}/encerrar")
    public ResponseEntity<Void> encerrar(@PathVariable UUID id) {
        chat.encerrar(id); return ResponseEntity.noContent().build();
    }
}
