package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Model.Colaborador;
import br.com.VixLegen.ProjetoVixLegen10.Service.ColaboradorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    public ResponseEntity<Colaborador> cadastrar(
            @Valid @RequestBody Colaborador colaborador) {

        return ResponseEntity.ok(
                colaboradorService.cadastrar(colaborador)
        );
    }

    @GetMapping
    public ResponseEntity<List<Colaborador>> listarTodos() {

        return ResponseEntity.ok(
                colaboradorService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Colaborador> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                colaboradorService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Colaborador> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Colaborador colaborador) {

        return ResponseEntity.ok(
                colaboradorService.atualizar(id, colaborador)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        colaboradorService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}