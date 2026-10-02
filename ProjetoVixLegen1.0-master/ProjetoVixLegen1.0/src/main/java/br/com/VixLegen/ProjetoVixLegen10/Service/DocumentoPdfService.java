package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Model.DocumentoJuridico;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class DocumentoPdfService {

    private final DocumentoJuridicoService documentoService;

    public DocumentoPdfService(
            DocumentoJuridicoService documentoService) {
        this.documentoService = documentoService;
    }

    public byte[] gerarPdf(Long idDocumento) {

        DocumentoJuridico documento =
                documentoService.buscarPorId(idDocumento);

        String conteudo = documento.getConteudo() == null
                ? ""
                : documento.getConteudo();

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4;
                            margin: 2.5cm 2cm 2.5cm 3cm;
                        }

                        body {
                            font-family: "Times New Roman", serif;
                            font-size: 12pt;
                            line-height: 1.5;
                            color: #000;
                        }

                        h1, h2, h3, h4, h5, h6 {
                            page-break-after: avoid;
                        }

                        p {
                            margin: 0 0 12pt 0;
                            text-align: justify;
                        }

                        blockquote {
                            margin-left: 4cm;
                            font-size: 10pt;
                            line-height: 1.2;
                            font-style: italic;
                        }

                        table {
                            width: 100%;
                            border-collapse: collapse;
                        }

                        img {
                            max-width: 100%;
                        }
                    </style>
                </head>
                <body>
                """ + conteudo + """
                </body>
                </html>
                """;

        Document parsed = Jsoup.parse(html);
        parsed.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .charset("UTF-8");

        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.useFastMode();
            builder.withHtmlContent(
                    parsed.html(),
                    null
            );
            builder.toStream(output);
            builder.run();

            return output.toByteArray();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Não foi possível gerar o PDF da minuta",
                    exception
            );
        }
    }
}
