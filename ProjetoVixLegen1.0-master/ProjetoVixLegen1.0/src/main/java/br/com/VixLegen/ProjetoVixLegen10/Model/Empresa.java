package br.com.VixLegen.ProjetoVixLegen10.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Organização jurídica, isolada por idEmpresa (o nome não é identidade). */
@Entity
@Table(name = "empresas")
@Getter
@Setter
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmpresa;

    @Column(nullable = false, length = 160)
    private String nome;

    // Dono inicial para migração/auditoria: não significa permissão global de acesso.
    @Column(name = "criador_usuario_id", unique = true)
    private Long criadorUsuarioId;
}
