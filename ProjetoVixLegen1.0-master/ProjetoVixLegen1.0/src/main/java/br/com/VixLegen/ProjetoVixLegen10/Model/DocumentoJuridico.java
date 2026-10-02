package br.com.VixLegen.ProjetoVixLegen10.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentosJuridicos")
@Getter
@Setter
public class DocumentoJuridico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocumento;

    @NotBlank
    private String nome;

    @NotNull
    private LocalDateTime dataCadastro;

    private String arquivo;

    private String tipoArquivo;

    private Long tamanhoArquivo;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoJuridico processo;

    @ManyToOne
    @JoinColumn(name = "categoria_documento_id", nullable = false)
    private CategoriaDocumento categoriaDocumento;
}
