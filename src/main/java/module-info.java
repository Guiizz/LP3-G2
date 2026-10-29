/**
 * Hotel4You: aplicação JavaFX para a gestão de um hotel (LP3, grupo 2).
 */
module pt.isep.lp3.g2.hotel4youg2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.xml;
    requires io.github.cdimascio.dotenv.java;


    opens pt.isep.lp3.g2.hotel4youg2 to javafx.fxml;
    opens pt.isep.lp3.g2.hotel4youg2.controller to javafx.fxml;
    opens pt.isep.lp3.g2.hotel4youg2.model to javafx.base;
    opens pt.isep.lp3.g2.hotel4youg2.bll to org.junit.platform.commons;
    exports pt.isep.lp3.g2.hotel4youg2;
}