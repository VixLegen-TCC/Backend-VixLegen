package br.com.VixLegen.ProjetoVixLegen10.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @NotBlank
    private String primeiroNome;

    @NotBlank
    private String ultimoNome;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @NotBlank
    private String senhaHash;

    @NotBlank
    private String telefone;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String cpf;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String rg;

    // Nome legado exibido no perfil. O vínculo de acesso é sempre empresaOrganizacao.
    @NotBlank
    private String empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_organizacao_id")
    private Empresa empresaOrganizacao;

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

    private boolean ativo;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    @JsonIgnore
    private String fotoPerfil;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "usuarioResponsavel")
    @JsonIgnore
    private List<Tarefa> tarefas = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Notificacao> notificacoes = new ArrayList<>();

    @OneToMany(mappedBy = "usuarioResponsavel")
    @JsonIgnore
    private List<Cliente> clientesResponsaveis = new ArrayList<>();
}
