package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Service.ProcessoJuridicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/processos")
public class ProcessoJuridicoController {

    private final ProcessoJuridicoService processoService;

    public ProcessoJuridicoController(
            ProcessoJuridicoService processoService) {

        this.processoService = processoService;
    }

    @PostMapping
    public ResponseEntity<ProcessoJuridico> cadastrar(
            @Valid @RequestBody ProcessoJuridico processo) {

        return ResponseEntity.ok(
                processoService.cadastrar(processo)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProcessoJuridico>> listarTodos() {

        return ResponseEntity.ok(
                processoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessoJuridico> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                processoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessoJuridico> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProcessoJuridico processo) {

        return ResponseEntity.ok(
                processoService.atualizar(id, processo)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        processoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProcessoJuridico>> listarPorStatus(
            @PathVariable StatusProcesso status) {

        return ResponseEntity.ok(
                processoService.listarPorStatus(status)
        );
    }

    @GetMapping("/{id}/situacao")
    public ResponseEntity<StatusProcesso> consultarSituacao(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                processoService.consultarSituacao(id)
        );
    }
}