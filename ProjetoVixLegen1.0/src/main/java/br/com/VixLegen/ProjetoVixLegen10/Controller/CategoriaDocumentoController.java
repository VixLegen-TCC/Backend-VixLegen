package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Model.CategoriaDocumento;
import br.com.VixLegen.ProjetoVixLegen10.Service.CategoriaDocumentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias-documento")
public class CategoriaDocumentoController {

    private final CategoriaDocumentoService service;

    public CategoriaDocumentoController(
            CategoriaDocumentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoriaDocumento> cadastrar(
            @Valid @RequestBody CategoriaDocumento categoria) {

        return ResponseEntity.ok(
                service.cadastrar(categoria)
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDocumento>> listarTodos() {

        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDocumento> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDocumento> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDocumento categoria) {

        return ResponseEntity.ok(
                service.atualizar(id, categoria)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        service.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
