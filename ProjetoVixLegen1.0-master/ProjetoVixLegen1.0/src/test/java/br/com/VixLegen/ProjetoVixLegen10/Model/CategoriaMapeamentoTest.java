package br.com.VixLegen.ProjetoVixLegen10.Model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Impede regressao dos nomes fisicos no banco legado migrado. */
class CategoriaMapeamentoTest {

    @Test
    void nomesFisicosSaoExplicitos() throws NoSuchFieldException {
        assertEquals("categoria", Categoria.class.getAnnotation(Table.class).name());
        conferirColuna("codigoCategoria", "codigo_categoria");
        conferirColuna("nivelAcesso", "nivel_acesso");
        conferirColuna("limiteProcessosSimultaneos", "limite_processos_simultaneos");
        conferirColuna("permissaoVisualizar", "permissao_visualizar");
        conferirColuna("permissaoEditar", "permissao_editar");
        conferirColuna("permissaoExcluir", "permissao_excluir");
        JoinColumn rel = Usuario.class.getDeclaredField("categoria").getAnnotation(JoinColumn.class);
        assertNotNull(rel);
        assertEquals("categoria_id", rel.name());
        assertEquals("codigo_categoria", rel.referencedColumnName());
    }

    private void conferirColuna(String nomeCampo, String nomeBanco) throws NoSuchFieldException {
        Column column = Categoria.class.getDeclaredField(nomeCampo).getAnnotation(Column.class);
        assertNotNull(column, "Falta @Column para " + nomeCampo);
        assertEquals(nomeBanco, column.name());
    }
}
