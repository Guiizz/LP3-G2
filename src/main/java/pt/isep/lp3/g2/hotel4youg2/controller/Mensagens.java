package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.scene.control.Label;

public final class Mensagens {

    private Mensagens() {
    }

    public static void erro(Label label, String texto) {
        aplicar(label, texto, "mensagem-erro");
    }

    public static void sucesso(Label label, String texto) {
        aplicar(label, texto, "mensagem-sucesso");
    }

    public static void aviso(Label label, String texto) {
        aplicar(label, texto, "mensagem-aviso");
    }

    public static void info(Label label, String texto) {
        aplicar(label, texto, null);
    }

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
