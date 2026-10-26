package pt.isep.lp3.g2.hotel4youg2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Arranque do JavaFX: abre a janela no ecrã de login.
 */
public class HelloApplication extends Application {
    /**
     * Mostra o ecrã de login na janela principal.
     *
     * @param stage a janela principal criada pelo JavaFX
     * @throws IOException se não for possível carregar o login-view.fxml
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 360, 320);
        stage.setTitle("Hotel4You · Entrar");
        stage.setScene(scene);
        stage.show();
    }
}
