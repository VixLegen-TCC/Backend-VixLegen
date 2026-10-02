package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Model.ParteEnvolvida;
import br.com.VixLegen.ProjetoVixLegen10.Service.ParteEnvolvidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partes-envolvidas")
public class ParteEnvolvidaController {

    @Autowired
    private ParteEnvolvidaService service;

    @PostMapping
    public ParteEnvolvida cadastrar(@RequestBody ParteEnvolvida parte) {
        return service.cadastrar(parte);
    }

    @GetMapping
    public List<ParteEnvolvida> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ParteEnvolvida buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ParteEnvolvida atualizar(
            @PathVariable Long id,
            @RequestBody ParteEnvolvida parte) {

        return service.atualizar(id, parte);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
