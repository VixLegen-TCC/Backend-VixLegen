package br.com.VixLegen.ProjetoVixLegen10.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo;
    private Instant expiraEm;
    private Long idUsuario;
    private String email;
    private String nome;
    private Long codigoCategoria;
    private Integer nivelAcesso;
    private String perfilAcesso;
}
