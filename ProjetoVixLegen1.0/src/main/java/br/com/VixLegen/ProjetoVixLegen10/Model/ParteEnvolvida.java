package br.com.VixLegen.ProjetoVixLegen10.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="partesEnvolvidas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ParteEnvolvida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idParte;

    @NotBlank
    private String posicaoCliente;

    @NotBlank
    private String parteContraria;

    @NotBlank
    private String advogadoContrario;

    @NotBlank
    private String juizResponsavel;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoJuridico processo;
}
