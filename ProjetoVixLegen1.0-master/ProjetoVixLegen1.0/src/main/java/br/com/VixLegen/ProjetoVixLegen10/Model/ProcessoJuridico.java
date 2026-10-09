package br.com.VixLegen.ProjetoVixLegen10.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "processosJuridicos")
@Getter
@Setter
public class ProcessoJuridico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProcesso;

    @Column(unique = true, nullable = false)
    @NotBlank
    private String numeroProcesso;

    @NotBlank
    private String vara;

    @NotBlank
    private String comarca;

    @NotBlank
    private String tribunal;

    @NotBlank
    private String instancia;

    private boolean segredoJustica;

    @NotNull
    private LocalDate dataAbertura;

    private LocalDate dataEncerramento;

    // Prazo informado pelo advogado, separado da data de encerramento.
    private LocalDate prazoProcessual;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ClassificacaoProcesso classificacao;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ParteEnvolvida> partesEnvolvidas = new ArrayList<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DocumentoJuridico> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Tarefa> tarefas = new ArrayList<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MovimentacaoProcessual> movimentacoes = new ArrayList<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<IA> analisesIA = new ArrayList<>();
}
