package br.com.VixLegen.ProjetoVixLegen10.Service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SanitizadorMinutaTest {
    private final SanitizadorMinuta sanitizador = new SanitizadorMinuta();

    @Test
    void removeScriptsEventosHtmlEImagemRemota() {
        String html = "<h2 onclick='alert(1)'>Petição</h2>"
                + "<script>alert(2)</script>"
                + "<img src='http://127.0.0.1:8080/admin' onerror='alert(3)'>"
                + "<a href='javascript:alert(4)'>Clique</a>";
        String limpo = sanitizador.limpar(html).toLowerCase();
        assertTrue(limpo.contains("petição"));
        assertFalse(limpo.contains("<script"));
        assertFalse(limpo.contains("onclick"));
        assertFalse(limpo.contains("onerror"));
        assertFalse(limpo.contains("javascript:"));
        assertFalse(limpo.contains("127.0.0.1"));
    }

    @Test
    void mantemApenasCssDeFormatacaoSemUrlsExternas() {
        String html = "<p style='text-align:center; margin-left: 4cm; "
                + "background-image:url(https://servidor-externo/roubo);"
                + " color: red'>Texto jurídico</p>";
        String limpo = sanitizador.limpar(html);
        assertTrue(limpo.contains("text-align: center"));
        assertTrue(limpo.contains("margin-left: 4cm"));
        assertFalse(limpo.contains("background-image"));
        assertFalse(limpo.contains("servidor-externo"));
    }
}
