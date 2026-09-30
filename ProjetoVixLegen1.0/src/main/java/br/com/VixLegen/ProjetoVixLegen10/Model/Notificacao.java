package br.com.VixLegen.ProjetoVixLegen10.Model;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusNotificacao;
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

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
