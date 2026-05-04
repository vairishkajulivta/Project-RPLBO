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

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatManager {

    @FXML private TextField   inputField;
    @FXML private VBox        chatContainer;
    @FXML private ScrollPane  chatScrollPane;
    @FXML private Button      btnChat;

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
                List<Produk> produkResult = botService.getProdukDariJawaban(pesan, jawaban);
                if (produkResult != null && !produkResult.isEmpty()) {
                    String header = jawaban.split("\n")[0];
                    addBotMessage(header);
                    addBotProductCards(produkResult);
                } else {
                    addBotMessage(jawaban);
                }
            }
        });
        pause.play();
    }

    private void addUserMessage(String text) {
        Label msg = new Label(text);
        msg.getStyleClass().add("bubble-user-text");
        msg.setWrapText(true);
        msg.setMaxWidth(360);

        VBox bubbleBox = new VBox(4);
        bubbleBox.getStyleClass().add("bubble-user");
        bubbleBox.getChildren().add(msg);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        VBox bubble = new VBox(4, bubbleBox, time);
        bubble.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        Label msg = new Label(text);
        msg.getStyleClass().add("bubble-bot-text");
        msg.setWrapText(true);
        msg.setMaxWidth(380);

        VBox bubbleBox = new VBox(4);
        bubbleBox.getStyleClass().add("bubble-bot");
        bubbleBox.getChildren().add(msg);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        VBox bubble = new VBox(4, bubbleBox, time);
        bubble.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CARD PRODUK — thumbnail kiri besar + info kanan
    // ════════════════════════════════════════════════════════════════════════
    private void addBotProductCards(List<Produk> produkList) {
        for (Produk prod : produkList) {

            // ── Thumbnail kiri 120x120 ────────────────────────────────────
            StackPane thumbPane = new StackPane();
            thumbPane.setMinSize(120, 120);
            thumbPane.setMaxSize(120, 120);
            thumbPane.setStyle(
                    "-fx-background-color: #f0eeff;" +
                    "-fx-background-radius: 12;");

            String imgUrl = prod.getGambarUrl();
            boolean imgLoaded = false;

            if (imgUrl != null && !imgUrl.isBlank()) {
                try {
                    String resolvedUrl;
                    if (imgUrl.startsWith("http://") || imgUrl.startsWith("https://")
                            || imgUrl.startsWith("file:")) {
                        resolvedUrl = imgUrl;
                    } else {
                        // Path lokal relatif → konversi ke file:/// URI absolut
                        File imgFile = new File(imgUrl);
                        resolvedUrl = imgFile.getAbsoluteFile().toURI().toString();
                    }
                    // false = load synchronous, pasti muncul langsung
                    Image img = new Image(resolvedUrl, 120, 120, true, true, false);
                    if (!img.isError()) {
                        ImageView imgView = new ImageView(img);
                        imgView.setFitWidth(120);
                        imgView.setFitHeight(120);
                        imgView.setPreserveRatio(true);
                        Rectangle clip = new Rectangle(120, 120);
                        clip.setArcWidth(20);
                        clip.setArcHeight(20);
                        imgView.setClip(clip);
                        thumbPane.getChildren().add(imgView);
                        imgLoaded = true;
                    }
                } catch (Exception ex) { /* fallback */ }
            }

            if (!imgLoaded) {
                Label icon = new Label("\uD83D\uDDBC\uFE0F");
                icon.setStyle("-fx-font-size: 30px;");
                Label noImgTxt = new Label("No Image");
                noImgTxt.setStyle("-fx-text-fill: #bbb; -fx-font-size: 10px;");
                VBox ph = new VBox(4, icon, noImgTxt);
                ph.setAlignment(Pos.CENTER);
                thumbPane.getChildren().add(ph);
            }

            // ── Info kanan ────────────────────────────────────────────────
            Label nameLbl = new Label(prod.getNamaProduk());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a1a2e;");
            nameLbl.setWrapText(true);

            Label katLbl = new Label("\u2194\uFE0F " + prod.getKategori());
            katLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            Label hargaLbl = new Label(prod.getHargaFormatted());
            hargaLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #4B3FC8;");

            Label kandLbl = new Label("\u2714 " + prod.getKandungan());
            kandLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            kandLbl.setWrapText(true);

            String jenisKulit = prod.getJenisKulit() != null ? prod.getJenisKulit() : "Semua Jenis Kulit";
            Label badgeKulit = new Label("\uD83C\uDF3F " + jenisKulit);
            badgeKulit.setStyle(
                    "-fx-font-size: 10px; -fx-text-fill: #276a3f;" +
                    "-fx-background-color: #e6f4ea;" +
                    "-fx-background-radius: 20; -fx-padding: 3 10 3 10;");

            HBox badgeRow = new HBox(badgeKulit);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            VBox infoBox = new VBox(6, nameLbl, katLbl, hargaLbl, kandLbl, badgeRow);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            infoBox.setPadding(new Insets(2, 0, 2, 0));
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            // ── Card ─────────────────────────────────────────────────────
            HBox card = new HBox(14, thumbPane, infoBox);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(14));
            card.setMaxWidth(500);

            String sNormal =
                    "-fx-background-color: white;" +
                    "-fx-border-color: #e8e8f0; -fx-border-width: 1;" +
                    "-fx-border-radius: 14; -fx-background-radius: 14;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);";
            String sHover =
                    "-fx-background-color: #f8f7ff;" +
                    "-fx-border-color: #4B3FC8; -fx-border-width: 1.5;" +
                    "-fx-border-radius: 14; -fx-background-radius: 14;" +
                    "-fx-effect: dropshadow(gaussian, rgba(75,63,200,0.15), 10, 0, 0, 3);";

            card.setStyle(sNormal);
            card.setOnMouseEntered(e -> card.setStyle(sHover));
            card.setOnMouseExited(e -> card.setStyle(sNormal));

            Label time = new Label(LocalTime.now().format(TIME_FMT));
            time.getStyleClass().add("timestamp");

            VBox bubble = new VBox(6, card, time);
            bubble.setAlignment(Pos.CENTER_LEFT);

            HBox row = new HBox(bubble);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 8, 2, 8));
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

    public void tampilPesan(String msg) { System.out.println(msg); }

    @FXML public void showChatPanel() {}

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
