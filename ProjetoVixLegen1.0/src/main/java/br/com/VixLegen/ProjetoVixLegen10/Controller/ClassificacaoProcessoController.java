package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Model.ClassificacaoProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Service.ClassificacaoProcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classificacoes-processo")
public class ClassificacaoProcessoController {

    @Autowired
    private ClassificacaoProcessoService service;

    @PostMapping
    public ClassificacaoProcesso cadastrar(
            @RequestBody ClassificacaoProcesso classificacao) {

        return service.cadastrar(classificacao);
    }

    @GetMapping
    public List<ClassificacaoProcesso> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ClassificacaoProcesso buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ClassificacaoProcesso atualizar(
            @PathVariable Long id,
            @RequestBody ClassificacaoProcesso classificacao) {

        return service.atualizar(id, classificacao);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClassificacaoProcesso> alterarStatus(
            @PathVariable Long id,
            @RequestBody StatusProcesso status) {

        return ResponseEntity.ok(
                service.alterarStatus(id, status)
        );
    }
}
