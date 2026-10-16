package br.com.VixLegen.ProjetoVixLegen10.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Rich text jurídico: impede execução de scripts/event handlers no navegador.
 * Mantém estilos de texto estritamente permitidos (sem url(), expression ou imports).
 * Conteúdo legado também é limpo antes de ser enviado ao frontend.
 */
@Component
public class SanitizadorMinuta {
    private static final Set<String> ESTILOS = Set.of(
            "text-align", "font-weight", "font-style", "text-decoration",
            "font-size", "font-family", "line-height", "color", "background-color",
            "margin", "margin-left", "margin-right", "margin-top", "margin-bottom",
            "padding-left", "padding-right", "border-bottom", "text-indent");
    private static final Safelist PERMITIDOS = Safelist.relaxed()
            .addTags("div", "span", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "u", "s", "sub", "sup")
            .addAttributes(":all", "style")
            .addProtocols("img", "src", "data");

    public String limpar(String html) {
        if (html == null) return null;
        Document doc = Jsoup.parseBodyFragment(html);
        for (Element elemento : doc.getAllElements()) {
            if (elemento.hasAttr("style")) {
                String estiloSeguro = filtrarEstilo(elemento.attr("style"));
                if (estiloSeguro.isBlank()) elemento.removeAttr("style");
                else elemento.attr("style", estiloSeguro);
            }
            // PDF server-side nunca deve solicitar recursos remotos arbitrários.
            if (elemento.normalName().equals("img") && elemento.hasAttr("src")) {
                String src = elemento.attr("src");
                if (!src.matches("(?is)^data:image/(?:png|jpeg|gif|webp);base64,[a-z0-9+/=]{1,400000}$")) {
                    elemento.removeAttr("src");
                }
            }
        }
        return Jsoup.clean(doc.body().html(), PERMITIDOS);
    }

    private String filtrarEstilo(String estilo) {
        List<String> seguros = new ArrayList<>();
        for (String declaracao : estilo.split(";")) {
            int separador = declaracao.indexOf(':');
            if (separador <= 0) continue;
            String propriedade = declaracao.substring(0, separador).trim().toLowerCase(Locale.ROOT);
            String valor = declaracao.substring(separador + 1).trim();
            // Não deixar valores executáveis nem requisições de recursos externos via CSS.
            if (ESTILOS.contains(propriedade) && valor.length() <= 90
                    && valor.matches("[#a-zA-Z0-9.,%/ +\\-]*") && !valor.isBlank()) {
                seguros.add(propriedade + ": " + valor);
            }
        }
        return String.join("; ", seguros);
    }
}
