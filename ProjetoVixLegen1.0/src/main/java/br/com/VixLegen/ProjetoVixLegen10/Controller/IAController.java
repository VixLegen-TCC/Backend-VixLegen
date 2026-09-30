package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Model.IA;
import br.com.VixLegen.ProjetoVixLegen10.Service.IAService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inteligencia-artificial")
public class IAController {

    private final IAService analiseService;

    public IAController(IAService analiseService) {
        this.analiseService = analiseService;
    }

    @PostMapping
    public ResponseEntity<IA> cadastrar(
            @Valid @RequestBody IA analise) {

        return ResponseEntity.ok(
                analiseService.cadastrar(analise)
        );
    }

    @GetMapping
    public ResponseEntity<List<IA>> listarTodos() {

        return ResponseEntity.ok(
                analiseService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<IA> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                analiseService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<IA> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody IA analise) {

        return ResponseEntity.ok(
                analiseService.atualizar(id, analise)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        analiseService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
