package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.Service.ChatContextoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/chat/contexto")
public class ChatContextoController {
    private final ChatContextoService contexto;
    public ChatContextoController(ChatContextoService contexto) { this.contexto = contexto; }

    @GetMapping("/{tipo}/{id}")
    public ResponseEntity<Map<String, Object>> interno(@PathVariable String tipo, @PathVariable Long id) {
        return ResponseEntity.ok(Map.of("origem", "interno", "tipo", tipo, "id", id,
                "texto", contexto.contextoInterno(tipo, id)));
    }

    @PostMapping(value = "/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> arquivo(@RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(Map.of("origem", "upload", "texto", contexto.extrair(arquivo)));
    }
}
