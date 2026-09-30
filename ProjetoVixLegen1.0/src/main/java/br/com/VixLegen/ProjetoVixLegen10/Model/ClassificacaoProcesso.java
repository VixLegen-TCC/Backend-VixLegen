package br.com.VixLegen.ProjetoVixLegen10.Model;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "classificacoes_processo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassificacaoProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClassificacao;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusProcesso status;

    @NotBlank
    private String areaDireito;

    @NotBlank
    private String tipoAcao;

    @NotBlank
    private String faseProcessual;

    @NotBlank
    private String descricaoObjeto;

    @OneToOne
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoJuridico processo;
}