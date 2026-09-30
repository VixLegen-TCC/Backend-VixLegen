package br.com.VixLegen.ProjetoVixLegen10.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "IA")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnalise;

    private String previsaoTempoJulgamento;

    private BigDecimal probabilidadeSucesso;

    private BigDecimal nivelConfianca;

    @NotBlank
    private String sugestoes;

    @NotBlank
    private String padroesEncontrados;

    @ManyToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoJuridico processo;
}
