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
    @Column(name = "id_usuario")
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
    @Column(name = "numero_oab", nullable = false)
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

    // Nulo em cadastros legados; a primeira emissão considera versão zero.
    // Alterações de segurança e logout invalidam todos os tokens anteriores.
    @Column(name = "auth_version")
    private Long authVersion = 0L;


    @Lob
    @Column(columnDefinition = "LONGTEXT")
    @JsonIgnore
    private String fotoPerfil;

    @ManyToOne
    @JoinColumn(name = "categoria_id", referencedColumnName = "codigo_categoria", nullable = false)
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
