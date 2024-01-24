module com.tummosoft.testsvgfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.tummosoft.testsvgfx to javafx.fxml;
    exports com.tummosoft.testsvgfx;
}
