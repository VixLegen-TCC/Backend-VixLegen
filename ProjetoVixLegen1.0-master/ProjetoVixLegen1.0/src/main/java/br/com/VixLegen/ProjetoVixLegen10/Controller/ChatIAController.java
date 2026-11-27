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

@RestController
@RequestMapping("/chat/ia")
public class ChatIAController {
    private final ChatIAService chat;
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
    @PostMapping("/sessoes/{id}/reset")
    public Map<String, Object> resetar(@PathVariable UUID id) { return chat.resetar(id); }
    @PostMapping("/sessoes/{id}/encerrar")
    public ResponseEntity<Void> encerrar(@PathVariable UUID id) {
        chat.encerrar(id); return ResponseEntity.noContent().build();
    }
}
