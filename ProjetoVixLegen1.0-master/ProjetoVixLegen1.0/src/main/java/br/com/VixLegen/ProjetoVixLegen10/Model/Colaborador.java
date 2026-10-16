package br.com.VixLegen.ProjetoVixLegen10.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "colaboradores")
@Getter
@Setter
public class Colaborador {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_organizacao_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Empresa empresaOrganizacao;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idColaborador;

    @NotBlank
    private String nomeCompleto;

    @NotBlank
    private String endereco;

    @NotBlank
    private String cidade;

    @NotBlank
    private String estado;

    @NotBlank
    private String telefone;

    @NotNull
    private LocalDate dataNascimento;
}