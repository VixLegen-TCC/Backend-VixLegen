package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProcessoJuridicoRequest {

    @NotBlank
    private String numeroProcesso;

    @NotBlank
    private String vara;

    @NotBlank
    private String comarca;

    @NotBlank
    private String tribunal;

    @NotBlank
    private String instancia;

    private boolean segredoJustica;

    @NotNull
    private LocalDate dataAbertura;

    private LocalDate dataEncerramento;

    // Prazo opcional, não calculado automaticamente.
    private LocalDate prazoProcessual;

    @NotNull
    private Long clienteId;
}
