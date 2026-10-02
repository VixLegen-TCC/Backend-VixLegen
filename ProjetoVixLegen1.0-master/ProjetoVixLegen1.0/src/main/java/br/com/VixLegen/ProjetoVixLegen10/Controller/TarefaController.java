package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.AlterarPrazoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.AlterarStatusTarefaRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.AtribuirTarefaRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.TarefaRequest;
import br.com.VixLegen.ProjetoVixLegen10.Model.Tarefa;
import br.com.VixLegen.ProjetoVixLegen10.Service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    public ResponseEntity<Tarefa> cadastrar(
            @Valid @RequestBody TarefaRequest request) {

        return ResponseEntity.ok(
                tarefaService.cadastrar(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<Tarefa>> listarTodos() {
        return ResponseEntity.ok(
                tarefaService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tarefaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarefaRequest request) {

        return ResponseEntity.ok(
                tarefaService.atualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Tarefa> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody
            AlterarStatusTarefaRequest request) {

        return ResponseEntity.ok(
                tarefaService.alterarStatus(
                        id,
                        request.getStatus()
                )
        );
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Tarefa> concluir(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tarefaService.concluir(id)
        );
    }

    @PatchMapping("/{id}/atribuir")
    public ResponseEntity<Tarefa> atribuir(
            @PathVariable Long id,
            @Valid @RequestBody AtribuirTarefaRequest request) {

        return ResponseEntity.ok(
                tarefaService.atribuir(
                        id,
                        request.getIdUsuario(),
                        request.getIdProcesso()
                )
        );
    }

    @PatchMapping("/{id}/prazo")
    public ResponseEntity<Tarefa> alterarPrazo(
            @PathVariable Long id,
            @Valid @RequestBody AlterarPrazoRequest request) {

        return ResponseEntity.ok(
                tarefaService.alterarPrazo(
                        id,
                        request.getPrazo()
                )
        );
    }
}
