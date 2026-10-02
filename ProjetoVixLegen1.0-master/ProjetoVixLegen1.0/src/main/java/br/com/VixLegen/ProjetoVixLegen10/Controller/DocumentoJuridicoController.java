package br.com.VixLegen.ProjetoVixLegen10.Controller;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.DocumentoJuridicoRequest;
import br.com.VixLegen.ProjetoVixLegen10.Model.DocumentoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Service.DocumentoJuridicoService;
import br.com.VixLegen.ProjetoVixLegen10.Service.DocumentoPdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/documentos")
public class DocumentoJuridicoController {

    private final DocumentoJuridicoService service;
    private final DocumentoPdfService pdfService;

    public DocumentoJuridicoController(
            DocumentoJuridicoService service,
            DocumentoPdfService pdfService) {

        this.service = service;
        this.pdfService = pdfService;
    }

    @PostMapping
    public ResponseEntity<DocumentoJuridico> cadastrar(
            @Valid @RequestBody DocumentoJuridicoRequest request) {

        return ResponseEntity.ok(
                service.cadastrar(request)
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

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable Long id) {

        DocumentoJuridico documento =
                service.buscarPorId(id);

        byte[] pdf = pdfService.gerarPdf(id);

        String nomeSeguro = documento.getNome()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        String nomeArquivo = URLEncoder.encode(
                nomeSeguro + ".pdf",
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + nomeArquivo
                )
                .body(pdf);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentoJuridico> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoJuridicoRequest request) {

        return ResponseEntity.ok(
                service.atualizar(id, request)
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
