package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Enums.PerfilAcesso;
import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import java.util.ArrayList;
import java.util.List;

/** Mesma regra de permissões para emissão e validação do token. */
public final class PermissoesJwt {
    private PermissoesJwt() {}

    public static String escopos(Categoria categoria) {
        if (categoria == null) return "";
        List<String> escopos = new ArrayList<>();
        if (categoria.isPermissaoVisualizar()) escopos.add("visualizar");
        if (categoria.isPermissaoEditar()) escopos.add("editar");
        if (categoria.isPermissaoExcluir()) escopos.add("excluir");
        return String.join(" ", escopos);
    }

    public static String perfil(Categoria categoria) {
        if (categoria == null) return null;
        PerfilAcesso perfil = PerfilAcesso.porNivel(categoria.getNivelAcesso());
        return perfil == null ? null : perfil.name();
    }
}
