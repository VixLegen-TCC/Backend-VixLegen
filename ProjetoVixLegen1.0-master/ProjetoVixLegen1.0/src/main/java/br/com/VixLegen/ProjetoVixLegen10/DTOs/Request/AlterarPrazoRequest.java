package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AlterarPrazoRequest {

    @NotNull
    private LocalDateTime prazo;
}
