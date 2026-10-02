package br.com.VixLegen.ProjetoVixLegen10.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacaoProcessual")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovimentacaoProcessual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimentacao;

    @NotNull
    private LocalDateTime data;

    @NotBlank
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoJuridico processo;
}
