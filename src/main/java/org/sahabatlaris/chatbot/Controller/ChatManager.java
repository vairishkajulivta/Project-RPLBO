package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.service.DatabaseService.HariLibur;
import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.service.ChatbotService;
import org.sahabatlaris.chatbot.service.ManagedDataService;
import javafx.animation.PauseTransition;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ChatManager {

    @FXML private TextField   inputField;
    @FXML private VBox        chatContainer;
    @FXML private ScrollPane  chatScrollPane;
    @FXML @SuppressWarnings("unused") private Button btnChat;
    @FXML @SuppressWarnings("unused") private Circle onlineDot;
    @FXML @SuppressWarnings("unused") private Label  onlineLabel;

    // ── Sidebar Riwayat ───────────────────────────────────────────────────────
    @FXML private VBox        riwayatListContainer; // VBox isi item riwayat di sidebar
    @FXML private Label       riwayatEmptyLabel;    // Label "Belum ada riwayat"
    @FXML @SuppressWarnings("unused") private Button btnHapusRiwayat; // Hapus Riwayat di sidebar

    private ChatbotService     botService  = new ChatbotService();
    private ManagedDataService dataService = new ManagedDataService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH.mm");

    @FXML
    public void initialize() {
        addWelcomeCard();
        muatSidebarRiwayat();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  WELCOME — Bubble sapaan bot sederhana seperti chat biasa
    // ════════════════════════════════════════════════════════════════════════
    private void addWelcomeCard() {
        String[] info   = dataService.getInfoToko();
        String namaToko = (info.length > 0 && !info[0].isBlank()) ? info[0] : "SahabatLaris";

        // ── Bubble sapaan ──────────────────────────────────────────────────
        Label msg = new Label(
                "Halo! Selamat datang di " + namaToko + " \uD83D\uDC4B\n"
                        + "Saya bisa membantu Anda mencari informasi produk skincare.\n\n"
                        + "Coba tanyakan:\n"
                        + "\u2022 Harga moisturizer berapa?\n"
                        + "\u2022 Rekomendasi serum\n"
                        + "\u2022 Produk untuk kulit sensitif");
        msg.getStyleClass().add("bubble-bot-text");
        msg.setWrapText(true);
        msg.setMaxWidth(380);

        VBox bubbleBox = new VBox(4);
        bubbleBox.getStyleClass().add("bubble-bot");
        bubbleBox.getChildren().add(msg);

        // ── Quick reply chips ──────────────────────────────────────────────
        FlowPane chips = new FlowPane(8, 6);
        chips.setMaxWidth(400);
        chips.setPadding(new Insets(8, 0, 0, 0));

        String[][] quickReplies = {
                {"\uD83C\uDF3F Kulit Sensitif",   "produk untuk kulit sensitif"},
                {"\uD83D\uDCAB Rekomendasi",       "rekomendasi produk"},
                {"\uD83D\uDCB0 Cek Harga",         "harga semua produk"},
                {"\uD83E\uDDF4 Serum",             "serum"},
                {"\uD83E\uDDF4 Toner",             "toner"},
                {"\uD83C\uDFEA Status Toko",       "status toko"}
        };

        for (String[] qr : quickReplies) {
            Button chip = new Button(qr[0]);
            chip.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #4B3FC8;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #c5c0f0;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 20;" +
                            "-fx-padding: 5 12;" +
                            "-fx-cursor: hand;");
            String pesanChip = qr[1];
            chip.setOnAction(e -> {
                if (inputField != null) {
                    inputField.setText(pesanChip);
                    prosesPesan();
                }
            });
            chip.setOnMouseEntered(e -> chip.setStyle(
                    "-fx-background-color: #f0eeff;" +
                            "-fx-text-fill: #4B3FC8;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #4B3FC8;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 20;" +
                            "-fx-padding: 5 12;" +
                            "-fx-cursor: hand;"));
            chip.setOnMouseExited(e -> chip.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #4B3FC8;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #c5c0f0;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 20;" +
                            "-fx-padding: 5 12;" +
                            "-fx-cursor: hand;"));
            chips.getChildren().add(chip);
        }

        VBox fullBubble = new VBox(0);
        fullBubble.getStyleClass().add("bubble-bot");
        fullBubble.setMaxWidth(420);
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

        HBox row = new HBox(wrap);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 8, 2, 8));

        chatContainer.getChildren().add(row);
        scrollToBottom();
    }


    /** Bangun section hari libur 30 hari ke depan */
    private VBox buildHariLiburSection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(12, 16, 14, 16));

        Label titleLbl = new Label("📅  Hari Libur & Status Toko (30 Hari ke Depan)");
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        section.getChildren().add(titleLbl);

        VBox liburList = new VBox(6);
        liburList.setPadding(new Insets(10, 0, 0, 0));

        LocalDate hari = LocalDate.now();
        List<HariLibur> daftarLibur = dataService.getAllHariLibur();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy",
                new java.util.Locale("id", "ID"));

        int count = 0;
        for (HariLibur hl : daftarLibur) {
            try {
                LocalDate tgl = LocalDate.parse(hl.getTanggal());
                if (!tgl.isBefore(hari) && tgl.isBefore(hari.plusDays(30))) {
                    liburList.getChildren().add(buildHariLiburRow(tgl, hl, fmt));
                    count++;
                }
            } catch (Exception ignored) {}
        }

        if (count == 0) {
            Label emptyLbl = new Label("Tidak ada hari libur dalam 30 hari ke depan.");
            emptyLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-padding: 8 0 0 0;");
            liburList.getChildren().add(emptyLbl);
        }

        section.getChildren().add(liburList);
        return section;
    }

    /** Satu baris kartu hari libur */
    private HBox buildHariLiburRow(LocalDate tgl, HariLibur hl,
                                   DateTimeFormatter fmt) {
        boolean tutup = hl.isTutup();

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle(tutup
                ? "-fx-background-color: #fff5f5; -fx-background-radius: 10; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 10;"
                : "-fx-background-color: #f0fdf4; -fx-background-radius: 10; -fx-border-color: #bbf7d0; -fx-border-width: 1; -fx-border-radius: 10;");

        // Ikon status
        Label ikonLbl = new Label(tutup ? "❌" : "✅");
        ikonLbl.setStyle("-fx-font-size: 16px;");

        // Info nama + tanggal
        VBox infoBox = new VBox(2);
        Label namaLbl = new Label(hl.getNama());
        namaLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " +
                (tutup ? "#c62828;" : "#1b5e20;"));

        Label tglLbl = new Label(tgl.format(fmt) + "  •  " + hl.getKeterangan());
        tglLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        tglLbl.setWrapText(true);

        infoBox.getChildren().addAll(namaLbl, tglLbl);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Badge kanan
        Label badgeLbl = new Label(tutup ? "Tutup" : "Buka");
        badgeLbl.setStyle(tutup
                ? "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 20;"
                : "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 20;");

        row.getChildren().addAll(ikonLbl, infoBox, badgeLbl);
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PROSES PESAN CHAT
    // ════════════════════════════════════════════════════════════════════════
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

            // Cek apakah user tanya status toko / hari libur
            String pesanLower = pesan.toLowerCase();
            if (pesanLower.contains("buka") || pesanLower.contains("tutup")
                    || pesanLower.contains("jam") || pesanLower.contains("operasional")
                    || pesanLower.contains("libur") || pesanLower.contains("status toko")
                    || pesanLower.contains("hari libur")) {
                addStatusTokoCard();
                dataService.tambahRiwayat(pesan, "Info status/jam toko ditampilkan.", "Info Toko");
                muatSidebarRiwayat();
                return;
            }

            // Selalu pakai cariJawaban agar filter area+kulit berjalan dengan benar
            String jawaban = botService.cariJawaban(pesan);
            List<Produk> produkResult = botService.getLastProdukResult();

            // Jika tidak ada dari lastProdukResult, coba getProdukMentioned
            // hanya untuk kasus nama produk spesifik disebut langsung
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

    /** Tampilkan kartu status toko + jam operasional yang bagus */
    private void addStatusTokoCard() {
        String[] info   = dataService.getInfoToko();
        String namaToko = info.length > 0 && !info[0].isBlank() ? info[0] : "SahabatLaris";
        String[] status = dataService.cekStatusTokoHariIni();
        boolean buka    = "buka".equalsIgnoreCase(status[0]);

        String hariIniStr = LocalDate.now().getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("id","ID"));
        String tglStr = LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", new java.util.Locale("id","ID")));

        // ── Card utama ────────────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setMaxWidth(430);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e8e8f0; -fx-border-width: 1;" +
                        "-fx-border-radius: 18; -fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 12, 0, 0, 3);");

        // ── Header: status BUKA / TUTUP ───────────────────────────────────────
        VBox header = new VBox(6);
        header.setPadding(new Insets(16, 18, 14, 18));
        header.setStyle(buka
                ? "-fx-background-color: #f0fdf4; -fx-background-radius: 18 18 0 0;"
                : "-fx-background-color: #fff5f5; -fx-background-radius: 18 18 0 0;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Ikon status bulat
        StackPane ikonCircle = new StackPane();
        ikonCircle.setMinSize(40, 40);
        ikonCircle.setMaxSize(40, 40);
        ikonCircle.setStyle(buka
                ? "-fx-background-color: #dcfce7; -fx-background-radius: 20;"
                : "-fx-background-color: #fee2e2; -fx-background-radius: 20;");
        Label ikonLbl = new Label(buka ? "✓" : "✕");
        ikonLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " +
                (buka ? "#16a34a;" : "#dc2626;"));
        ikonCircle.getChildren().add(ikonLbl);

        VBox statusInfo = new VBox(3);
        Label statusLbl = new Label("Toko " + (buka ? "BUKA" : "TUTUP") + " Sekarang");
        statusLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " +
                (buka ? "#15803d;" : "#dc2626;"));
        Label subLbl = new Label(status[1]);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        statusInfo.getChildren().addAll(statusLbl, subLbl);
        HBox.setHgrow(statusInfo, Priority.ALWAYS);

        // Badge tanggal hari ini
        Label tglBadge = new Label(hariIniStr + ", " + tglStr);
        tglBadge.setStyle(
                "-fx-background-color: " + (buka ? "#dcfce7" : "#fee2e2") + ";" +
                        "-fx-text-fill: " + (buka ? "#15803d" : "#dc2626") + ";" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-padding: 3 9; -fx-background-radius: 20;");

        topRow.getChildren().addAll(ikonCircle, statusInfo, tglBadge);
        header.getChildren().add(topRow);

        // Jam buka hari ini (jika buka)
        if (!"-".equals(status[2])) {
            HBox jamHariIni = new HBox(6);
            jamHariIni.setAlignment(Pos.CENTER_LEFT);
            Label jamIcon = new Label("🕐");
            jamIcon.setStyle("-fx-font-size: 12px;");
            Label jamLbl2 = new Label("Jam buka hari ini: " + status[2] + " – " + status[3]);
            jamLbl2.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-weight: bold;");
            jamHariIni.getChildren().addAll(jamIcon, jamLbl2);
            header.getChildren().add(jamHariIni);
        }

        card.getChildren().add(header);

        // ── Divider ───────────────────────────────────────────────────────────
        Separator div1 = new Separator();
        div1.setPadding(new Insets(0));
        div1.setStyle("-fx-background-color: #f0f0f6;");
        card.getChildren().add(div1);

        // ── Jam Operasional Section ───────────────────────────────────────────
        VBox jamSection = new VBox(0);
        jamSection.setPadding(new Insets(14, 18, 14, 18));

        HBox jamHeader = new HBox(8);
        jamHeader.setAlignment(Pos.CENTER_LEFT);
        jamHeader.setPadding(new Insets(0, 0, 10, 0));
        Label jamIcon2 = new Label("📋");
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
            jamRow.setPadding(new Insets(7, 10, 7, 10));
            if (isToday) {
                jamRow.setStyle(
                        "-fx-background-color: " + (buka ? "#f0fdf4" : "#fff5f5") + ";" +
                                "-fx-background-radius: 10;" +
                                "-fx-border-color: " + (buka ? "#bbf7d0" : "#fecaca") + ";" +
                                "-fx-border-width: 1; -fx-border-radius: 10;");
            }

            Label hariLbl = new Label(namaHari);
            hariLbl.setMinWidth(80);
            hariLbl.setStyle(isToday
                    ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (buka ? "#15803d;" : "#dc2626;")
                    : "-fx-font-size: 12px; -fx-text-fill: #555;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label jamTxt = new Label(bukaHari ? jam[2] + " – " + jam[3] : "Libur");
            jamTxt.setStyle(bukaHari
                    ? (isToday
                       ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (buka ? "#15803d;" : "#dc2626;")
                       : "-fx-font-size: 12px; -fx-text-fill: #333;")
                    : "-fx-font-size: 12px; -fx-text-fill: #e53e3e;");

            jamRow.getChildren().addAll(hariLbl, sp, jamTxt);

            if (isToday) {
                Label todayChip = new Label("Hari ini");
                todayChip.setStyle(
                        "-fx-background-color: " + (buka ? "#dcfce7" : "#fee2e2") + ";" +
                                "-fx-text-fill: " + (buka ? "#15803d" : "#dc2626") + ";" +
                                "-fx-font-size: 10px; -fx-font-weight: bold;" +
                                "-fx-padding: 2 8; -fx-background-radius: 20;");
                HBox.setMargin(todayChip, new Insets(0, 0, 0, 10));
                jamRow.getChildren().add(todayChip);
            }
            jamSection.getChildren().add(jamRow);
        }
        card.getChildren().add(jamSection);

        // ── Hari libur section ────────────────────────────────────────────────
        List<HariLibur> liburList = dataService.getAllHariLibur();
        // Filter 30 hari ke depan
        List<HariLibur> liburDepan = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        for (HariLibur hl : liburList) {
            try {
                LocalDate tgl = LocalDate.parse(hl.getTanggal());
                if (!tgl.isBefore(today) && tgl.isBefore(today.plusDays(31)))
                    liburDepan.add(hl);
            } catch (Exception ignored) {}
        }

        Separator div2 = new Separator();
        div2.setStyle("-fx-background-color: #f0f0f6;");
        card.getChildren().add(div2);

        VBox liburSection = new VBox(8);
        liburSection.setPadding(new Insets(14, 18, 14, 18));

        HBox liburHeader = new HBox(8);
        liburHeader.setAlignment(Pos.CENTER_LEFT);
        Label liburIcon = new Label("📅");
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
                liburRow.setPadding(new Insets(6, 10, 6, 10));
                liburRow.setStyle(
                        "-fx-background-color: #fff8e1; -fx-background-radius: 8;" +
                                "-fx-border-color: #fde68a; -fx-border-width: 1; -fx-border-radius: 8;");

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

        // ── Wrapper & tambah ke chat ──────────────────────────────────────────
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

    // ════════════════════════════════════════════════════════════════════════
    //  PESAN TEKS BIASA
    // ════════════════════════════════════════════════════════════════════════
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
                    String resolvedUrl = resolveImageUrl(imgUrl);
                    if (resolvedUrl != null) {
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
                    }
                } catch (Exception ex) { /* fallback ke placeholder */ }
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

            String areaTubuhStr = prod.getAreaTubuh() != null ? prod.getAreaTubuh() : "Muka";
            Label badgeArea = new Label("\uD83D\uDCCD " + areaTubuhStr);
            badgeArea.setStyle(
                    "-fx-font-size: 10px; -fx-text-fill: #7c4daa;" +
                            "-fx-background-color: #f3e8ff;" +
                            "-fx-background-radius: 20; -fx-padding: 3 10 3 10;");

            HBox badgeRow = new HBox(6, badgeKulit, badgeArea);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            VBox infoBox = new VBox(6, nameLbl, katLbl, hargaLbl, kandLbl, badgeRow);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            infoBox.setPadding(new Insets(2, 0, 2, 0));
            HBox.setHgrow(infoBox, Priority.ALWAYS);

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

    // ════════════════════════════════════════════════════════════════════════
    //  SIDEBAR RIWAYAT — daftar "TERAKHIR DITANYA" di panel kiri
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Muat/refresh daftar riwayat ke sidebar kiri.
     * Dipanggil saat initialize() dan setiap kali ada pesan baru / hapus riwayat.
     */
    private void muatSidebarRiwayat() {
        if (riwayatListContainer == null) return; // FXML belum terhubung

        riwayatListContainer.getChildren().clear();

        List<String[]> riwayat = dataService.getRiwayatTerakhir(20);

        if (riwayat.isEmpty()) {
            // Tampilkan label "Belum ada riwayat"
            if (riwayatEmptyLabel != null) riwayatEmptyLabel.setVisible(true);
            return;
        }

        if (riwayatEmptyLabel != null) riwayatEmptyLabel.setVisible(false);

        // Tampilkan dari yang terbaru
        java.util.List<String[]> reversed = new java.util.ArrayList<>(riwayat);
        java.util.Collections.reverse(reversed);

        for (String[] r : reversed) {
            // r[0]=pesan, r[1]=balasan, r[2]=tag, r[3]=waktu
            String pesan  = r[0];
            String tag    = r.length > 2 ? r[2] : "";
            String waktu  = r.length > 3 ? r[3] : "";
            String jam    = waktu.length() > 10 ? waktu.substring(11) : waktu;

            // Potong teks pesan jika terlalu panjang
            String pesanPendek = pesan.length() > 32 ? pesan.substring(0, 32) + "…" : pesan;

            // ── Item sidebar ──────────────────────────────────────────────
            VBox item = new VBox(4);
            item.setPadding(new Insets(9, 12, 9, 12));
            item.setCursor(javafx.scene.Cursor.HAND);
            String styleNormal = "-fx-background-color: transparent; -fx-background-radius: 10;";
            String styleHover  = "-fx-background-color: #f0eeff; -fx-background-radius: 10;";
            item.setStyle(styleNormal);
            item.setOnMouseEntered(e -> item.setStyle(styleHover));
            item.setOnMouseExited(e  -> item.setStyle(styleNormal));

            // Klik item → kirim ulang pesan tersebut ke chat
            String pesanFinal = pesan;
            item.setOnMouseClicked(e -> {
                if (inputField != null) {
                    inputField.setText(pesanFinal);
                    prosesPesan();
                }
            });

            // Baris atas: ikon + teks pesan
            HBox topRow = new HBox(7);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label ikonLbl = new Label(ikonUntukTag(tag));
            ikonLbl.setStyle("-fx-font-size: 13px;");

            Label pesanLbl = new Label(pesanPendek);
            pesanLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;");
            pesanLbl.setWrapText(false);
            HBox.setHgrow(pesanLbl, Priority.ALWAYS);

            topRow.getChildren().addAll(ikonLbl, pesanLbl);

            // Baris bawah: tag badge + jam
            HBox botRow = new HBox(6);
            botRow.setAlignment(Pos.CENTER_LEFT);

            if (tag != null && !tag.isBlank()) {
                Label tagLbl = new Label(tag);
                tagLbl.setStyle(
                        "-fx-background-color: #f0eeff; -fx-text-fill: #4B3FC8;" +
                                "-fx-font-size: 9px; -fx-padding: 2 7; -fx-background-radius: 20;");
                botRow.getChildren().add(tagLbl);
            }

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label jamLbl = new Label(jam);
            jamLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #bbb;");

            botRow.getChildren().addAll(sp, jamLbl);

            item.getChildren().addAll(topRow, botRow);

            // Separator tipis
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #f0f0f5; -fx-padding: 0;");
            sep.setPadding(new Insets(0, 12, 0, 12));

            riwayatListContainer.getChildren().addAll(item, sep);
        }
    }

    /** Ikon emoji berdasarkan tag riwayat */
    private String ikonUntukTag(String tag) {
        if (tag == null) return "💬";
        switch (tag) {
            case "Produk":      return "🛍";
            case "Info Toko":   return "🏪";
            case "Harga":       return "💰";
            case "Rekomendasi": return "✨";
            case "Serum":       return "💧";
            case "Toner":       return "🧴";
            case "Pelembab":    return "🌿";
            case "Sunscreen":   return "☀️";
            case "Pembersih":   return "🧼";
            case "Katalog":     return "📋";
            case "Sapaan":      return "👋";
            case "Lokasi":      return "📍";
            default:            return "💬";
        }
    }

    /** Tentukan tag kategori riwayat berdasarkan isi pesan */
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

    /**
     * Resolve path gambar ke URL yang bisa dibuka JavaFX.
     * Coba absolut, working dir, user.home.
     */
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


    /** Alias untuk kompatibilitas FXML lama yang pakai #hapusRiwayat */
    @FXML
    public void hapusRiwayat() {
        hapusRiwayatSidebar();
    }

    /** Handler tombol "Hapus Riwayat" di sidebar kiri */
    @FXML
    public void hapusRiwayatSidebar() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Riwayat");
        konfirmasi.setHeaderText("Hapus semua riwayat percakapan?");
        konfirmasi.setContentText("Tindakan ini tidak dapat dibatalkan.");
        Optional<ButtonType> hasil = konfirmasi.showAndWait();
        if (hasil.isPresent() && hasil.get() == ButtonType.OK) {
            dataService.hapusSemuaRiwayat();
            // Bersihkan seluruh chat dan kembali ke tampilan awal
            chatContainer.getChildren().clear();
            addWelcomeCard();
            muatSidebarRiwayat();
        }
    }

    @FXML @SuppressWarnings("unused") public void showChatPanel() {
        // Tidak perlu panel switching - semuanya dalam satu chat
    }

    @FXML
    public void showInfoToko() {
        // Tampilkan info toko langsung di area chat (tidak ada panel terpisah)
        addStatusTokoCard();
        scrollToBottom();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RIWAYAT CHAT — Panel overlay di dalam chatContainer
    // ════════════════════════════════════════════════════════════════════════
    //  RIWAYAT CHAT — Tombol navbar atas "Riwayat Chat"
    //  Tombol ini me-refresh sidebar dan scroll sidebar ke posisi teratas.
    //  Tampilan riwayat ada di sidebar kiri (riwayatListContainer di FXML).
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    public void showRiwayat() {
        // Refresh sidebar agar data terbaru tampil
        muatSidebarRiwayat();
        // Tampilkan konfirmasi singkat di chat bahwa riwayat ada di sidebar kiri
        List<String[]> riwayat = dataService.getRiwayatTerakhir(1);
        if (riwayat.isEmpty()) {
            addBotMessage("💬 Belum ada riwayat percakapan.\nMulai chat untuk menyimpan riwayat di sidebar kiri.");
        } else {
            addBotMessage("🕐 Riwayat percakapan sudah diperbarui di panel kiri.\nKlik salah satu item untuk mengulangi pertanyaan.");
        }
        scrollToBottom();
    }

    /** Format tanggal
     /** Format tanggal dari "yyyy-MM-dd" ke "Senin, 10 Mei 2026" */
    private String formatTanggal(String raw) {
        try {
            LocalDate d = LocalDate.parse(raw);
            return d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",
                    new java.util.Locale("id", "ID")));
        } catch (Exception e) {
            return raw;
        }
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