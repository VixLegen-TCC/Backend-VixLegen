package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusTarefa;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlterarStatusTarefaRequest {

    @NotNull
    private StatusTarefa status;
}
