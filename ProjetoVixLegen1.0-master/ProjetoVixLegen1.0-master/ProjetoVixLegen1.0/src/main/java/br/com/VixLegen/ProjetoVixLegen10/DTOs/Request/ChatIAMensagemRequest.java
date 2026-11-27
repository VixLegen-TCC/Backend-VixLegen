package br.com.VixLegen.ProjetoVixLegen10.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatIAMensagemRequest(@NotBlank @Size(max = 4000) String mensagem) {}
