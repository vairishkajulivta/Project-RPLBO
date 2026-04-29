package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.service.ChatbotService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatManager {

    @FXML private TextField inputField;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Button btnChat;
    @FXML private Button btnInfoProduk;
    @FXML private Button btnInfoTokoUser;

    private ChatbotService botService = new ChatbotService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH.mm");

    @FXML
    public void initialize() {
        // Welcome message
        addBotMessage("Halo! Selamat datang di SahabatLaris 👋\nSaya bisa membantu Anda mencari informasi produk skincare untuk kulit sensitif.\n\nCoba tanyakan:\n• Harga moisturizer berapa?\n• Rekomendasi serum\n• Produk untuk kulit sensitif");
    }

    @FXML
    public void prosesPesan() {
        String pesan = inputField.getText().trim();
        if (pesan.isEmpty()) return;

        addUserMessage(pesan);
        inputField.clear();

        // Typing indicator
        HBox typingRow = createTypingIndicator();
        chatContainer.getChildren().add(typingRow);
        scrollToBottom();

        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e -> {
            chatContainer.getChildren().remove(typingRow);
            String jawaban = botService.cariJawaban(pesan);
            addBotMessage(jawaban);
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
        if (text.contains("•") && (text.contains("Rp.") || text.contains("Kandungan"))) {
            addBotProductMessage(text);
            return;
        }

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

    private void addBotProductMessage(String text) {
        VBox outer = new VBox(6);
        outer.getStyleClass().add("bubble-bot");
        outer.setMaxWidth(400);

        // Parse product entries
        String[] lines = text.split("\n");
        String headerLine = null;
        VBox currentProduct = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                if (currentProduct != null) {
                    outer.getChildren().add(currentProduct);
                    currentProduct = null;
                }
                continue;
            }
            if (!line.startsWith("•") && !line.startsWith("Rp") && !line.startsWith("Kandungan")) {
                if (headerLine == null) {
                    Label header = new Label(line);
                    header.getStyleClass().add("bubble-bot-text");
                    header.setWrapText(true);
                    outer.getChildren().add(header);
                }
                headerLine = line;
            } else if (line.startsWith("•")) {
                if (currentProduct != null) outer.getChildren().add(currentProduct);
                currentProduct = new VBox(4);
                currentProduct.getStyleClass().add("bubble-bot-product");
                String prodName = line.substring(1).trim();
                // Remove harga from name line if combined
                if (prodName.contains(" - Rp")) {
                    String[] parts = prodName.split(" - ");
                    Label nameLbl = new Label(parts[0]);
                    nameLbl.getStyleClass().add("product-name");
                    nameLbl.setWrapText(true);
                    currentProduct.getChildren().add(nameLbl);
                    if (parts.length > 1) {
                        Label priceLbl = new Label(parts[1]);
                        priceLbl.getStyleClass().add("product-price");
                        currentProduct.getChildren().add(priceLbl);
                    }
                } else {
                    Label nameLbl = new Label(prodName);
                    nameLbl.getStyleClass().add("product-name");
                    nameLbl.setWrapText(true);
                    currentProduct.getChildren().add(nameLbl);
                }
            } else if (line.startsWith("Rp") && currentProduct != null) {
                Label priceLbl = new Label(line);
                priceLbl.getStyleClass().add("product-price");
                currentProduct.getChildren().add(priceLbl);
            } else if (line.startsWith("Kandungan") && currentProduct != null) {
                Label kLbl = new Label(line);
                kLbl.getStyleClass().add("product-kandungan");
                kLbl.setWrapText(true);
                currentProduct.getChildren().add(kLbl);
            }
        }
        if (currentProduct != null) outer.getChildren().add(currentProduct);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        VBox bubble = new VBox(4);
        bubble.getChildren().addAll(outer, time);
        bubble.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);

        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    private HBox createTypingIndicator() {
        Label dots = new Label("•••");
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
    public void showChatPanel() {
        setChatSidebarActive(btnChat);
    }

    private void setChatSidebarActive(javafx.scene.control.Button active) {
        for (javafx.scene.control.Button b : new javafx.scene.control.Button[]{btnChat, btnInfoProduk, btnInfoTokoUser}) {
            if (b == null) continue;
            b.getStyleClass().remove("chat-sidebar-btn-active");
            if (!b.getStyleClass().contains("chat-sidebar-btn"))
                b.getStyleClass().add("chat-sidebar-btn");
        }
        active.getStyleClass().remove("chat-sidebar-btn");
        if (!active.getStyleClass().contains("chat-sidebar-btn-active"))
            active.getStyleClass().add("chat-sidebar-btn-active");
    }

    @FXML
    public void showInfoProduk() {
        setChatSidebarActive(btnInfoProduk);
        addBotMessage(botService.cariJawaban("produk"));
    }

    @FXML
    public void showInfoToko() {
        if (btnInfoTokoUser != null) setChatSidebarActive(btnInfoTokoUser);
        addBotMessage("SahabatLaris adalah toko skincare untuk kulit sensitif.\n\uD83D\uDCCD Yogyakarta\n\uD83D\uDD50 Senin - Minggu: 08.00 - 21.00");
    }

    @FXML
    public void showRiwayat() {
        addBotMessage("Fitur riwayat chat akan segera hadir 🚀");
    }

    @FXML
    public void showBantuan() {
        addBotMessage("Bantuan:\n• Ketik nama produk untuk info harga\n• Ketik kategori (moisturizer, toner, serum, dll)\n• Tanya rekomendasi untuk kulit sensitif");
    }
}
