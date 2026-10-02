package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtribuirTarefaRequest {

    @NotNull
    private Long idUsuario;

    @NotNull
    private Long idProcesso;
}