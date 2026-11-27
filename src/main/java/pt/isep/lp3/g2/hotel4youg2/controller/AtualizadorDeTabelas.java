package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class AtualizadorDeTabelas {

    public static final Duration INTERVALO = Duration.seconds(30);

    private static final List<Ecra> abertos = new ArrayList<>();

    public interface Ecra {
        void recarregar();
    }

    public interface Chave<T> {
        Object de(T linha);
    }

    private AtualizadorDeTabelas() {
    }

    public static void ligar(Node no, Ecra ecra) {
        Timeline relogio = new Timeline(new KeyFrame(INTERVALO, new AoPassarOTempo(ecra)));
        relogio.setCycleCount(Animation.INDEFINITE);
        AoMudarDeCena aoMudar = new AoMudarDeCena(ecra, relogio);
        no.sceneProperty().addListener(aoMudar);
        if (no.getScene() != null) {
            aoMudar.abrir(false);
        }
    }

    public static void atualizarAbertos() {
        for (Ecra ecra : new ArrayList<>(abertos)) {
            ecra.recarregar();
        }
    }

    static void registar(Ecra ecra) {
        if (!abertos.contains(ecra)) {
            abertos.add(ecra);
        }
    }

    static void esquecer(Ecra ecra) {
        abertos.remove(ecra);
    }

    public static <T> void mostrar(TableView<T> tabela, List<T> linhas, Chave<T> chave) {
        T escolhida = tabela.getSelectionModel().getSelectedItem();
        tabela.getItems().setAll(linhas);
        int posicao = posicao(linhas, escolhida, chave);
        if (posicao >= 0) {
            tabela.getSelectionModel().select(posicao);
        }
    }

    static <T> int posicao(List<T> linhas, T escolhida, Chave<T> chave) {
        if (escolhida == null) {
            return -1;
        }
        Object procurada = chave.de(escolhida);
        for (int i = 0; i < linhas.size(); i++) {
            if (procurada.equals(chave.de(linhas.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private static class AoPassarOTempo implements EventHandler<ActionEvent> {
        private final Ecra ecra;

        AoPassarOTempo(Ecra ecra) {
            this.ecra = ecra;
        }

        @Override
        public void handle(ActionEvent evento) {
            ecra.recarregar();
        }
    }

    private static class AoMudarDeCena implements ChangeListener<Scene> {
        private final Ecra ecra;
        private final Timeline relogio;
        private boolean abertoAntes;

        AoMudarDeCena(Ecra ecra, Timeline relogio) {
            this.ecra = ecra;
            this.relogio = relogio;
        }

        @Override
        public void changed(ObservableValue<? extends Scene> observavel, Scene antiga, Scene nova) {
            if (nova == null) {
                relogio.stop();
                esquecer(ecra);
            } else if (antiga == null) {
                abrir(abertoAntes);
            }
        }

        void abrir(boolean recarregarJa) {
            registar(ecra);
            relogio.playFromStart();
            if (recarregarJa) {
                ecra.recarregar();
            }
            abertoAntes = true;
        }
    }
}
