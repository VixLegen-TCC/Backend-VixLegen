package br.com.VixLegen.ProjetoVixLegen10.DTOs.Response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UsuarioResponse {

    private Long idUsuario;
    private String primeiroNome;
    private String ultimoNome;
    private String email;
    private String telefone;
    private String cpf;
    private String rg;
    private String empresa;
    private String numeroOAB;
    private LocalDate dataNascimento;
    private String estado;
    private String cidade;
    private String cep;
    private boolean ativo;
    private Long codigoCategoria;

}