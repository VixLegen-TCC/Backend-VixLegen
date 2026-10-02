package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Model.Notificacao;
import br.com.VixLegen.ProjetoVixLegen10.Service.NotificacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @PostMapping
    public ResponseEntity<Notificacao> cadastrar(
            @Valid @RequestBody Notificacao notificacao) {

        return ResponseEntity.ok(
                notificacaoService.cadastrar(notificacao)
        );
    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listarTodos() {

        return ResponseEntity.ok(
                notificacaoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacao> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificacaoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notificacao> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Notificacao notificacao) {

        return ResponseEntity.ok(
                notificacaoService.atualizar(id, notificacao)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        notificacaoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enviar")
    public ResponseEntity<Notificacao> enviar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificacaoService.enviar(id)
        );
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Notificacao> cancelar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificacaoService.cancelar(id)
        );
    }
}