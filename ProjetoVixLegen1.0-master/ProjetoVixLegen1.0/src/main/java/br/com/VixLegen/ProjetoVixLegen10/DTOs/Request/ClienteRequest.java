package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequest {

    @NotBlank
    private String nomeCompleto;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String telefone;

    private String cpf;

    private String cnpj;

    @NotNull
    private Long usuarioResponsavelId;
}
