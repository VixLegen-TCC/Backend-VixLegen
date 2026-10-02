package br.com.VixLegen.ProjetoVixLegen10.Model;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusTarefa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
@Getter
@Setter
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarefa;

    @NotNull
    private LocalDateTime dataAtribuicao;

    @NotNull
    private LocalDateTime prazo;

    @NotBlank
    private String tipoTarefa;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusTarefa status;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoJuridico processo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioResponsavel;
}
