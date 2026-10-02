package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassificacaoProcessoRequest {

    @NotNull
    private StatusProcesso status;

    @NotBlank
    private String areaDireito;

    @NotBlank
    private String tipoAcao;

    @NotBlank
    private String faseProcessual;

    @NotBlank
    private String descricaoObjeto;

    @NotNull
    private Long processoId;
}
