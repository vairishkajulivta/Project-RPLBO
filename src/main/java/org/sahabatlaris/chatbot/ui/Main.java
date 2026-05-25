package org.sahabatlaris.chatbot.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    private static final String CSS_PATH =
            "/org/sahabatlaris/chatbot/css/styles.css";

    @Override
    public void start(Stage primaryStage) {
        showPilihMode(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showPilihMode(Stage stage) {

        Label judulLbl = new Label("SahabatLaris \uD83D\uDECD");
        judulLbl.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        Label subLbl = new Label("Pilih mode untuk melanjutkan");
        subLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

        VBox header = new VBox(8, judulLbl, subLbl);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 0, 32, 0));

        VBox kartuAdmin = buatKartu(
                "\uD83D\uDEE1",   // shield emoji
                "#4B3FC8",
                "Admin",
                "Kelola produk, kategori, dan info toko",
                "Masuk sebagai Admin",
                "#4B3FC8",
                e -> { try { showAdminLogin(stage); } catch (Exception ex) { ex.printStackTrace(); } }
        );

        VBox kartuUser = buatKartu(
                "\uD83D\uDCAC",   // speech bubble emoji
                "#22c55e",
                "User / Pelanggan",
                "Tanya produk skincare dan info toko",
                "Masuk sebagai User",
                "#22c55e",
                e -> { try { showUserChat(stage); } catch (Exception ex) { ex.printStackTrace(); } }
        );

        HBox kartuRow = new HBox(28, kartuAdmin, kartuUser);
        kartuRow.setAlignment(Pos.CENTER);
        kartuRow.setPadding(new Insets(0, 40, 40, 40));

        Label footer = new Label("\u00A9 2026 SahabatLaris \u00B7 Skincare Chatbot");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #bbb;");
        footer.setPadding(new Insets(0, 0, 20, 0));

        VBox root = new VBox(header, kartuRow, footer);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0eeff;");

        Scene scene = new Scene(root, 700, 520);
        URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle("SahabatLaris - Pilih Mode");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private VBox buatKartu(String ikon, String warnaBg, String judul,
                           String deskripsi, String tombolTeks,
                           String warnaTombol,
                           javafx.event.EventHandler<javafx.event.ActionEvent> aksi) {

        StackPane lingkaran = new StackPane();
        lingkaran.setMinSize(80, 80);
        lingkaran.setMaxSize(80, 80);
        lingkaran.setStyle("-fx-background-color: " + warnaBg +
                "; -fx-background-radius: 40;");
        Label ikonLbl = new Label(ikon);
        ikonLbl.setStyle("-fx-font-size: 32px;");
        lingkaran.getChildren().add(ikonLbl);

        Label judulLbl = new Label(judul);
        judulLbl.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        Label descLbl = new Label(deskripsi);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(180);
        descLbl.setAlignment(Pos.CENTER);

        Button tombol = new Button(tombolTeks);
        tombol.setMaxWidth(Double.MAX_VALUE);
        tombol.setStyle(
                "-fx-background-color: " + warnaTombol + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;");
        tombol.setOnAction(aksi);

        VBox kartu = new VBox(16, lingkaran, judulLbl, descLbl, tombol);
        kartu.setAlignment(Pos.CENTER);
        kartu.setPrefWidth(240);
        kartu.setPadding(new Insets(36, 32, 36, 32));
        kartu.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 14, 0, 0, 4);");

        return kartu;
    }

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
        if (fxmlUrl == null) throw new IOException("FXML tidak ditemukan: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Scene scene = new Scene(root, w, h);

        URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}
