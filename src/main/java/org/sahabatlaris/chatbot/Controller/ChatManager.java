package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.service.DatabaseService.HariLibur;
import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.service.ChatbotService;
import org.sahabatlaris.chatbot.service.ManagedDataService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ChatManager {

    @FXML private TextField inputField;
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML @SuppressWarnings("unused") private Button btnChat;
    @FXML @SuppressWarnings("unused") private Circle onlineDot;
    @FXML @SuppressWarnings("unused") private Label onlineLabel;

    @FXML private VBox riwayatListContainer;
    @FXML private Label riwayatEmptyLabel;
    @FXML @SuppressWarnings("unused") private Button btnHapusRiwayat;

    private ChatbotService botService = new ChatbotService();
    private ManagedDataService dataService = new ManagedDataService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH.mm");

    @FXML
    public void initialize() {
        addWelcomeCard();
        muatSidebarRiwayat();

        javafx.application.Platform.runLater(() -> {
            if (chatScrollPane != null && chatScrollPane.getScene() != null
                    && chatScrollPane.getScene().getWindow() != null) {
                javafx.stage.Stage stage =
                        (javafx.stage.Stage) chatScrollPane.getScene().getWindow();
                stage.setWidth(1150);
                stage.setHeight(750);
                stage.setMinWidth(950);
                stage.setMinHeight(600);
                stage.centerOnScreen();
            }
        });
    }

    private void addWelcomeCard() {
        String[] info = dataService.getInfoToko();
        String namaToko = (info.length > 0 && !info[0].isBlank()) ? info[0] : "SahabatLaris";

        Label msg = new Label(
                "Halo! Selamat datang di " + namaToko + " \uD83D\uDC4B\n"
                        + "Saya bisa membantu Anda mencari informasi produk skincare.\n\n"
                        + "Coba tanyakan:\n"
                        + "\u2022 Harga moisturizer berapa?\n"
                        + "\u2022 Rekomendasi serum\n"
                        + "\u2022 Produk untuk kulit sensitif");
        msg.getStyleClass().add("bubble-bot-text");
        msg.setWrapText(true);
        msg.setMaxWidth(360);

        FlowPane chips = new FlowPane(8, 6);
        chips.setMaxWidth(370);
        chips.setPadding(new Insets(8, 0, 0, 0));

        String[][] quickReplies = {
                {"\uD83C\uDF38 Kulit Sensitif", "produk untuk kulit sensitif"},
                {"\uD83D\uDCAB Rekomendasi",    "rekomendasi produk"},
                {"\uD83D\uDCB0 Cek Harga",      "harga semua produk"},
                {"\u2728 Serum",                "serum"},
                {"\uD83D\uDCA7 Toner",          "toner"},
                {"\uD83C\uDFEA Status Toko",    "status toko"}
        };

        for (String[] qr : quickReplies) {
            Button chip = new Button(qr[0]);
            chip.setStyle(
                    "-fx-background-color: white; -fx-text-fill: #4B3FC8;" +
                            "-fx-font-size: 11px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 20; -fx-border-color: #c5c0f0;" +
                            "-fx-border-width: 1; -fx-border-radius: 20;" +
                            "-fx-padding: 5 12; -fx-cursor: hand;");
            String pesanChip = qr[1];
            chip.setOnAction(e -> { if (inputField != null) { inputField.setText(pesanChip); prosesPesan(); } });
            chip.setOnMouseEntered(e -> chip.setStyle(
                    "-fx-background-color: #f0eeff; -fx-text-fill: #4B3FC8;" +
                            "-fx-font-size: 11px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 20; -fx-border-color: #4B3FC8;" +
                            "-fx-border-width: 1.5; -fx-border-radius: 20;" +
                            "-fx-padding: 5 12; -fx-cursor: hand;"));
            chip.setOnMouseExited(e -> chip.setStyle(
                    "-fx-background-color: white; -fx-text-fill: #4B3FC8;" +
                            "-fx-font-size: 11px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 20; -fx-border-color: #c5c0f0;" +
                            "-fx-border-width: 1; -fx-border-radius: 20;" +
                            "-fx-padding: 5 12; -fx-cursor: hand;"));
            chips.getChildren().add(chip);
        }

        VBox fullBubble = new VBox(0);
        fullBubble.getStyleClass().add("bubble-bot");
        fullBubble.setMinWidth(300);
        fullBubble.setMaxWidth(400);
        fullBubble.setPrefWidth(400);
        fullBubble.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e8e8f0; -fx-border-width: 1;" +
                        "-fx-border-radius: 4 16 16 16; -fx-background-radius: 4 16 16 16;" +
                        "-fx-padding: 14 16;");
        fullBubble.getChildren().addAll(msg, chips);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        VBox wrap = new VBox(4, fullBubble, time);
        wrap.setAlignment(Pos.CENTER_LEFT);
        wrap.setMaxWidth(420);

        HBox row = new HBox(wrap);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 16, 8, 16));

        chatContainer.getChildren().add(row);
        scrollToBottom();
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
            String pesanLower = pesan.toLowerCase();

            if (pesanLower.contains("lokasi") || pesanLower.contains("alamat")
                    || pesanLower.contains("dimana") || pesanLower.contains("di mana")
                    || pesanLower.contains("maps") || pesanLower.contains("peta")) {
                addLokasiCard();
                dataService.tambahRiwayat(pesan, "Info lokasi toko ditampilkan.", "Lokasi");
                muatSidebarRiwayat();
                return;
            }

            if (pesanLower.contains("buka") || pesanLower.contains("tutup")
                    || pesanLower.contains("jam") || pesanLower.contains("operasional")
                    || pesanLower.contains("libur") || pesanLower.contains("status toko")
                    || pesanLower.contains("hari libur")) {
                addStatusTokoCard();
                dataService.tambahRiwayat(pesan, "Info status/jam toko ditampilkan.", "Info Toko");
                muatSidebarRiwayat();
                return;
            }

            String jawaban = botService.cariJawaban(pesan);
            List<Produk> produkResult = botService.getLastProdukResult();

            if ((produkResult == null || produkResult.isEmpty())
                    && !jawaban.startsWith("Maaf")
                    && !jawaban.startsWith("Halo")
                    && !jawaban.startsWith("Saya bisa")) {
                produkResult = botService.getProdukMentioned(pesan);
            }

            if (produkResult != null && !produkResult.isEmpty()) {
                String header = jawaban.split("\n")[0];
                addBotMessage(header);
                addBotProductCards(produkResult);
                String ringkasan = header + " (" + produkResult.size() + " produk)";
                dataService.tambahRiwayat(pesan, ringkasan, "Produk");
                muatSidebarRiwayat();
            } else {
                addBotMessage(jawaban);
                String tag = tentukanTag(pesan);
                dataService.tambahRiwayat(pesan, jawaban, tag);
                muatSidebarRiwayat();
            }
        });
        pause.play();
    }

    private void addLokasiCard() {
        String[] info = dataService.getInfoToko();
        String namaToko = (info.length > 0 && info[0] != null && !info[0].isBlank())
                ? info[0] : "SahabatLaris";

        String alamat = "";
        if (info.length > 3 && info[3] != null && !info[3].isBlank()) {
            alamat = info[3];
            if (info.length > 4 && info[4] != null && !info[4].isBlank()) alamat += ", " + info[4];
            if (info.length > 5 && info[5] != null && !info[5].isBlank()) alamat += " " + info[5];
        }

        VBox card = new VBox(0);
        card.setMaxWidth(320);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e8e8f0; -fx-border-width: 1;" +
                        "-fx-border-radius: 18; -fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 12, 0, 0, 3);");

        StackPane imgPane = new StackPane();
        imgPane.setMinSize(320, 170);
        imgPane.setMaxSize(320, 170);
        imgPane.setStyle("-fx-background-color: #f0eeff; -fx-background-radius: 18 18 0 0;");

        boolean imgLoaded = false;
        String[] candidatePaths = {
                "images/toko/toko.jpg", "images/toko/toko.png", "images/toko/toko.jpeg",
                "images/toko.jpg", "images/toko.png", "images/toko.jpeg",
                "images/store.jpg", "images/store.png"
        };
        for (String candidate : candidatePaths) {
            if (imgLoaded) break;
            try {
                File f = new File(candidate);
                if (!f.exists()) f = new File(System.getProperty("user.dir"), candidate);
                if (!f.exists()) f = new File(System.getProperty("user.home"), candidate);
                if (f.exists()) {
                    Image img = new Image(f.toURI().toString(), 320, 170, false, true, false);
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(320); iv.setFitHeight(170); iv.setPreserveRatio(false);
                        Rectangle clip = new Rectangle(320, 170);
                        clip.setArcWidth(36); clip.setArcHeight(36);
                        iv.setClip(clip);
                        imgPane.getChildren().add(iv);
                        imgLoaded = true;
                    }
                }
            } catch (Exception ignored) {}
        }
        if (!imgLoaded) {
            Label ikonLbl = new Label("\uD83C\uDFEA");
            ikonLbl.setStyle("-fx-font-size: 52px;");
            Label noImgTxt = new Label("Foto Toko");
            noImgTxt.setStyle("-fx-font-size: 12px; -fx-text-fill: #bbb;");
            VBox ph = new VBox(6, ikonLbl, noImgTxt);
            ph.setAlignment(Pos.CENTER);
            imgPane.getChildren().add(ph);
        }
        card.getChildren().add(imgPane);

        VBox infoBox = new VBox(6);
        infoBox.setPadding(new Insets(14, 16, 16, 16));
        Label namaLbl = new Label(namaToko);
        namaLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        HBox alamatRow = new HBox(6);
        alamatRow.setAlignment(Pos.CENTER_LEFT);
        Label pinIcon = new Label("\uD83D\uDCCD");
        pinIcon.setStyle("-fx-font-size: 13px;");
        Label alamatLbl = new Label(alamat.isBlank() ? "Alamat belum tersedia" : alamat);
        alamatLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        alamatLbl.setWrapText(true);
        alamatLbl.setMaxWidth(270);
        alamatRow.getChildren().addAll(pinIcon, alamatLbl);
        infoBox.getChildren().addAll(namaLbl, alamatRow);
        card.getChildren().add(infoBox);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");
        VBox wrap = new VBox(6, card, time);
        wrap.setAlignment(Pos.CENTER_LEFT);
        HBox row = new HBox(wrap);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 8, 2, 8));
        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addStatusTokoCard() {
        String[] info = dataService.getInfoToko();
        String[] status = dataService.cekStatusTokoHariIni();
        boolean buka = "buka".equalsIgnoreCase(status[0]);

        String hariIniStr = LocalDate.now().getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("id","ID"));
        String tglStr = LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy",
                        new java.util.Locale("id","ID")));

        VBox card = new VBox(0);
        card.setMaxWidth(430);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e8e8f0;" +
                "-fx-border-width: 1; -fx-border-radius: 18; -fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 12, 0, 0, 3);");

        VBox header = new VBox(6);
        header.setPadding(new Insets(16, 18, 14, 18));
        header.setStyle(buka ? "-fx-background-color: #f0fdf4; -fx-background-radius: 18 18 0 0;"
                : "-fx-background-color: #fff5f5; -fx-background-radius: 18 18 0 0;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane ikonCircle = new StackPane();
        ikonCircle.setMinSize(40,40); ikonCircle.setMaxSize(40,40);
        ikonCircle.setStyle(buka ? "-fx-background-color: #dcfce7; -fx-background-radius: 20;"
                : "-fx-background-color: #fee2e2; -fx-background-radius: 20;");
        Label ikonLbl = new Label(buka ? "✓" : "✕");
        ikonLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: "
                + (buka ? "#16a34a;" : "#dc2626;"));
        ikonCircle.getChildren().add(ikonLbl);

        VBox statusInfo = new VBox(3);
        Label statusLbl = new Label("Toko " + (buka ? "BUKA" : "TUTUP") + " Sekarang");
        statusLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: "
                + (buka ? "#15803d;" : "#dc2626;"));
        Label subLbl = new Label(status[1]);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        statusInfo.getChildren().addAll(statusLbl, subLbl);
        HBox.setHgrow(statusInfo, Priority.ALWAYS);

        Label tglBadge = new Label(hariIniStr + ", " + tglStr);
        tglBadge.setStyle("-fx-background-color: " + (buka ? "#dcfce7" : "#fee2e2") + ";"
                + "-fx-text-fill: " + (buka ? "#15803d" : "#dc2626") + ";"
                + "-fx-font-size: 10px; -fx-font-weight: bold;"
                + "-fx-padding: 3 9; -fx-background-radius: 20;");

        topRow.getChildren().addAll(ikonCircle, statusInfo, tglBadge);
        header.getChildren().add(topRow);

        if (!"-".equals(status[2])) {
            HBox jamHariIni = new HBox(6);
            jamHariIni.setAlignment(Pos.CENTER_LEFT);
            Label jamIcon = new Label("\uD83D\uDD50");
            jamIcon.setStyle("-fx-font-size: 12px;");
            Label jamLbl2 = new Label("Jam buka hari ini: " + status[2] + " - " + status[3]);
            jamLbl2.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-weight: bold;");
            jamHariIni.getChildren().addAll(jamIcon, jamLbl2);
            header.getChildren().add(jamHariIni);
        }
        card.getChildren().add(header);

        Separator div1 = new Separator();
        div1.setStyle("-fx-background-color: #f0f0f6;");
        card.getChildren().add(div1);

        VBox jamSection = new VBox(0);
        jamSection.setPadding(new Insets(14, 18, 14, 18));
        HBox jamHeader = new HBox(8);
        jamHeader.setAlignment(Pos.CENTER_LEFT);
        jamHeader.setPadding(new Insets(0,0,10,0));
        Label jamIcon2 = new Label("\uD83D\uDCCB");
        jamIcon2.setStyle("-fx-font-size: 14px;");
        Label jamTitle = new Label("Jam Operasional");
        jamTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        jamHeader.getChildren().addAll(jamIcon2, jamTitle);
        jamSection.getChildren().add(jamHeader);

        List<String[]> jamList = dataService.getJamOperasional();
        for (String[] jam : jamList) {
            boolean bukaHari = "1".equals(jam[1]);
            String namaHari  = jam[0];
            boolean isToday  = namaHari.equalsIgnoreCase(hariIniStr);

            HBox jamRow = new HBox(0);
            jamRow.setAlignment(Pos.CENTER_LEFT);
            jamRow.setPadding(new Insets(7,10,7,10));
            if (isToday) {
                jamRow.setStyle("-fx-background-color: " + (buka ? "#f0fdf4" : "#fff5f5") + ";"
                        + "-fx-background-radius: 10; -fx-border-color: "
                        + (buka ? "#bbf7d0" : "#fecaca")
                        + "; -fx-border-width: 1; -fx-border-radius: 10;");
            }
            Label hariLbl = new Label(namaHari);
            hariLbl.setMinWidth(80);
            hariLbl.setStyle(isToday
                    ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (buka ? "#15803d;" : "#dc2626;")
                    : "-fx-font-size: 12px; -fx-text-fill: #555;");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label jamTxt = new Label(bukaHari ? jam[2] + " - " + jam[3] : "Libur");
            jamTxt.setStyle(bukaHari
                    ? (isToday ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (buka ? "#15803d;" : "#dc2626;")
                       : "-fx-font-size: 12px; -fx-text-fill: #333;")
                    : "-fx-font-size: 12px; -fx-text-fill: #e53e3e;");
            jamRow.getChildren().addAll(hariLbl, sp, jamTxt);
            if (isToday) {
                Label todayChip = new Label("Hari ini");
                todayChip.setStyle("-fx-background-color: " + (buka ? "#dcfce7" : "#fee2e2") + ";"
                        + "-fx-text-fill: " + (buka ? "#15803d" : "#dc2626") + ";"
                        + "-fx-font-size: 10px; -fx-font-weight: bold;"
                        + "-fx-padding: 2 8; -fx-background-radius: 20;");
                HBox.setMargin(todayChip, new Insets(0,0,0,10));
                jamRow.getChildren().add(todayChip);
            }
            jamSection.getChildren().add(jamRow);
        }
        card.getChildren().add(jamSection);

        List<HariLibur> liburList  = dataService.getAllHariLibur();
        List<HariLibur> liburDepan = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        for (HariLibur hl : liburList) {
            try {
                LocalDate tgl = LocalDate.parse(hl.getTanggal());
                if (!tgl.isBefore(today) && tgl.isBefore(today.plusDays(31))) liburDepan.add(hl);
            } catch (Exception ignored) {}
        }

        Separator div2 = new Separator();
        div2.setStyle("-fx-background-color: #f0f0f6;");
        card.getChildren().add(div2);

        VBox liburSection = new VBox(8);
        liburSection.setPadding(new Insets(14, 18, 14, 18));
        HBox liburHeader = new HBox(8);
        liburHeader.setAlignment(Pos.CENTER_LEFT);
        Label liburIcon = new Label("\uD83D\uDCC5");
        liburIcon.setStyle("-fx-font-size: 14px;");
        Label liburTitle = new Label("Hari Libur & Status (30 Hari ke Depan)");
        liburTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        liburHeader.getChildren().addAll(liburIcon, liburTitle);
        liburSection.getChildren().add(liburHeader);

        if (liburDepan.isEmpty()) {
            Label kosong = new Label("Tidak ada hari libur dalam 30 hari ke depan.");
            kosong.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-font-style: italic;");
            liburSection.getChildren().add(kosong);
        } else {
            for (HariLibur hl : liburDepan) {
                HBox liburRow = new HBox(10);
                liburRow.setAlignment(Pos.CENTER_LEFT);
                liburRow.setPadding(new Insets(6,10,6,10));
                liburRow.setStyle("-fx-background-color: #fff8e1; -fx-background-radius: 8;"
                        + "-fx-border-color: #fde68a; -fx-border-width: 1; -fx-border-radius: 8;");
                Label tglLibur = new Label(hl.getTanggal());
                tglLibur.setStyle("-fx-font-size: 11px; -fx-text-fill: #92400e; -fx-font-weight: bold;");
                tglLibur.setMinWidth(85);
                Label namaLibur = new Label(hl.getNama());
                namaLibur.setStyle("-fx-font-size: 12px; -fx-text-fill: #78350f;");
                HBox.setHgrow(namaLibur, Priority.ALWAYS);
                boolean isTutup = hl.isTutup();
                Label statusChip = new Label(isTutup ? "Tutup" : "Tetap Buka");
                statusChip.setStyle(isTutup
                        ? "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 20;"
                        : "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 20;");
                liburRow.getChildren().addAll(tglLibur, namaLibur, statusChip);
                liburSection.getChildren().add(liburRow);
            }
        }
        card.getChildren().add(liburSection);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");
        VBox wrap = new VBox(6, card, time);
        wrap.setAlignment(Pos.CENTER_LEFT);
        HBox row = new HBox(wrap);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2,8,2,8));
        chatContainer.getChildren().add(row);
        scrollToBottom();
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
        msg.setMaxWidth(360);
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


    private void addBotProductCards(List<Produk> produkList) {
        FlowPane grid = new FlowPane(10, 10);
        // 5 kolom × 175px + gap = ~925px
        grid.setPrefWrapLength(925);
        grid.setPadding(new Insets(4, 0, 4, 0));

        for (Produk prod : produkList) {

            StackPane imgPane = new StackPane();
            imgPane.setMinSize(165, 165);
            imgPane.setMaxSize(165, 165);
            imgPane.setStyle("-fx-background-color: #f0eeff; -fx-background-radius: 12 12 0 0;");

            String imgUrl = prod.getGambarUrl();
            boolean imgLoaded = false;

            if (imgUrl != null && !imgUrl.isBlank()) {
                try {
                    String resolvedUrl = resolveImageUrl(imgUrl);
                    if (resolvedUrl != null) {
                        Image img = new Image(resolvedUrl, 165, 165, true, true, false);
                        if (!img.isError()) {
                            ImageView iv = new ImageView(img);
                            iv.setFitWidth(165); iv.setFitHeight(165);
                            iv.setPreserveRatio(false);
                            Rectangle clip = new Rectangle(165, 165);
                            clip.setArcWidth(24); clip.setArcHeight(24);
                            iv.setClip(clip);
                            imgPane.getChildren().add(iv);
                            imgLoaded = true;
                        }
                    }
                } catch (Exception ex) { /* fallback */ }
            }

            if (!imgLoaded) {
                Label icon = new Label("\uD83D\uDDBC\uFE0F");
                icon.setStyle("-fx-font-size: 34px;");
                Label noImg = new Label("No Image");
                noImg.setStyle("-fx-font-size: 10px; -fx-text-fill: #bbb;");
                VBox ph = new VBox(4, icon, noImg);
                ph.setAlignment(Pos.CENTER);
                imgPane.getChildren().add(ph);
            }

            Label badgeKulit = new Label(prod.getJenisKulit() != null
                    ? prod.getJenisKulit() : "Semua Jenis Kulit");
            badgeKulit.setStyle(
                    "-fx-background-color: #4B3FC8; -fx-text-fill: white;" +
                            "-fx-font-size: 9px; -fx-font-weight: bold;" +
                            "-fx-padding: 3 7; -fx-background-radius: 6;");
            StackPane.setAlignment(badgeKulit, Pos.TOP_LEFT);
            StackPane.setMargin(badgeKulit, new Insets(8, 0, 0, 8));
            imgPane.getChildren().add(badgeKulit);

            Label namaLbl = new Label(prod.getNamaProduk());
            namaLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;");
            namaLbl.setWrapText(true);
            namaLbl.setMaxWidth(148);

            Label hargaLbl = new Label(prod.getHargaFormatted());
            hargaLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e53935;");

            Label katLbl = new Label(emojiKategori(prod.getKategori()) + " " + prod.getKategori());
            katLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

            Label kandLbl = new Label(prod.getKandungan());
            kandLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");
            kandLbl.setWrapText(true);
            kandLbl.setMaxWidth(148);

            Label areaLbl = new Label("\uD83D\uDCCD "
                    + (prod.getAreaTubuh() != null ? prod.getAreaTubuh() : "Muka"));
            areaLbl.setStyle(
                    "-fx-font-size: 9px; -fx-text-fill: #7c4daa;" +
                            "-fx-background-color: #f3e8ff;" +
                            "-fx-background-radius: 20; -fx-padding: 2 8;");

            Button btnDetail = new Button("Lihat Detail");
            btnDetail.setStyle(
                    "-fx-background-color: #4B3FC8; -fx-text-fill: white;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand;");
            btnDetail.setMaxWidth(Double.MAX_VALUE);
            btnDetail.setOnMouseEntered(e -> btnDetail.setStyle(
                    "-fx-background-color: #3a2fb0; -fx-text-fill: white;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand;"));
            btnDetail.setOnMouseExited(e -> btnDetail.setStyle(
                    "-fx-background-color: #4B3FC8; -fx-text-fill: white;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand;"));
            btnDetail.setOnAction(e -> showDetailProdukPopup(prod));

            VBox infoBox = new VBox(5, namaLbl, hargaLbl, katLbl, kandLbl, areaLbl, btnDetail);
            infoBox.setPadding(new Insets(9, 9, 9, 9));
            infoBox.setStyle("-fx-background-color: white;");

            VBox card = new VBox(0, imgPane, infoBox);
            card.setMinWidth(165); card.setMaxWidth(165);
            card.setCursor(javafx.scene.Cursor.HAND);

            String sNormal = "-fx-background-color: white;" +
                    "-fx-border-color: #e8e8f0; -fx-border-width: 1;" +
                    "-fx-border-radius: 12; -fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);";
            String sHover  = "-fx-background-color: #fdfcff;" +
                    "-fx-border-color: #4B3FC8; -fx-border-width: 1.5;" +
                    "-fx-border-radius: 12; -fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(75,63,200,0.18), 10, 0, 0, 3);";

            card.setStyle(sNormal);
            card.setOnMouseEntered(ev -> card.setStyle(sHover));
            card.setOnMouseExited(ev  -> card.setStyle(sNormal));
            card.setOnMouseClicked(ev -> showDetailProdukPopup(prod));

            grid.getChildren().add(card);
        }

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");
        VBox bubble = new VBox(6, grid, time);
        bubble.setAlignment(Pos.CENTER_LEFT);
        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 8, 2, 8));
        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void showDetailProdukPopup(Produk prod) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Detail Produk - " + prod.getNamaProduk());
        popup.setResizable(false);

        StackPane headerPane = new StackPane();
        headerPane.setMinHeight(220);
        headerPane.setStyle("-fx-background-color: #4B3FC8;");

        StackPane imgCircle = new StackPane();
        imgCircle.setMinSize(200, 200);
        imgCircle.setMaxSize(200, 200);
        imgCircle.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(255,255,255,0.4);" +
                        "-fx-border-width: 2; -fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 3);");

        String imgUrl = prod.getGambarUrl();
        boolean prodImgLoaded = false;
        if (imgUrl != null && !imgUrl.isBlank()) {
            try {
                String resolvedUrl = resolveImageUrl(imgUrl);
                if (resolvedUrl != null) {
                    Image img = new Image(resolvedUrl, 196, 196, true, true, false);
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(196); iv.setFitHeight(196);
                        iv.setPreserveRatio(true);
                        Rectangle clip = new Rectangle(196, 196);
                        clip.setArcWidth(20); clip.setArcHeight(20);
                        iv.setClip(clip);
                        imgCircle.getChildren().add(iv);
                        prodImgLoaded = true;
                    }
                }
            } catch (Exception ignored) {}
        }
        if (!prodImgLoaded) {
            Label fallback = new Label("\uD83D\uDDBC\uFE0F");
            fallback.setStyle("-fx-font-size: 52px;");
            imgCircle.getChildren().add(fallback);
        }

        Label namaProdukLbl = new Label(prod.getNamaProduk());
        namaProdukLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;" +
                "-fx-text-fill: white; -fx-wrap-text: true; -fx-text-alignment: center;");
        namaProdukLbl.setMaxWidth(360);
        namaProdukLbl.setWrapText(true);

        Label subLbl = new Label(prod.getKategori() + "  •  " + prod.getHargaFormatted());
        subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");

        VBox headerContent = new VBox(8, imgCircle, namaProdukLbl, subLbl);
        headerContent.setAlignment(Pos.CENTER);
        headerContent.setPadding(new Insets(20, 20, 20, 20));
        headerPane.getChildren().add(headerContent);

        VBox contentBox = new VBox(0);
        contentBox.setStyle("-fx-background-color: white;");

        Label hargaBesar = new Label(prod.getHargaFormatted());
        hargaBesar.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;" +
                "-fx-text-fill: #e53935; -fx-padding: 16 24 8 24;");

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(0, 24, 0, 24));

        VBox detailBox = new VBox(0);
        detailBox.setPadding(new Insets(12, 24, 12, 24));
        detailBox.getChildren().addAll(
                buildPopupRow("\uD83C\uDFF7", "Kategori",   prod.getKategori()),
                buildPopupRow("\uD83C\uDF3F", "Jenis Kulit",
                        prod.getJenisKulit() != null ? prod.getJenisKulit() : "Semua Jenis Kulit"),
                buildPopupRow("\uD83D\uDCCD", "Area Tubuh",
                        prod.getAreaTubuh() != null ? prod.getAreaTubuh() : "Muka"),
                buildPopupRow("\u2697",       "Kandungan",  prod.getKandungan())
        );

        VBox deskSection = new VBox(6);
        deskSection.setPadding(new Insets(0, 24, 16, 24));
        try {
            String deskripsi = prod.getDeskripsi();
            if (deskripsi != null && !deskripsi.isBlank()) {
                Separator sep2 = new Separator();
                Label deskTitle = new Label("\uD83D\uDCDD Deskripsi Produk");
                deskTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-text-fill: #4B3FC8; -fx-padding: 10 0 4 0;");
                Label deskVal = new Label(deskripsi);
                deskVal.setStyle("-fx-font-size: 12px; -fx-text-fill: #444;" +
                        "-fx-wrap-text: true; -fx-line-spacing: 2;");
                deskVal.setWrapText(true);
                deskVal.setMaxWidth(400);
                deskSection.getChildren().addAll(sep2, deskTitle, deskVal);
            }
        } catch (Exception ignored) {}

        contentBox.getChildren().addAll(hargaBesar, sep1, detailBox, deskSection);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        scrollPane.setPrefHeight(280);

        Button btnTutup = new Button("Tutup");
        btnTutup.setStyle(
                "-fx-background-color: #4B3FC8; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-padding: 10 0; -fx-background-radius: 8;" +
                        "-fx-cursor: hand; -fx-pref-height: 44;");
        btnTutup.setMaxWidth(Double.MAX_VALUE);
        btnTutup.setOnAction(e -> popup.close());

        btnTutup.setOnMouseEntered(e -> btnTutup.setStyle(
                "-fx-background-color: #3a2fb0; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-padding: 10 0; -fx-background-radius: 8;" +
                        "-fx-cursor: hand; -fx-pref-height: 44;"));
        btnTutup.setOnMouseExited(e -> btnTutup.setStyle(
                "-fx-background-color: #4B3FC8; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-padding: 10 0; -fx-background-radius: 8;" +
                        "-fx-cursor: hand; -fx-pref-height: 44;"));

        VBox btnBox = new VBox(btnTutup);
        btnBox.setPadding(new Insets(12, 24, 20, 24));
        btnBox.setStyle("-fx-background-color: white;");

        VBox root = new VBox(0, headerPane, scrollPane, btnBox);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 460, 560);
        popup.setScene(scene);
        popup.show();
    }

    private HBox buildPopupRow(String emoji, String label, String value) {
        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size: 16px;");
        emojiLbl.setMinWidth(28);

        Label labelLbl = new Label(label + ":");
        labelLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: bold;");
        labelLbl.setMinWidth(100);

        Label valueLbl = new Label(value != null ? value : "-");
        valueLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a2e;");
        valueLbl.setWrapText(true);
        valueLbl.setMaxWidth(280);

        HBox row = new HBox(10, emojiLbl, labelLbl, valueLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        row.setStyle("-fx-border-color: transparent transparent #f0f0f6 transparent;" +
                "-fx-border-width: 0 0 1 0;");
        return row;
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

    private void muatSidebarRiwayat() {
        if (riwayatListContainer == null) return;
        riwayatListContainer.getChildren().clear();
        List<String[]> riwayat = dataService.getRiwayatTerakhir(20);
        if (riwayat.isEmpty()) {
            if (riwayatEmptyLabel != null) riwayatEmptyLabel.setVisible(true);
            return;
        }
        if (riwayatEmptyLabel != null) riwayatEmptyLabel.setVisible(false);

        java.util.List<String[]> reversed = new java.util.ArrayList<>(riwayat);
        java.util.Collections.reverse(reversed);

        for (String[] r : reversed) {
            String pesan = r[0];
            String tag   = r.length > 2 ? r[2] : "";
            String waktu = r.length > 3 ? r[3] : "";
            String jam   = waktu.length() > 10 ? waktu.substring(11) : waktu;
            String pesanPendek = pesan.length() > 32 ? pesan.substring(0, 32) + "..." : pesan;

            VBox item = new VBox(4);
            item.setPadding(new Insets(9, 12, 9, 12));
            item.setCursor(javafx.scene.Cursor.HAND);
            String styleNormal = "-fx-background-color: transparent; -fx-background-radius: 10;";
            String styleHover  = "-fx-background-color: #f0eeff; -fx-background-radius: 10;";
            item.setStyle(styleNormal);
            item.setOnMouseEntered(e -> item.setStyle(styleHover));
            item.setOnMouseExited(e  -> item.setStyle(styleNormal));
            String pesanFinal = pesan;
            item.setOnMouseClicked(e -> { if (inputField != null) { inputField.setText(pesanFinal); prosesPesan(); } });

            HBox topRow = new HBox(7);
            topRow.setAlignment(Pos.CENTER_LEFT);
            Label ikonLbl = new Label(ikonUntukTag(tag));
            ikonLbl.setStyle("-fx-font-size: 13px;");
            Label pesanLbl = new Label(pesanPendek);
            pesanLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;");
            HBox.setHgrow(pesanLbl, Priority.ALWAYS);
            topRow.getChildren().addAll(ikonLbl, pesanLbl);

            HBox botRow = new HBox(6);
            botRow.setAlignment(Pos.CENTER_LEFT);
            if (tag != null && !tag.isBlank()) {
                Label tagLbl = new Label(tag);
                tagLbl.setStyle("-fx-background-color: #f0eeff; -fx-text-fill: #4B3FC8;" +
                        "-fx-font-size: 9px; -fx-padding: 2 7; -fx-background-radius: 20;");
                botRow.getChildren().add(tagLbl);
            }
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label jamLbl = new Label(jam);
            jamLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #bbb;");
            botRow.getChildren().addAll(sp, jamLbl);
            item.getChildren().addAll(topRow, botRow);

            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #f0f0f5;");
            riwayatListContainer.getChildren().addAll(item, sep);
        }
    }

    private String ikonUntukTag(String tag) {
        if (tag == null) return "\uD83D\uDCAC";
        switch (tag) {
            case "Produk":      return "\uD83D\uDED8";
            case "Info Toko":   return "\uD83C\uDFEA";
            case "Harga":       return "\uD83D\uDCB0";
            case "Rekomendasi": return "\u2728";
            case "Serum":       return "\uD83D\uDCA7";
            case "Toner":       return "\uD83E\uDDF4";
            case "Pelembab":    return "\uD83C\uDF3F";
            case "Sunscreen":   return "\u2600\uFE0F";
            case "Pembersih":   return "\uD83E\uDDFC";
            case "Katalog":     return "\uD83D\uDCCB";
            case "Sapaan":      return "\uD83D\uDC4B";
            case "Lokasi":      return "\uD83D\uDCCD";
            default:            return "\uD83D\uDCAC";
        }
    }

    private String emojiKategori(String kategori) {
        if (kategori == null) return "\u2728";
        switch (kategori) {
            case "Serum":       return "\u2728";
            case "Toner":       return "\uD83D\uDCA7";
            case "Pelembab":    return "\uD83C\uDF3F";
            case "Facial Wash": return "\uD83D\uDCA6";
            case "Sunscreen":   return "\u2600";
            case "Exfoliator":  return "\uD83C\uDF00";
            case "Body Care":   return "\uD83D\uDEBF";
            case "Eye Care":    return "\uD83D\uDC41";
            case "Lip Care":    return "\uD83D\uDC44";
            case "Hair Care":   return "\uD83D\uDC87";
            case "Hand Care":   return "\uD83D\uDC50";
            case "Acne Care":   return "\u2665";
            default:            return "\uD83C\uDF3F";
        }
    }

    private String tentukanTag(String pesan) {
        String p = pesan.toLowerCase();
        if (p.contains("halo") || p.contains("hai") || p.contains("hello")) return "Sapaan";
        if (p.contains("lokasi") || p.contains("alamat") || p.contains("maps")) return "Lokasi";
        if (p.contains("harga") || p.contains("berapa")) return "Harga";
        if (p.contains("rekomendasi") || p.contains("saran")) return "Rekomendasi";
        if (p.contains("serum"))     return "Serum";
        if (p.contains("toner"))     return "Toner";
        if (p.contains("pelembab") || p.contains("moisturizer")) return "Pelembab";
        if (p.contains("sunscreen") || p.contains("spf")) return "Sunscreen";
        if (p.contains("pembersih") || p.contains("cleanser")) return "Pembersih";
        if (p.contains("produk") || p.contains("semua")) return "Katalog";
        return "Umum";
    }

    private String resolveImageUrl(String path) {
        if (path == null || path.isBlank()) return null;
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:"))
            return path;
        File f = new File(path);
        if (f.exists()) return f.toURI().toString();
        File f2 = new File(System.getProperty("user.dir"), path);
        if (f2.exists()) return f2.toURI().toString();
        File f3 = new File(System.getProperty("user.home"), path);
        if (f3.exists()) return f3.toURI().toString();
        return null;
    }

    private void scrollToBottom() {
        chatContainer.layout();
        chatScrollPane.setVvalue(1.0);
    }

    @FXML public void hapusRiwayat() { hapusRiwayatSidebar(); }

    @FXML
    public void hapusRiwayatSidebar() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Riwayat");
        konfirmasi.setHeaderText("Hapus semua riwayat percakapan?");
        konfirmasi.setContentText("Tindakan ini tidak dapat dibatalkan.");
        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            dataService.hapusSemuaRiwayat();
            chatContainer.getChildren().clear();
            addWelcomeCard();
            muatSidebarRiwayat();
        }
    }

    @FXML @SuppressWarnings("unused") public void showChatPanel() {}

    @FXML public void showInfoToko() { addStatusTokoCard(); scrollToBottom(); }

    @FXML
    public void showRiwayat() {
        muatSidebarRiwayat();
        List<String[]> riwayat = dataService.getRiwayatTerakhir(1);
        if (riwayat.isEmpty())
            addBotMessage("\uD83D\uDCAC Belum ada riwayat percakapan.");
        else
            addBotMessage("\uD83D\uDD50 Riwayat percakapan sudah diperbarui di panel kiri.");
        scrollToBottom();
    }

    @FXML
    public void showBantuan() {
        addBotMessage("Bantuan:\n"
                + "\u2022 Ketik nama produk untuk info harga\n"
                + "\u2022 Ketik kategori (moisturizer, toner, serum, dll)\n"
                + "\u2022 Tanya rekomendasi untuk jenis kulit tertentu\n"
                + "\u2022 Tanya jam buka toko atau lokasi toko");
    }

    @FXML
    public void handleKembali() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) inputField.getScene().getWindow();
            new org.sahabatlaris.chatbot.ui.Main().showPilihMode(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}