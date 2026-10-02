package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentoJuridicoRequest {

    @NotBlank
    private String nome;

    private String conteudo;

    private String tipoArquivo;

    private Long tamanhoArquivo;

    @NotNull
    private Long processoId;

    @NotNull
    private Long categoriaDocumentoId;
}
