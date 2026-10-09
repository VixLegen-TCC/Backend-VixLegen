package br.com.VixLegen.ProjetoVixLegen10.Model;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusNotificacao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacao;

    @NotBlank
    private String mensagem;

    @NotNull
    private LocalDateTime dataEnvio;

    @NotBlank
    private String canal;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusNotificacao status;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(name = "chave_alerta", unique = true, length = 180)
    private String chaveAlerta;

    @Column(name = "tipo_referencia", length = 20)
    private String tipoReferencia;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "etapa_alerta", length = 20)
    private String etapaAlerta;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @PrePersist
    public void preencherPadroes() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }
}
