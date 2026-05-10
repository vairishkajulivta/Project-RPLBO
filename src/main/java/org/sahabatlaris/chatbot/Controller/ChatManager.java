package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.service.ChatbotService;
import org.sahabatlaris.chatbot.service.DatabaseService;
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

    @FXML private TextField  inputField;
    @FXML private VBox       chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private Button     btnChat;
    @FXML private VBox       sidebarRiwayatContainer;

    private ChatbotService   botService = new ChatbotService();
    private DatabaseService  db         = DatabaseService.getInstance();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH.mm");

    // ═══════════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        showWelcomeScreen();
        refreshSidebarRiwayat();
    }

    /** Tampilan welcome di tengah layar — mirip screenshot: bubble teks + tombol kategori */
    private void showWelcomeScreen() {
        VBox center = new VBox(0);
        center.setAlignment(Pos.CENTER);
        VBox.setVgrow(center, Priority.ALWAYS);

        // ── Bubble pesan sambutan (gaya bot bubble) ──────────────────────────
        String[] infoToko = db.getInfoToko();
        String namaToko = (infoToko != null && infoToko.length > 0 && !infoToko[0].isBlank())
                ? infoToko[0] : "SahabatLaris";

        VBox bubbleBox = new VBox(10);
        bubbleBox.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:16 16 16 4;" +
            "-fx-border-color:#e5e5e5; -fx-border-radius:16 16 16 4;" +
            "-fx-padding:18 20 18 20;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);"
        );
        bubbleBox.setMaxWidth(500);

        // Baris pertama: Halo + nama toko
        Label haloLbl = new Label("Halo! Selamat datang di " + namaToko + " 👋");
        haloLbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e;");
        haloLbl.setWrapText(true);

        Label subLbl = new Label("Saya bisa membantu Anda mencari informasi produk skincare.");
        subLbl.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");
        subLbl.setWrapText(true);

        // Separator visual
        Label sepLbl = new Label("");
        sepLbl.setMinHeight(4);

        // "Coba tanyakan:"
        Label cobaLbl = new Label("Coba tanyakan:");
        cobaLbl.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");

        // Daftar contoh pertanyaan
        String[][] contoh = {
            {"—", "Harga moisturizer berapa?"},
            {"—", "Rekomendasi serum"},
            {"—", "Produk untuk kulit sensitif"},
        };

        VBox contohBox = new VBox(4);
        for (String[] c : contoh) {
            Label lbl = new Label(c[0] + "  " + c[1]);
            lbl.setStyle("-fx-font-size:13px; -fx-text-fill:#444;");
            contohBox.getChildren().add(lbl);
        }

        bubbleBox.getChildren().addAll(haloLbl, subLbl, sepLbl, cobaLbl, contohBox);

        // ── Tombol-tombol kategori cepat ─────────────────────────────────────
        String[][] kategoriBtn = {
            {"🌿", "Kulit Sensitif",  "rekomendasi untuk kulit sensitif"},
            {"🔁", "Rekomendasi",     "rekomendasi produk skincare"},
            {"💰", "Cek Harga",       "daftar harga semua produk"},
            {"✨", "Serum",           "serum"},
            {"🧴", "Toner",           "toner"},
            {"🏪", "Status Toko",     "apakah toko buka hari ini"},
        };

        // Baris 1: 3 tombol
        HBox row1 = new HBox(8);
        row1.setAlignment(Pos.CENTER);

        // Baris 2: 3 tombol
        HBox row2 = new HBox(8);
        row2.setAlignment(Pos.CENTER);

        for (int i = 0; i < kategoriBtn.length; i++) {
            String[] k = kategoriBtn[i];
            HBox btn = buildKategoriChip(k[0], k[1], k[2]);
            if (i < 3) row1.getChildren().add(btn);
            else       row2.getChildren().add(btn);
        }

        VBox btnSection = new VBox(8, row1, row2);
        btnSection.setAlignment(Pos.CENTER);
        btnSection.setPadding(new Insets(14, 0, 0, 0));

        // Timestamp
        Label timeLbl = new Label(LocalTime.now().format(TIME_FMT));
        timeLbl.getStyleClass().add("timestamp");
        timeLbl.setPadding(new Insets(4, 0, 0, 4));

        center.getChildren().addAll(bubbleBox, timeLbl, btnSection);

        StackPane wrapper = new StackPane(center);
        StackPane.setAlignment(center, Pos.CENTER);
        wrapper.setPrefHeight(480);
        wrapper.setStyle("-fx-background-color:transparent;");
        wrapper.setPadding(new Insets(0, 40, 0, 40));

        chatContainer.getChildren().add(wrapper);
    }

    /** Buat satu chip/tombol kategori bergaya outline */
    private HBox buildKategoriChip(String emoji, String label, String query) {
        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size:13px;");

        Label textLbl = new Label(label);
        textLbl.setStyle("-fx-font-size:12.5px; -fx-font-weight:600; -fx-text-fill:#333;");

        HBox chip = new HBox(6, emojiLbl, textLbl);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(8, 14, 8, 14));

        String sNormal =
            "-fx-background-color:white;" +
            "-fx-border-color:#d0cef5; -fx-border-width:1.5;" +
            "-fx-border-radius:20; -fx-background-radius:20;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),3,0,0,1);";
        String sHover =
            "-fx-background-color:#f0eeff;" +
            "-fx-border-color:#4B3FC8; -fx-border-width:1.5;" +
            "-fx-border-radius:20; -fx-background-radius:20;" +
            "-fx-cursor:hand;";

        chip.setStyle(sNormal);
        chip.setOnMouseEntered(e -> chip.setStyle(sHover));
        chip.setOnMouseExited(e -> chip.setStyle(sNormal));
        chip.setOnMouseClicked(e -> {
            chatContainer.getChildren().clear();
            inputField.setText(query);
            prosesPesan();
        });

        return chip;
    }

    // ═══════════════════════════════════════════════════════════════
    //  KIRIM PESAN
    // ═══════════════════════════════════════════════════════════════

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
                String ringkasan = "Menampilkan " + produkDisebut.size() + " produk.";
                simpanRiwayat(pesan, ringkasan, "produk");
                addBotProductCards(produkDisebut);
            } else {
                String jawaban = botService.cariJawaban(pesan);
                List<Produk> produkResult = botService.getProdukDariJawaban(pesan, jawaban);

                if (produkResult != null && !produkResult.isEmpty()) {
                    String header = jawaban.split("\n")[0];
                    addBotMessage(header);
                    simpanRiwayat(pesan, header + " (" + produkResult.size() + " produk)", "produk");
                    addBotProductCards(produkResult);
                } else {
                    addBotMessage(jawaban);
                    String tag = deteksiTag(pesan, jawaban);
                    simpanRiwayat(pesan, jawaban, tag);
                }
            }
            refreshSidebarRiwayat();
        });
        pause.play();
    }

    // ═══════════════════════════════════════════════════════════════
    //  RIWAYAT CHAT — tampilan baru yang lebih jelas
    // ═══════════════════════════════════════════════════════════════

    @FXML
    public void showRiwayat() {
        List<String[]> daftarRiwayat = db.getRiwayatTerakhir(10);

        // Header kartu riwayat
        VBox headerBox = new VBox(6);
        headerBox.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#e8e8f0; -fx-border-width:1;" +
            "-fx-border-radius:14; -fx-background-radius:14;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);"
        );
        headerBox.setPadding(new Insets(16));
        headerBox.setMaxWidth(500);

        // Icon + judul
        HBox headRow = new HBox(10);
        headRow.setAlignment(Pos.CENTER_LEFT);

        StackPane headIcon = new StackPane();
        headIcon.setMinSize(38, 38);
        headIcon.setMaxSize(38, 38);
        headIcon.setStyle("-fx-background-color:linear-gradient(to bottom right,#4B3FC8,#818cf8); -fx-background-radius:10;");
        Label headIconLbl = new Label("🕐");
        headIconLbl.setStyle("-fx-font-size:18px;");
        headIcon.getChildren().add(headIconLbl);

        VBox headText = new VBox(2);
        Label headTitle = new Label("10 Percakapan Terakhir");
        headTitle.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:#1a1a2e;");
        Label headSub = new Label("Klik untuk mengulangi pertanyaan");
        headSub.setStyle("-fx-font-size:11px; -fx-text-fill:#aaa;");
        headText.getChildren().addAll(headTitle, headSub);

        headRow.getChildren().addAll(headIcon, headText);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#e8e8f0;");

        headerBox.getChildren().addAll(headRow, sep);

        if (daftarRiwayat.isEmpty()) {
            Label empty = new Label("Belum ada riwayat percakapan.\nMulai bertanya untuk menyimpan riwayat.");
            empty.setStyle("-fx-font-size:12px; -fx-text-fill:#aaa; -fx-text-alignment:center;");
            empty.setWrapText(true);
            headerBox.getChildren().add(empty);
        } else {
            VBox itemsBox = new VBox(6);
            itemsBox.setPadding(new Insets(4, 0, 0, 0));

            for (String[] r : daftarRiwayat) {
                // r[0]=id | r[1]=pesan | r[2]=balasan | r[3]=waktu | r[4]=tag
                if (r.length < 2) continue;
                String rId      = r.length >= 1 ? r[0] : "";
                String rPesan   = r.length >= 2 ? r[1] : "";
                String rBalasan = r.length >= 3 ? r[2] : "";
                String rWaktu   = r.length >= 4 ? r[3] : "";
                String rTag     = r.length >= 5 ? r[4] : "umum";
                HBox item = buildRiwayatItem(rId, rPesan, rBalasan, rWaktu, rTag);
                itemsBox.getChildren().add(item);
            }
            headerBox.getChildren().add(itemsBox);
        }

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        VBox bubble = new VBox(5, headerBox, time);
        bubble.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    /** Buat satu baris item riwayat di dalam kartu showRiwayat */
    private HBox buildRiwayatItem(String id, String pesan, String balasan,
                                   String waktu, String tag) {
        // Badge warna
        String badgeBg, badgeFg, tagLabel, iconStr;
        switch (tag) {
            case "produk"  -> { badgeBg="#EEEDFE"; badgeFg="#3C3489"; tagLabel="Produk";   iconStr="🛍️"; }
            case "info"    -> { badgeBg="#EAF3DE"; badgeFg="#3B6D11"; tagLabel="Info";     iconStr="🏪"; }
            case "bantuan" -> { badgeBg="#E6F1FB"; badgeFg="#0C447C"; tagLabel="Bantuan";  iconStr="❓"; }
            default        -> { badgeBg="#F1EFE8"; badgeFg="#5F5E5A"; tagLabel="Umum";     iconStr="💬"; }
        }

        // Icon kategori
        Label iconLbl = new Label(iconStr);
        iconLbl.setStyle("-fx-font-size:14px;");
        StackPane iconPane = new StackPane(iconLbl);
        iconPane.setMinSize(30, 30);
        iconPane.setMaxSize(30, 30);
        iconPane.setStyle("-fx-background-color:" + badgeBg + "; -fx-background-radius:8;");

        // Konten tengah
        Label pesanLbl = new Label(pesan);
        pesanLbl.setStyle("-fx-font-weight:bold; -fx-font-size:12px; -fx-text-fill:#1a1a2e;");
        pesanLbl.setWrapText(true);
        pesanLbl.setMaxWidth(280);

        String balasanShort = balasan.length() > 55 ? balasan.substring(0, 55) + "…" : balasan;
        Label balasanLbl = new Label(balasanShort);
        balasanLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#888;");

        VBox content = new VBox(2, pesanLbl, balasanLbl);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Waktu + badge
        Label waktuLbl = new Label(waktu != null && waktu.length() >= 5 ? waktu.substring(11, 16) : waktu);
        waktuLbl.setStyle("-fx-font-size:10px; -fx-text-fill:#bbb;");

        VBox rightBox = new VBox(3, waktuLbl);
        rightBox.setAlignment(Pos.TOP_RIGHT);

        HBox item = new HBox(10, iconPane, content, rightBox);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(9, 10, 9, 10));

        String sNormal = "-fx-background-color:#fafafa; -fx-border-color:#efefef; -fx-border-width:1; -fx-border-radius:10; -fx-background-radius:10; -fx-cursor:hand;";
        String sHover  = "-fx-background-color:#f0eeff; -fx-border-color:#4B3FC8; -fx-border-width:1.5; -fx-border-radius:10; -fx-background-radius:10; -fx-cursor:hand;";
        item.setStyle(sNormal);
        item.setOnMouseEntered(e -> item.setStyle(sHover));
        item.setOnMouseExited(e -> item.setStyle(sNormal));
        item.setOnMouseClicked(e -> {
            inputField.setText(pesan);
            prosesPesan();
        });

        return item;
    }

    /** Refresh daftar riwayat mini di sidebar (5 terakhir) */
    private void refreshSidebarRiwayat() {
        if (sidebarRiwayatContainer == null) return;
        sidebarRiwayatContainer.getChildren().clear();

        List<String[]> riwayat = db.getRiwayatTerakhir(5);
        if (riwayat.isEmpty()) {
            Label empty = new Label("Belum ada riwayat");
            empty.setStyle("-fx-font-size:11px; -fx-text-fill:#bbb; -fx-padding:4 8;");
            sidebarRiwayatContainer.getChildren().add(empty);
            return;
        }

        for (String[] r : riwayat) {
            if (r.length < 2) continue;
            String pesan = r[1];
            String tag   = r.length >= 5 ? r[4] : "umum";

            String iconStr;
            String iconBg;
            switch (tag) {
                case "produk"  -> { iconStr="🛍️"; iconBg="#EEEDFE"; }
                case "bantuan" -> { iconStr="❓"; iconBg="#E6F1FB"; }
                case "info"    -> { iconStr="🏪"; iconBg="#EAF3DE"; }
                default        -> { iconStr="💬"; iconBg="#F1EFE8"; }
            }

            Label iconLbl = new Label(iconStr);
            iconLbl.setStyle("-fx-font-size:11px;");
            StackPane iconPane = new StackPane(iconLbl);
            iconPane.setMinSize(24, 24);
            iconPane.setMaxSize(24, 24);
            iconPane.setStyle("-fx-background-color:" + iconBg + "; -fx-background-radius:6;");

            String short_pesan = pesan.length() > 26 ? pesan.substring(0, 26) + "…" : pesan;
            Label pesanLbl = new Label(short_pesan);
            pesanLbl.setStyle("-fx-font-size:11.5px; -fx-text-fill:#444;");

            HBox btn = new HBox(8, iconPane, pesanLbl);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPadding(new Insets(7, 8, 7, 8));

            String sN = "-fx-background-color:transparent; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand;";
            String sH = "-fx-background-color:#f0eeff; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand;";
            btn.setStyle(sN);
            btn.setOnMouseEntered(e -> btn.setStyle(sH));
            btn.setOnMouseExited(e -> btn.setStyle(sN));
            btn.setOnMouseClicked(e -> {
                inputField.setText(pesan);
                prosesPesan();
            });

            sidebarRiwayatContainer.getChildren().add(btn);
        }
    }

    /** Hapus semua riwayat */
    @FXML
    public void hapusRiwayat() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus semua riwayat percakapan?",
                ButtonType.YES, ButtonType.NO);
        konfirmasi.setHeaderText(null);
        konfirmasi.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                db.hapusSemuaRiwayat();
                chatContainer.getChildren().clear();
                showWelcomeScreen();
                refreshSidebarRiwayat();
            }
        });
    }

    private void simpanRiwayat(String pesan, String balasan, String tag) {
        db.tambahRiwayat(pesan, balasan, tag);
    }

    private String deteksiTag(String pesan, String jawaban) {
        String p = pesan.toLowerCase();
        if (p.contains("bantuan") || p.contains("help") || p.contains("bisa apa")
                || p.contains("cara") || p.contains("panduan")) return "bantuan";
        if (p.contains("jam") || p.contains("buka") || p.contains("tutup")
                || p.contains("alamat") || p.contains("lokasi") || p.contains("toko")) return "info";
        return "umum";
    }

    // ═══════════════════════════════════════════════════════════════
    //  PANEL BANTUAN — tampilan baru dengan kartu terstruktur
    // ═══════════════════════════════════════════════════════════════

    @FXML
    public void showBantuan() {
        VBox mainCard = new VBox(0);
        mainCard.setMaxWidth(480);
        mainCard.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#e8e8f0; -fx-border-width:1;" +
            "-fx-border-radius:14; -fx-background-radius:14;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"
        );

        // ── Hero header ──
        HBox hero = new HBox(12);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(14, 16, 14, 16));
        hero.setStyle("-fx-background-color:linear-gradient(to right,#4B3FC8,#7c3aed); -fx-background-radius:13 13 0 0;");

        StackPane heroIcon = new StackPane();
        heroIcon.setMinSize(38, 38);
        heroIcon.setMaxSize(38, 38);
        heroIcon.setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-background-radius:10;");
        Label heroIconLbl = new Label("📖");
        heroIconLbl.setStyle("-fx-font-size:18px;");
        heroIcon.getChildren().add(heroIconLbl);

        VBox heroText = new VBox(2);
        Label heroTitle = new Label("Panduan Penggunaan SahabatLaris");
        heroTitle.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:white;");
        Label heroSub = new Label("Klik topik untuk melihat detail");
        heroSub.setStyle("-fx-font-size:11px; -fx-text-fill:rgba(255,255,255,0.75);");
        heroText.getChildren().addAll(heroTitle, heroSub);
        hero.getChildren().addAll(heroIcon, heroText);

        // ── Accordion sections ──
        VBox accordion = new VBox(0);
        accordion.setPadding(new Insets(10, 12, 12, 12));
        accordion.setSpacing(6);

        accordion.getChildren().addAll(
            buildAccordionItem("🔍", "#EEEDFE", "Mencari Produk",
                "Ketik kategori: toner · serum · pelembab · sunscreen\n" +
                "Atau langsung nama produk, contoh: \"serum vitamin c\""),
            buildAccordionItem("💡", "#FEF3C7", "Rekomendasi Jenis Kulit",
                "Ketik: \"produk untuk kulit sensitif\"\n" +
                "Atau: \"rekomendasi kulit berminyak\" / \"kulit berjerawat\""),
            buildAccordionItem("💰", "#D1FAE5", "Cek Harga",
                "Ketik: \"harga toner berapa?\" atau \"moisturizer paling murah\"\n" +
                "Atau ketik \"semua produk\" untuk lihat daftar lengkap"),
            buildAccordionItem("📋", "#E0F2FE", "Riwayat Chat",
                "Klik tombol \"Riwayat Chat\" di kanan atas\n" +
                "Klik item riwayat untuk mengulangi pertanyaan otomatis")
        );

        mainCard.getChildren().addAll(hero, accordion);

        Label time = new Label(LocalTime.now().format(TIME_FMT));
        time.getStyleClass().add("timestamp");

        VBox bubble = new VBox(5, mainCard, time);
        bubble.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        chatContainer.getChildren().add(row);
        scrollToBottom();
    }

    /** Accordion item: default TUTUP, klik header untuk toggle */
    private VBox buildAccordionItem(String icon, String iconBg,
                                     String title, String content) {
        Label contentLbl = new Label(content);
        contentLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#555; -fx-line-spacing:3;");
        contentLbl.setWrapText(true);
        contentLbl.setMaxWidth(420);

        VBox body = new VBox(contentLbl);
        body.setPadding(new Insets(0, 12, 12, 44));
        // Default: BUKA
        body.setVisible(true);
        body.setManaged(true);

        StackPane iconPane = new StackPane();
        iconPane.setMinSize(26, 26);
        iconPane.setMaxSize(26, 26);
        iconPane.setStyle("-fx-background-color:" + iconBg + "; -fx-background-radius:7;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:12px;");
        iconPane.getChildren().add(iconLbl);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight:bold; -fx-font-size:12.5px; -fx-text-fill:#1a1a2e;");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        // Chevron default ▾ (terbuka)
        Label chevron = new Label("▾");
        chevron.setStyle("-fx-font-size:11px; -fx-text-fill:#4B3FC8;");

        HBox header = new HBox(10, iconPane, titleLbl, chevron);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(11, 12, 11, 12));

        String hNormal = "-fx-background-color:#fafafa; -fx-background-radius:8; -fx-cursor:hand;";
        String hOpen   = "-fx-background-color:#f0eeff; -fx-background-radius:8 8 0 0; -fx-cursor:hand;";
        header.setStyle(hOpen);
        header.setOnMouseEntered(e -> {
            if (!body.isVisible()) header.setStyle(hNormal.replace("#fafafa","#f0eeff"));
        });
        header.setOnMouseExited(e -> {
            header.setStyle(body.isVisible() ? hOpen : hNormal);
        });

        header.setOnMouseClicked(e -> {
            boolean wasOpen = body.isVisible();
            if (wasOpen) {
                // Tutup
                body.setVisible(false);
                body.setManaged(false);
                chevron.setText("▸");
                chevron.setStyle("-fx-font-size:11px; -fx-text-fill:#bbb;");
                header.setStyle(hNormal);
            } else {
                // Buka
                body.setVisible(true);
                body.setManaged(true);
                chevron.setText("▾");
                chevron.setStyle("-fx-font-size:11px; -fx-text-fill:#4B3FC8;");
                header.setStyle(hOpen);
            }
        });

        VBox card = new VBox(0, header, body);
        card.setStyle(
            "-fx-border-color:#efefef; -fx-border-width:1;" +
            "-fx-border-radius:9; -fx-background-radius:9;" +
            "-fx-background-color:white;"
        );
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  RENDER BUBBLE CHAT
    // ═══════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════
    //  KARTU PRODUK
    // ═══════════════════════════════════════════════════════════════

    private void addBotProductCards(List<Produk> produkList) {
        for (Produk prod : produkList) {
            StackPane thumbPane = new StackPane();
            thumbPane.setMinSize(120, 120);
            thumbPane.setMaxSize(120, 120);
            thumbPane.setStyle("-fx-background-color:#f0eeff; -fx-background-radius:12;");

            String imgUrl = prod.getGambarUrl();
            boolean imgLoaded = false;

            if (imgUrl != null && !imgUrl.isBlank()) {
                try {
                    String resolvedUrl;
                    if (imgUrl.startsWith("http://") || imgUrl.startsWith("https://")
                            || imgUrl.startsWith("file:")) {
                        resolvedUrl = imgUrl;
                    } else {
                        File imgFile = new File(imgUrl);
                        resolvedUrl = imgFile.getAbsoluteFile().toURI().toString();
                    }
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
                Label icon = new Label("🖼️");
                icon.setStyle("-fx-font-size:30px;");
                Label noImgTxt = new Label("No Image");
                noImgTxt.setStyle("-fx-text-fill:#bbb; -fx-font-size:10px;");
                VBox ph = new VBox(4, icon, noImgTxt);
                ph.setAlignment(Pos.CENTER);
                thumbPane.getChildren().add(ph);
            }

            Label nameLbl = new Label(prod.getNamaProduk());
            nameLbl.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:#1a1a2e;");
            nameLbl.setWrapText(true);

            Label katLbl = new Label("↔️ " + prod.getKategori());
            katLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#888;");

            Label hargaLbl = new Label(prod.getHargaFormatted());
            hargaLbl.setStyle("-fx-font-weight:bold; -fx-font-size:15px; -fx-text-fill:#4B3FC8;");

            Label kandLbl = new Label("✔ " + prod.getKandungan());
            kandLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#555;");
            kandLbl.setWrapText(true);

            String jenisKulit = prod.getJenisKulit() != null ? prod.getJenisKulit() : "Semua Jenis Kulit";
            Label badgeKulit = new Label("🌿 " + jenisKulit);
            badgeKulit.setStyle("-fx-font-size:10px; -fx-text-fill:#276a3f; -fx-background-color:#e6f4ea; -fx-background-radius:20; -fx-padding:3 10 3 10;");

            HBox badgeRow = new HBox(badgeKulit);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            VBox infoBox = new VBox(6, nameLbl, katLbl, hargaLbl, kandLbl, badgeRow);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            infoBox.setPadding(new Insets(2, 0, 2, 0));
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            HBox card = new HBox(14, thumbPane, infoBox);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(14));
            card.setMaxWidth(500);

            String sNormal = "-fx-background-color:white; -fx-border-color:#e8e8f0; -fx-border-width:1; -fx-border-radius:14; -fx-background-radius:14; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);";
            String sHover  = "-fx-background-color:#f8f7ff; -fx-border-color:#4B3FC8; -fx-border-width:1.5; -fx-border-radius:14; -fx-background-radius:14; -fx-effect:dropshadow(gaussian,rgba(75,63,200,0.15),10,0,0,3);";

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

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    private HBox createTypingIndicator() {
        Label dots = new Label("•••");
        dots.setStyle("-fx-text-fill:#4B3FC8; -fx-font-size:18px;");
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
}
