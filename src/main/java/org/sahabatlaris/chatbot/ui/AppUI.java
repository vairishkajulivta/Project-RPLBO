package org.sahabatlaris.chatbot.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AppUI {

    private static final String CSS_PATH =
            "/org/sahabatlaris/chatbot/css/styles.css";

    public void showAdminLogin(Stage stage) throws IOException {
        loadScene(stage,
                "/org/sahabatlaris/chatbot/view/admin_login.fxml",
                "SahabatLaris - Login Admin", 900, 600);
    }


    public void showAdmin(Stage stage) throws IOException {
        loadScene(stage,
                "/org/sahabatlaris/chatbot/view/admin.fxml",
                "SahabatLaris - Admin Panel", 1200, 750);
    }

    public void showUserChat(Stage stage) throws IOException {
        loadScene(stage,
                "/org/sahabatlaris/chatbot/view/user_chat.fxml",
                "SahabatLaris - Chat", 1100, 700);
    }

    private void loadScene(Stage stage, String fxmlPath,
                           String title, double w, double h) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML tidak ditemukan: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root, w, h);

        URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}
