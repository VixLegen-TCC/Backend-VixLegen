package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import br.com.VixLegen.ProjetoVixLegen10.Enums.PrioridadeTarefa;
import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TarefaRequest {

    @NotBlank
    private String tipoTarefa;

    private String descricao;

    @NotNull
    private LocalDateTime prazo;

    @NotNull
    private StatusTarefa status;

    private PrioridadeTarefa prioridade;

    @NotNull
    private Long processoId;

    @NotNull
    private Long usuarioResponsavelId;
}
