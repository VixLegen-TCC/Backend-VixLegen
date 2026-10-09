package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.CadastroPublicoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.LoginRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioAtualizacaoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.LoginResponse;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.UsuarioResponse;
import br.com.VixLegen.ProjetoVixLegen10.Service.AuthService;
import br.com.VixLegen.ProjetoVixLegen10.Service.UsuarioService;
import jakarta.validation.Valid;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import java.util.Map;
import java.util.Base64;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public AuthController(
            AuthService authService,
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository) {

        this.authService = authService;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
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
    private Usuario usuarioAutenticado(Jwt jwt) {
        return usuarioRepository.findById(Long.valueOf(jwt.getSubject()))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    @GetMapping("/me/foto")
    public ResponseEntity<Map<String, String>> foto(@AuthenticationPrincipal Jwt jwt) {
        String foto = usuarioAutenticado(jwt).getFotoPerfil();
        return ResponseEntity.ok(foto == null ? Map.of() : Map.of("foto", foto));
    }

    @PutMapping("/me/foto")
    public ResponseEntity<Void> atualizarFoto(@AuthenticationPrincipal Jwt jwt,
                                               @RequestBody Map<String, String> dados) {
        String foto = dados.get("foto");
        if (foto == null || !foto.matches("^data:image/(jpeg|png|webp);base64,[A-Za-z0-9+/=]+$")) {
            throw new RegraNegocioException("Foto inválida: envie JPG, PNG ou WebP.");
        }
        String mime = foto.substring(5, foto.indexOf(';'));
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(foto.substring(foto.indexOf(',') + 1));
        } catch (IllegalArgumentException exception) {
            throw new RegraNegocioException("Imagem em base64 inválida");
        }
        if (bytes.length == 0 || bytes.length > 256 * 1024) {
            throw new RegraNegocioException("A foto deve ter até 256 KB");
        }
        boolean jpg = mime.equals("image/jpeg") && bytes.length > 2
            && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8;
        boolean png = mime.equals("image/png") && bytes.length > 7
            && (bytes[0] & 0xff) == 137 && bytes[1] == 80 && bytes[2] == 78 && bytes[3] == 71;
        boolean webp = mime.equals("image/webp") && bytes.length > 12
            && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
            && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        if (!jpg && !png && !webp) {
            throw new RegraNegocioException("O conteúdo não corresponde ao formato da imagem");
        }
        Usuario usuario = usuarioAutenticado(jwt);
        usuario.setFotoPerfil(foto);
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/foto")
    public ResponseEntity<Void> removerFoto(@AuthenticationPrincipal Jwt jwt) {
        Usuario usuario = usuarioAutenticado(jwt);
        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

}
