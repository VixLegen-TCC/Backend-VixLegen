package br.com.VixLegen.ProjetoVixLegen10.Enums;

public enum PerfilAcesso {

    ADMIN(1),
    ADVOGADO_SENIOR(2),
    ADVOGADO_JUNIOR(3),
    ESTAGIARIO(4);

    private final int nivelAcesso;

    PerfilAcesso(int nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    public int getNivelAcesso() {
        return nivelAcesso;
    }

    public static PerfilAcesso porNivel(Integer nivelAcesso) {

        if (nivelAcesso == null) {
            return null;
        }

        for (PerfilAcesso perfil : values()) {
            if (perfil.nivelAcesso == nivelAcesso) {
                return perfil;
            }
        }

        return null;
    }
}
