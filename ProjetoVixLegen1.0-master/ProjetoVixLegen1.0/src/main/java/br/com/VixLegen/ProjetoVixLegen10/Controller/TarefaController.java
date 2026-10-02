package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.AlterarPrazoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.AtribuirTarefaRequest;
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
            @Valid @RequestBody Tarefa tarefa) {

        return ResponseEntity.ok(
                tarefaService.cadastrar(tarefa)
        );
    }

    @GetMapping
    public ResponseEntity<List<Tarefa>> listarTodos() {
        return ResponseEntity.ok(tarefaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Tarefa tarefa) {

        return ResponseEntity.ok(
                tarefaService.atualizar(id, tarefa)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Tarefa> concluir(
            @PathVariable Long id) {

        return ResponseEntity.ok(tarefaService.concluir(id));
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
