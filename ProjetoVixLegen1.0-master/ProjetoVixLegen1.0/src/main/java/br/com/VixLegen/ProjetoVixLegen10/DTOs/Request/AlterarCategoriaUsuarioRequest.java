package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlterarCategoriaUsuarioRequest {

    @NotNull
    private Long codigoCategoria;
}
