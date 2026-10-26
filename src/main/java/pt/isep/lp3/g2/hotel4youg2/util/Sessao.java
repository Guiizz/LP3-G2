package pt.isep.lp3.g2.hotel4youg2.util;

import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

/**
 * Guarda o utilizador que tem a sessão iniciada na aplicação.
 * Existe uma só sessão de cada vez, partilhada por todos os ecrãs.
 */
public final class Sessao {

    private static Utilizador utilizador;

    private Sessao() {
    }

    /**
     * Inicia a sessão de um utilizador, depois de autenticado.
     *
     * @param utilizadorAutenticado o utilizador que entrou na aplicação
     */
    public static void iniciar(Utilizador utilizadorAutenticado) {
        utilizador = utilizadorAutenticado;
    }

    /**
     * Devolve o utilizador com a sessão iniciada.
     *
     * @return o utilizador autenticado, ou {@code null} se ninguém tiver a sessão iniciada
     */
    public static Utilizador getUtilizador() {
        return utilizador;
    }

    /**
     * Indica se há alguém com a sessão iniciada.
     *
     * @return {@code true} se há um utilizador autenticado
     */
    public static boolean estaIniciada() {
        return utilizador != null;
    }

    /**
     * Termina a sessão: deixa de haver um utilizador autenticado.
     */
    public static void terminar() {
        utilizador = null;
    }
}
