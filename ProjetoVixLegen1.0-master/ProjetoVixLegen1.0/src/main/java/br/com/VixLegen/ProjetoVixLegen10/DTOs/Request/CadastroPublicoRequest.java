package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CadastroPublicoRequest {

    @NotBlank
    private String primeiroNome;

    @NotBlank
    private String ultimoNome;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "A senha deve possuir no mínimo 8 caracteres")
    private String senha;

    @NotBlank
    private String telefone;

    @NotBlank
    private String cpf;

    @NotBlank
    private String rg;

    @NotBlank
    private String empresa;

    @NotBlank
    private String numeroOAB;

    @NotNull
    private LocalDate dataNascimento;

    @NotBlank
    private String estado;

    @NotBlank
    private String cidade;

    @NotBlank
    private String cep;
}
