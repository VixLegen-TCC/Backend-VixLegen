package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Model.MovimentacaoProcessual;
import br.com.VixLegen.ProjetoVixLegen10.Service.MovimentacaoProcessualService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoProcessualController {

    private final MovimentacaoProcessualService movimentacaoService;

    public MovimentacaoProcessualController(
            MovimentacaoProcessualService movimentacaoService) {

        this.movimentacaoService = movimentacaoService;
    }

    @PostMapping
    public ResponseEntity<MovimentacaoProcessual> cadastrar(
            @Valid @RequestBody MovimentacaoProcessual movimentacao) {

        return ResponseEntity.ok(
                movimentacaoService.cadastrar(movimentacao)
        );
    }

    @GetMapping
    public ResponseEntity<List<MovimentacaoProcessual>> listarTodos() {

        return ResponseEntity.ok(
                movimentacaoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoProcessual> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                movimentacaoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimentacaoProcessual> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoProcessual movimentacao) {

        return ResponseEntity.ok(
                movimentacaoService.atualizar(id, movimentacao)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        movimentacaoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/processo/{idProcesso}")
    public ResponseEntity<MovimentacaoProcessual> registrar(
            @PathVariable Long idProcesso,
            @Valid @RequestBody MovimentacaoProcessual movimentacao) {

        return ResponseEntity.ok(
                movimentacaoService.registrar(idProcesso, movimentacao)
        );
    }
}