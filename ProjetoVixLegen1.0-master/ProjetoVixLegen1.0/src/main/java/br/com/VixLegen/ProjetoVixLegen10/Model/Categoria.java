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
    @Column(name = "codigo_categoria")
    private Long codigoCategoria;

    @NotBlank
    private String descricao;

    @NotNull
    @Column(name = "nivel_acesso")
    private Integer nivelAcesso;

    @NotNull
    @Column(name = "limite_processos_simultaneos")
    private Integer limiteProcessosSimultaneos;

    @Column(name = "permissao_visualizar")
    private boolean permissaoVisualizar;

    @Column(name = "permissao_editar")
    private boolean permissaoEditar;

    @Column(name = "permissao_excluir")
    private boolean permissaoExcluir;

    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    private List<Usuario> usuarios = new ArrayList<>();
}
