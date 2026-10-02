package br.com.VixLegen.ProjetoVixLegen10.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codigoCategoria;

    @NotBlank
    private String descricao;

    @NotNull
    private Integer nivelAcesso;

    @NotNull
    private Integer limiteProcessosSimultaneos;

    private boolean permissaoVisualizar;

    private boolean permissaoEditar;

    private boolean permissaoExcluir;

    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    private List<Usuario> usuarios = new ArrayList<>();
}
