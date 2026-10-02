package br.com.VixLegen.ProjetoVixLegen10.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @NotBlank
    private String nomeCompleto;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String telefone;

    @Column(unique = true)
    private String cpf;

    @Column(unique = true)
    private String cnpj;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id", nullable = false)
    private Usuario usuarioResponsavel;

    @OneToMany(mappedBy = "cliente")
    @JsonIgnore
    private List<ProcessoJuridico> processos = new ArrayList<>();

    @AssertTrue(message = "Informe CPF ou CNPJ, mas não os dois")
    @JsonIgnore
    public boolean isDocumentoValido() {
        boolean possuiCpf = cpf != null && !cpf.isBlank();
        boolean possuiCnpj = cnpj != null && !cnpj.isBlank();

        return possuiCpf ^ possuiCnpj;
    }
}
