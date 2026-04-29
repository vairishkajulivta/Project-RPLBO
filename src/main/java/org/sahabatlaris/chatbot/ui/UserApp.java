package org.sahabatlaris.chatbot.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class UserApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new AppUI().showUserChat(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
