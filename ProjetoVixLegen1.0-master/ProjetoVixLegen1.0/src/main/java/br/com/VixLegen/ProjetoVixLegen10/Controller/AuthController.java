package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.CadastroPublicoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.LoginRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioAtualizacaoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.LoginResponse;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.UsuarioResponse;
import br.com.VixLegen.ProjetoVixLegen10.Service.AuthService;
import br.com.VixLegen.ProjetoVixLegen10.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(
            AuthService authService,
            UsuarioService usuarioService) {

        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(
            @Valid @RequestBody CadastroPublicoRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        usuarioService.cadastrarPublico(
                                request
                        )
                );
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> perfil(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                usuarioService.buscarPorId(
                        Long.valueOf(jwt.getSubject())
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> atualizarPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            UsuarioAtualizacaoRequest request) {

        return ResponseEntity.ok(
                usuarioService.atualizar(
                        Long.valueOf(jwt.getSubject()),
                        request
                )
        );
    }
}
