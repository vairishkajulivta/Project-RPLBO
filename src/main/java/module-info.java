module org.sahabatlaris.chatbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens org.sahabatlaris.chatbot.Controller to javafx.fxml;
    opens org.sahabatlaris.chatbot.ui to javafx.fxml, javafx.graphics;
    opens org.sahabatlaris.chatbot.model to javafx.base;
    opens org.sahabatlaris.chatbot.service to javafx.base;

    exports org.sahabatlaris.chatbot.ui;
}
