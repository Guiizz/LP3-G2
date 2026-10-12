module pt.isep.lp3.g2.hotel4youg2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens pt.isep.lp3.g2.hotel4youg2 to javafx.fxml;
    exports pt.isep.lp3.g2.hotel4youg2;
}