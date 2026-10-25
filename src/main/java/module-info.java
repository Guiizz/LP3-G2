module pt.isep.lp3.g2.hotel4youg2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;


    opens pt.isep.lp3.g2.hotel4youg2 to javafx.fxml;
    opens pt.isep.lp3.g2.hotel4youg2.controller to javafx.fxml;
    opens pt.isep.lp3.g2.hotel4youg2.model to javafx.base;
    exports pt.isep.lp3.g2.hotel4youg2;
}