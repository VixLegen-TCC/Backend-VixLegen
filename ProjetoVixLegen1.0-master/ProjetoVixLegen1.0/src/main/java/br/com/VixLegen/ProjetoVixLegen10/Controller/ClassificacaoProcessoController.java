package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.ClassificacaoProcessoRequest;
import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Model.ClassificacaoProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Service.ClassificacaoProcessoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classificacoes-processo")
public class ClassificacaoProcessoController {

    private final ClassificacaoProcessoService service;

    public ClassificacaoProcessoController(
            ClassificacaoProcessoService service) {
        this.service = service;
    }

    @PostMapping
    public ClassificacaoProcesso cadastrar(
            @Valid @RequestBody
            ClassificacaoProcessoRequest request) {

        return service.cadastrar(request);
    }

    @GetMapping
    public List<ClassificacaoProcesso> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ClassificacaoProcesso buscarPorId(
            @PathVariable Long id) {

        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ClassificacaoProcesso atualizar(
            @PathVariable Long id,
            @Valid @RequestBody
            ClassificacaoProcessoRequest request) {

        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClassificacaoProcesso>
    alterarStatus(
            @PathVariable Long id,
            @RequestBody StatusProcesso status) {

        return ResponseEntity.ok(
                service.alterarStatus(
                        id,
                        status
                )
        );
    }
}
