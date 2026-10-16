package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.ArquivoInvalidoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ArquivoMuitoGrandeException;
import br.com.VixLegen.ProjetoVixLegen10.Repository.DocumentoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.NotificacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatContextoUploadTest {
    private final ChatContextoService servico = new ChatContextoService(
            mock(TenantAccessService.class),
            mock(DocumentoJuridicoRepository.class),
            mock(NotificacaoRepository.class));

    @Test
    void textoSimplesPodeSerAnalisado() {
        var arquivo = new MockMultipartFile("arquivo", "caso.txt", "text/plain",
                "Texto de exemplo para análise".getBytes(StandardCharsets.UTF_8));
        assertEquals("Texto de exemplo para análise", servico.extrair(arquivo));
    }

    @Test
    void pdfFalsoEhRejeitado() {
        var arquivo = new MockMultipartFile("arquivo", "caso.pdf", "application/pdf",
                "não é um PDF".getBytes(StandardCharsets.UTF_8));
        assertThrows(ArquivoInvalidoException.class, () -> servico.extrair(arquivo));
    }

    @Test
    void arquivoVazioEhRejeitado() {
        var arquivo = new MockMultipartFile("arquivo", "vazio.txt", "text/plain", new byte[0]);
        assertThrows(ArquivoInvalidoException.class, () -> servico.extrair(arquivo));
    }

    @Test
    void textoComUtf8InvalidoEhRejeitado() {
        var arquivo = new MockMultipartFile("arquivo", "erro.txt", "text/plain",
                new byte[] {(byte) 0xC3, (byte) 0x28});
        assertThrows(ArquivoInvalidoException.class, () -> servico.extrair(arquivo));
    }

    @Test
    void uploadAcimaDoLimiteEhRejeitado() {
        var arquivo = new MockMultipartFile("arquivo", "grande.txt", "text/plain",
                new byte[10 * 1024 * 1024 + 1]);
        assertThrows(ArquivoMuitoGrandeException.class, () -> servico.extrair(arquivo));
    }
}
