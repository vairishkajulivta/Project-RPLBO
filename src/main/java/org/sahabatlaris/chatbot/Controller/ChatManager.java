package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.service.ChatbotService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatManager {

    @FXML private TextField inputField;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Button btnChat;

    private ChatbotService botService = new ChatbotService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH.mm");

    @FXML
    public void initialize() {
        addBotMessage("Halo! Selamat datang di SahabatLaris \uD83D\uDC4B\n"
                + "Saya bisa membantu Anda mencari informasi produk skincare.\n\n"
                + "Coba tanyakan:\n"
                + "\u2022 Harga moisturizer berapa?\n"
                + "\u2022 Rekomendasi serum\n"
                + "\u2022 Produk untuk kulit sensitif");
    }

    @FXML
    public void prosesPesan() {
        String pesan = inputField.getText().trim();
        if (pesan.isEmpty()) return;

        addUserMessage(pesan);
        inputField.clear();

        HBox typingRow = createTypingIndicator();
        chatContainer.getChildren().add(typingRow);
        scrollToBottom();

        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e -> {
            chatContainer.getChildren().remove(typingRow);
            List<Produk> produkDisebut = botService.getProdukMentioned(pesan);
            if (!produkDisebut.isEmpty()) {
                addBotProductCards(produkDisebut);
            } else {
                String jawaban = botService.cariJawaban(pesan);
                List<Produk> produkKategori = botService.getLastProdukResult();
                if (produkKategori != null && !produkKategori.isEmpty()) {
                    String header = jawaban.split("\n")[0];
                    addBotMessage(header);
                    addBotProductCards(produkKategori);
                } else {
                    addBotMessage(jawaban);
                }
            }
        });
        pause.play();
    }

    private void addUserMessage(String text) {
        VBox bubble = new VBox(4);
        Label msg = new Label(text);
        msg.getStyleClass().add("bubble-user-text");
        msg.setWrapText(true);
        msg.setMaxWidth(360);

        VBox bubbleBox = new VBox(4);
        bubbleBox.getStyleClass().add("bubble-user");
        bubbleBox.getChildren().add(msg);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        bubble.getChildren().addAll(bubbleBox, time);
        bubble.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        HBox.setMargin(bubble, new Insets(0));

        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        VBox bubbleBox = new VBox(4);
        bubbleBox.getStyleClass().add("bubble-bot");

        Label msg = new Label(text);
        msg.getStyleClass().add("bubble-bot-text");
        msg.setWrapText(true);
        msg.setMaxWidth(380);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        VBox bubble = new VBox(4);
        bubbleBox.getChildren().add(msg);
        bubble.getChildren().addAll(bubbleBox, time);
        bubble.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);

        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    /**
     * Card layout: thumbnail kiri 80x80 + info kanan
     * Nama | Kategori | Harga | Kandungan | [Badge Area] [Badge Kulit]
     */
    private void addBotProductCards(List<Produk> produkList) {
        for (Produk prod : produkList) {

            // ── Thumbnail kiri ────────────────────────────────────────────
            StackPane thumbPane = new StackPane();
            thumbPane.setMinWidth(80);  thumbPane.setMaxWidth(80);
            thumbPane.setMinHeight(80); thumbPane.setMaxHeight(80);
            thumbPane.setStyle("-fx-background-color: #e8e8e8; -fx-background-radius: 10;");

            String imgUrl = prod.getGambarUrl();
            boolean imgLoaded = false;
            if (imgUrl != null && !imgUrl.isBlank()) {
                try {
                    ImageView imgView = new ImageView(new Image(imgUrl, 80, 80, true, true, true));
                    imgView.setFitWidth(80);
                    imgView.setFitHeight(80);
                    imgView.setPreserveRatio(true);
                    Rectangle clip = new Rectangle(80, 80);
                    clip.setArcWidth(16);
                    clip.setArcHeight(16);
                    imgView.setClip(clip);
                    thumbPane.getChildren().add(imgView);
                    imgLoaded = true;
                } catch (Exception ex) { /* fallback */ }
            }
            if (!imgLoaded) {
                Label noImg = new Label("No Image");
                noImg.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
                thumbPane.getChildren().add(noImg);
            }

            // ── Info kanan ────────────────────────────────────────────────
            Label nameLbl = new Label(prod.getNamaProduk());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a1a2e;");
            nameLbl.setWrapText(true);

            Label katLbl = new Label(prod.getKategori());
            katLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            Label hargaLbl = new Label(prod.getHargaFormatted());
            hargaLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #4B3FC8;");

            Label kandLbl = new Label("Kandungan: " + prod.getKandungan());
            kandLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            kandLbl.setWrapText(true);

            // ── Badge: Area Tubuh & Jenis Kulit ───────────────────────────
            String areaTubuh  = prod.getAreaTubuh()  != null ? prod.getAreaTubuh()  : "Muka";
            String jenisKulit = prod.getJenisKulit() != null ? prod.getJenisKulit() : "Semua Jenis Kulit";

            Label badgeArea = new Label("\uD83D\uDCCD " + areaTubuh);
            badgeArea.setStyle(
                    "-fx-font-size: 10px; -fx-text-fill: #3a6fc4;" +
                            "-fx-background-color: #e8f0fe; -fx-background-radius: 20;" +
                            "-fx-padding: 2 8 2 8;");

            Label badgeKulit = new Label("\uD83C\uDF3F " + jenisKulit);
            badgeKulit.setStyle(
                    "-fx-font-size: 10px; -fx-text-fill: #276a3f;" +
                            "-fx-background-color: #e6f4ea; -fx-background-radius: 20;" +
                            "-fx-padding: 2 8 2 8;");

            HBox badgeRow = new HBox(6, badgeArea, badgeKulit);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            VBox infoBox = new VBox(4, nameLbl, katLbl, hargaLbl, kandLbl, badgeRow);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            // ── Card container ────────────────────────────────────────────
            HBox card = new HBox(12, thumbPane, infoBox);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(12));
            card.setMaxWidth(460);
            String styleNormal =
                    "-fx-background-color: white;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);";
            String styleHover =
                    "-fx-background-color: #f8f7ff;" +
                            "-fx-border-color: #4B3FC8;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(75,63,200,0.12), 8, 0, 0, 3);";
            card.setStyle(styleNormal);
            card.setOnMouseEntered(e -> card.setStyle(styleHover));
            card.setOnMouseExited(e -> card.setStyle(styleNormal));

            Label time = new Label(LocalTime.now().format(TIME_FMT));
            time.getStyleClass().add("timestamp");

            VBox bubble = new VBox(4, card, time);
            bubble.setAlignment(Pos.CENTER_LEFT);

            HBox row = new HBox(bubble);
            row.setAlignment(Pos.CENTER_LEFT);
            chatContainer.getChildren().add(row);
        }
        scrollToBottom();
    }

    private HBox createTypingIndicator() {
        Label dots = new Label("\u2022\u2022\u2022");
        dots.setStyle("-fx-text-fill: #4B3FC8; -fx-font-size: 18px;");
        VBox bubble = new VBox(dots);
        bubble.getStyleClass().add("typing-indicator");
        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void scrollToBottom() {
        chatContainer.layout();
        chatScrollPane.setVvalue(1.0);
    }

    public void tampilPesan(String msg) {
        System.out.println(msg);
    }

    @FXML
    public void showChatPanel() {}

    @FXML
    public void showRiwayat() {
        addBotMessage("Fitur riwayat chat akan segera hadir \uD83D\uDE80");
    }

    @FXML
    public void showBantuan() {
        addBotMessage("Bantuan:\n"
                + "\u2022 Ketik nama produk untuk info harga\n"
                + "\u2022 Ketik kategori (moisturizer, toner, serum, dll)\n"
                + "\u2022 Tanya rekomendasi untuk jenis kulit tertentu\n"
                + "\u2022 Tanya jam buka toko atau lokasi toko");
    }
}
