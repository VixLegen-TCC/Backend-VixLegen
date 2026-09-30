package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioAtualizacaoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.UsuarioResponse;
import br.com.VixLegen.ProjetoVixLegen10.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(
            @Valid @RequestBody UsuarioRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAtualizacaoRequest request) {

        return ResponseEntity.ok(
                usuarioService.atualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        usuarioService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponse> buscarPorEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(
                usuarioService.buscarPorEmail(email)
        );
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<UsuarioResponse> buscarPorCpf(
            @PathVariable String cpf) {

        return ResponseEntity.ok(
                usuarioService.buscarPorCpf(cpf)
        );
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<UsuarioResponse>> listarAtivos() {

        return ResponseEntity.ok(
                usuarioService.listarAtivos()
        );
    }
}
