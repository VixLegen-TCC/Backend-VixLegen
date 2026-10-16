package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.*;
import br.com.VixLegen.ProjetoVixLegen10.Repository.DocumentoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.NotificacaoRepository;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class ChatContextoService {
    private static final int MAX_CARACTERES = 100_000;
    private final TenantAccessService tenant;
    private final DocumentoJuridicoRepository documentos;
    private final NotificacaoRepository notificacoes;

    public ChatContextoService(TenantAccessService tenant, DocumentoJuridicoRepository documentos,
                               NotificacaoRepository notificacoes) {
        this.tenant = tenant;
        this.documentos = documentos;
        this.notificacoes = notificacoes;
    }

    // Os IDs são sempre validados no servidor usando o usuário do JWT.
    public String contextoInterno(String tipo, Long id) {
        if (id == null || id <= 0) throw new RegraNegocioException("ID do recurso obrigatório");
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "processo" -> processo(id);
            case "notificacao" -> notificacao(id);
            case "documento" -> documento(id);
            default -> throw new RegraNegocioException("Tipo suportado: processo, notificacao ou documento");
        };
    }

    private String processo(Long id) {
        ProcessoJuridico p = tenant.processo(id);
        return limitar("Processo: " + p.getNumeroProcesso() + "\nVara: " + p.getVara()
                + "\nComarca: " + p.getComarca() + "\nTribunal: " + p.getTribunal()
                + "\nInstância: " + p.getInstancia() + "\nData de abertura: " + p.getDataAbertura()
                + "\nPrazo registrado: " + p.getPrazoProcessual()
                + "\nSegredo de justiça: " + p.isSegredoJustica()
                + "\nAtenção: dados cadastrais não substituem leitura dos autos.");
    }

    private String notificacao(Long id) {
        Notificacao n = notificacoes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada"));
        if (!tenant.usuarioAtualId().equals(n.getUsuario().getIdUsuario())) {
            throw new RecursoNaoEncontradoException("Notificação não encontrada");
        }
        return limitar("Notificação: " + n.getMensagem()
                + "\nRegistrada em: " + n.getDataEnvio() + "\nTipo de referência: "
                + n.getTipoReferencia() + "\nReferência: " + n.getReferenciaId());
    }

    private String documento(Long id) {
        DocumentoJuridico d = documentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento não encontrado"));
        tenant.processo(d.getProcesso().getIdProcesso());
        if (d.getConteudo() == null || d.getConteudo().isBlank()) {
            throw new RegraNegocioException("Documento sem conteúdo textual disponível para análise");
        }
        return limitar("Documento: " + d.getNome() + "\n" + Jsoup.parse(d.getConteudo()).text());
    }

    public String extrair(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) throw new RegraNegocioException("Selecione um arquivo");
        if (arquivo.getSize() > 10L * 1024 * 1024) throw new RegraNegocioException("Limite de 10 MB por arquivo");
        String nome = arquivo.getOriginalFilename() == null ? "" : arquivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            byte[] bytes = arquivo.getBytes();
            if (nome.endsWith(".txt")) {
                return limitar(new String(bytes, StandardCharsets.UTF_8));
            }
            if (nome.endsWith(".pdf") && bytes.length >= 5
                    && new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
                try (var pdf = Loader.loadPDF(bytes)) {
                    if (pdf.getNumberOfPages() > 100) throw new RegraNegocioException("PDF acima de 100 páginas");
                    String texto = new PDFTextStripper().getText(pdf);
                    if (texto.isBlank()) throw new RegraNegocioException("PDF sem texto extraível; OCR ainda não disponível");
                    return limitar(texto);
                }
            }
            throw new RegraNegocioException("Formato não suportado. Use PDF pesquisável ou TXT");
        } catch (IOException ex) {
            throw new RegraNegocioException("Não foi possível ler o arquivo");
        }
    }

    private String limitar(String texto) {
        if (texto.isBlank()) throw new RegraNegocioException("Contexto sem texto");
        return texto.length() > MAX_CARACTERES ? texto.substring(0, MAX_CARACTERES) : texto;
    }
}
