module org.study.labs.cg.computergraphics {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.study.labs.cg.computergraphics to javafx.fxml;
    exports org.study.labs.cg.computergraphics;
}