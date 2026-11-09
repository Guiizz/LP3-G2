package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.scene.control.Label;

/**
 * Mostra as mensagens dos ecrãs com as cores da folha de estilos (US44): erro a vermelho,
 * sucesso a verde e aviso a laranja. Usa classes do CSS em vez de {@code setStyle}, por isso
 * as cores ficam todas definidas num só sítio.
 */
public final class Mensagens {

    private Mensagens() {
    }

    /**
     * Mostra uma mensagem de erro, a vermelho.
     *
     * @param label onde mostrar a mensagem
     * @param texto a mensagem
     */
    public static void erro(Label label, String texto) {
        aplicar(label, texto, "mensagem-erro");
    }

    /**
     * Mostra uma mensagem de sucesso, a verde.
     *
     * @param label onde mostrar a mensagem
     * @param texto a mensagem
     */
    public static void sucesso(Label label, String texto) {
        aplicar(label, texto, "mensagem-sucesso");
    }

    /**
     * Mostra um aviso, a laranja.
     *
     * @param label onde mostrar a mensagem
     * @param texto a mensagem
     */
    public static void aviso(Label label, String texto) {
        aplicar(label, texto, "mensagem-aviso");
    }

    /**
     * Mostra uma mensagem normal, sem cor.
     *
     * @param label onde mostrar a mensagem
     * @param texto a mensagem
     */
    public static void info(Label label, String texto) {
        aplicar(label, texto, null);
    }

    /**
     * Mostra uma mensagem com o nome da cor que os controllers já usavam. Assim, os ecrãs que tinham
     * {@code mostrarMensagem(texto, cor)} só precisaram de mudar o corpo desse método.
     *
     * @param label onde mostrar a mensagem
     * @param texto a mensagem
     * @param cor   {@code "red"} para erro, {@code "green"} para sucesso, {@code "darkorange"} para aviso;
     *              qualquer outra cor mostra a mensagem sem cor
     */
    public static void mostrar(Label label, String texto, String cor) {
        if ("red".equals(cor)) {
            erro(label, texto);
        } else if ("green".equals(cor)) {
            sucesso(label, texto);
        } else if ("darkorange".equals(cor)) {
            aviso(label, texto);
        } else {
            info(label, texto);
        }
    }

    private static void aplicar(Label label, String texto, String classe) {
        label.setStyle("");
        label.getStyleClass().removeAll("mensagem-erro", "mensagem-sucesso", "mensagem-aviso");
        if (classe != null) {
            label.getStyleClass().add(classe);
        }
        label.setText(texto);
    }
}
