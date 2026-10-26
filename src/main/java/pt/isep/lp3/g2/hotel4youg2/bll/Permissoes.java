package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

/**
 * Regras sobre o que cada perfil pode fazer na aplicação (US12).
 */
public final class Permissoes {

    private Permissoes() {
    }

    /**
     * @param utilizador o utilizador com a sessão iniciada
     * @return {@code true} se o utilizador for gestor
     */
    public static boolean eGestor(Utilizador utilizador) {
        return utilizador != null && utilizador.getPerfil() == Perfil.GESTOR;
    }

    /**
     * Garante que só o gestor continua.
     *
     * @param utilizador o utilizador com a sessão iniciada
     * @throws IllegalStateException se o utilizador não for gestor
     */
    public static void exigirGestor(Utilizador utilizador) {
        if (!eGestor(utilizador)) {
            throw new IllegalStateException("Só o gestor pode abrir este ecrã");
        }
    }
}
