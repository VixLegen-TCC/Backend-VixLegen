package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Model.DocumentoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Service.DocumentoJuridicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documentos")
public class DocumentoJuridicoController {

    private final DocumentoJuridicoService service;

    public DocumentoJuridicoController(
            DocumentoJuridicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DocumentoJuridico> cadastrar(
            @Valid @RequestBody DocumentoJuridico documento) {

        return ResponseEntity.ok(
                service.cadastrar(documento)
        );
    }

    @GetMapping
    public ResponseEntity<List<DocumentoJuridico>> listarTodos() {

        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoJuridico> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentoJuridico> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoJuridico documento) {

        return ResponseEntity.ok(
                service.atualizar(id, documento)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        service.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/processo/{idProcesso}")
    public ResponseEntity<List<DocumentoJuridico>> listarPorProcesso(
            @PathVariable Long idProcesso) {

        return ResponseEntity.ok(
                service.listarPorProcesso(idProcesso)
        );
    }

    @PatchMapping("/{id}/anexar")
    public ResponseEntity<DocumentoJuridico> anexar(
            @PathVariable Long id,
            @RequestBody String arquivo) {

        return ResponseEntity.ok(
                service.anexar(id, arquivo)
        );
    }

    @PatchMapping("/{id}/remover")
    public ResponseEntity<DocumentoJuridico> remover(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.remover(id)
        );
    }
}
