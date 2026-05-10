package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.service.ChatbotService;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatManager {

    // Identitas elemen dari FXML (sesuai fx:id)
    @FXML private TextField inputField;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox riwayatListContainer;
    @FXML private Label riwayatEmptyLabel;
    @FXML private Circle onlineDot;
    @FXML private Label onlineLabel;

    private ChatbotService botService = new ChatbotService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH.mm");

    @FXML
    public void initialize() {
        // Setup awal saat aplikasi dibuka
        addBotMessage("Halo! Selamat datang di SahabatLaris \uD83D\uDC4B\n"
                + "Saya bisa membantu Anda mencari informasi produk skincare.\n\n"
                + "Coba tanyakan:\n"
                + "\u2022 Tampilkan produk skincare untuk kulit sensitif\n"
                + "\u2022 Tampilkan deskripsi, kandungan, dan harga untuk Moisturizer\n"
                + "\u2022 Apakah Wardah Hydra Rose cocok untuk kulit sensitif?\n"
                + "\u2022 Tampilkan link maps untuk lokasi "
                + "\u2022 Tampilkan jam buka dan tutup toko\n"
                + "\u2022 Tampilkan semua produk untuk kategori Sabun Wajah\n"
                + "\u2022 Apakah Somethinc Calm Down masih tersedia?");

        // Auto-scroll logic
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                chatScrollPane.setVvalue(1.0));
    }

    // Dipanggil saat tombol pesawat diklik atau tekan Enter di TextField
    @FXML
    public void prosesPesan() {
        String pesan = inputField.getText().trim();
        if (pesan.isEmpty()) return;

        addUserMessage(pesan);
        inputField.clear();

        HBox typingRow = createTypingIndicator();
        chatContainer.getChildren().add(typingRow);

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

    // Method untuk tombol "Hapus Riwayat" (Sesuai FXML)
    @FXML
    private void hapusRiwayatSidebar(ActionEvent event) {
        riwayatListContainer.getChildren().clear();
        riwayatEmptyLabel.setVisible(true);
        System.out.println("Riwayat sidebar dihapus.");
    }

    // Method untuk tombol "Riwayat Chat" di Navbar (Sesuai FXML)
    @FXML
    public void showRiwayat(ActionEvent event) {
        addBotMessage("Fitur riwayat chat akan segera hadir 🚀");
    }

    // Method untuk tombol "Bantuan" di Navbar (Sesuai FXML)
    @FXML
    public void showBantuan(ActionEvent event) {
        // ─── LOGIKA TOMBOL BANTUAN ───
        // Menggunakan teks dari blok "Bantuan" di gambar Anda
        addBotMessage("Saya bisa membantu Anda:\n"
                + "\u2022 Cek harga produk\n"
                + "\u2022 Cari produk berdasarkan kategori\n"
                + "\u2022 Rekomendasi berdasarkan jenis kulit\n"
                + "\u2022 Info lokasi & jam buka toko\n\n"
                + "Contoh: 'harga toner berapa?' atau cukup ketik 'toner'");
    }

    // --- HELPER METHODS (Logika Tampilan) ---

    private void addUserMessage(String text) {
        Label msg = new Label(text);
        msg.setStyle("-fx-background-color: #4B3FC8; -fx-text-fill: white; -fx-padding: 10 14; -fx-background-radius: 15 15 0 15;");
        msg.setWrapText(true);
        msg.setMaxWidth(360);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.setStyle("-fx-font-size: 9px; -fx-text-fill: #999;");

        VBox bubble = new VBox(4, msg, time);
        bubble.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(5, 10, 5, 10));
        chatContainer.getChildren().add(row);
    }

    private void addBotMessage(String text) {
        Label msg = new Label(text);
        msg.setStyle("-fx-background-color: white; -fx-text-fill: #1a1a2e; -fx-padding: 10 14; -fx-background-radius: 15 15 15 0; -fx-border-color: #e8e8f0; -fx-border-radius: 15;");
        msg.setWrapText(true);
        msg.setMaxWidth(380);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.setStyle("-fx-font-size: 9px; -fx-text-fill: #999;");

        VBox bubble = new VBox(4, msg, time);
        bubble.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        chatContainer.getChildren().add(row);
    }

    private void addBotProductCards(List<Produk> produkList) {
        for (Produk prod : produkList) {
            // Logika pembuatan card produk Anda tetap sama...
            // (Untuk menghemat tempat, gunakan logika pembuatan card yang sudah Anda buat)
            // ... bagian card produk Anda ...
            Label cardPlaceholder = new Label("Produk: " + prod.getNamaProduk() + " - " + prod.getHargaFormatted());
            cardPlaceholder.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #4B3FC8; -fx-border-radius: 10;");
            chatContainer.getChildren().add(new HBox(cardPlaceholder));
        }
    }

    private HBox createTypingIndicator() {
        Label dots = new Label("Typing...");
        dots.setStyle("-fx-text-fill: #4B3FC8; -fx-font-style: italic;");
        HBox row = new HBox(dots);
        row.setPadding(new Insets(5, 20, 5, 20));
        return row;
    }
}