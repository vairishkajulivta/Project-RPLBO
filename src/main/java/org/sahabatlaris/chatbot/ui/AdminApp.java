package org.sahabatlaris.chatbot.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class AdminApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new AppUI().showAdminLogin(stage);
    }

}
