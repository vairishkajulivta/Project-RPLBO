package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.ui.Main;
import org.sahabatlaris.chatbot.service.ManagedDataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.DirectoryChooser;

public class AdminController {

    @FXML private TableView<Produk> tabelProduk;
    @FXML private TableColumn<Produk, String> colNo;
    @FXML private TableColumn<Produk, String> colNama;
    @FXML private TableColumn<Produk, String> colKategori;
    @FXML private TableColumn<Produk, String> colHarga;
    @FXML private TableColumn<Produk, String> colKandungan;
    @FXML private TableColumn<Produk, String> colJenisKulit;
    @FXML private TableColumn<Produk, String> colAreaTubuh;
    @FXML private TableColumn<Produk, String> colStatus;
    @FXML private TableColumn<Produk, Void>   colAksi;
    @FXML private Label                        labelJumlahProduk;
    @FXML private ComboBox<String>             filterKategori;
    @FXML private VBox                         panelDataProduk;
    @FXML private VBox                         panelInfoToko;
    @FXML private Button                       btnDataProduk;
    @FXML private Button                       btnInfoToko;

    @FXML private TextField  namaToko;
    @FXML private TextField  taglineToko;
    @FXML private TextArea   deskripsiToko;
    @FXML private TextArea   alamatToko;
    @FXML private TextField  linkPeta;
    @FXML private TextField  kotaToko;
    @FXML private VBox       jamOperasionalContainer;

    private ManagedDataService        layananData      = new ManagedDataService();
    private ObservableList<Produk>    produkObservable = FXCollections.observableArrayList();

    private static final String IMAGE_DIR = "images/produk/";

    private static String resolveImagePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) return "";
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://")
                || rawPath.startsWith("file:")) return rawPath;
        File f = new File(rawPath);
        if (f.isAbsolute()) return f.toURI().toString();
        File abs = new File(System.getProperty("user.dir"), rawPath);
        if (abs.exists()) return abs.toURI().toString();
        return rawPath;
    }

    @FXML
    public void initialize() {
        ensureImageDir();
        setupTable();
        setupFilterKategori();
        loadInfoToko();
        setupJamOperasional();
        tampilkanPanel();
    }

    private void ensureImageDir() {
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private void setupTable() {
        colNo.setCellValueFactory(cellData -> {
            int idx = tabelProduk.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(idx));
        });

        colNama.setCellValueFactory(d -> d.getValue().namaProdukProperty());
        colKategori.setCellValueFactory(d -> d.getValue().kategoriProperty());
        colHarga.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getHargaFormatted()));
        colKandungan.setCellValueFactory(d -> d.getValue().kandunganProperty());
        colJenisKulit.setCellValueFactory(d -> d.getValue().jenisKulitProperty());
        colAreaTubuh.setCellValueFactory(d -> d.getValue().areaTubuhProperty());

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Produk p = getTableView().getItems().get(getIndex());
                Label badge = new Label(p.isAktif() ? "Aktif" : "Non-Aktif");
                badge.getStyleClass().add("status-aktif");
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnUbah  = new Button("Ubah");
            private final Button btnHapus = new Button("Hapus");
            private final HBox   box      = new HBox(6, btnUbah, btnHapus);

            {
                box.setAlignment(Pos.CENTER);
                btnUbah.getStyleClass().add("btn-ubah");
                btnHapus.getStyleClass().add("btn-hapus");

                btnUbah.setOnAction(e -> {
                    Produk p = getTableView().getItems().get(getIndex());
                    showFormProduk(p);
                });
                btnHapus.setOnAction(e -> {
                    Produk p = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Hapus produk \"" + p.getNamaProduk() + "\"?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            layananData.hapusProduk(p.getKodeProduk());
                            tampilkanPanel();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tabelProduk.setItems(produkObservable);
    }

    private void setupFilterKategori() {
        filterKategori.getItems().addAll(layananData.getAllKategori());
        filterKategori.setValue("Semua Kategori");
        filterKategori.setOnAction(e -> tampilkanPanel());
    }

    private void loadInfoToko() {
        String[] info = layananData.getInfoToko();
        if (namaToko    != null) namaToko.setText(info[0]);
        if (taglineToko != null) taglineToko.setText(info[1]);
        if (deskripsiToko != null) deskripsiToko.setText(info[2]);
        if (alamatToko  != null) alamatToko.setText(info[3]);
        if (kotaToko    != null) kotaToko.setText(info[4]);
        if (linkPeta    != null) linkPeta.setText(info[6]);
    }

    private void setupJamOperasional() {
        if (jamOperasionalContainer == null) return;
        jamOperasionalContainer.getChildren().clear();

        List<String[]> jamList = layananData.getJamOperasional();
        for (String[] jam : jamList) {
            String hari    = jam[0];
            boolean buka   = "1".equals(jam[1]);
            String jamBuka  = jam[2];
            String jamTutup = jam[3];

            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);

            Label lblHari = new Label(hari);
            lblHari.setMinWidth(70);
            lblHari.getStyleClass().add("field-label-sm");

            ToggleButton toggleBuka = new ToggleButton(buka ? "Buka" : "Tutup");
            toggleBuka.setSelected(buka);
            String styleOn  = "-fx-background-color:#4B3FC8;-fx-text-fill:white;-fx-background-radius:12;-fx-padding:4 14;-fx-cursor:hand;-fx-font-size:12px;";
            String styleOff = "-fx-background-color:#ccc;-fx-text-fill:#555;-fx-background-radius:12;-fx-padding:4 14;-fx-cursor:hand;-fx-font-size:12px;";
            toggleBuka.setStyle(buka ? styleOn : styleOff);

            TextField tfBuka  = new TextField(jamBuka);  tfBuka.setPrefWidth(70);  tfBuka.getStyleClass().add("info-field");  tfBuka.setDisable(!buka);
            TextField tfTutup = new TextField(jamTutup); tfTutup.setPrefWidth(70); tfTutup.getStyleClass().add("info-field"); tfTutup.setDisable(!buka);
            Label lblSampai = new Label("-");

            toggleBuka.setOnAction(e -> {
                boolean isOpen = toggleBuka.isSelected();
                toggleBuka.setText(isOpen ? "Buka" : "Tutup");
                toggleBuka.setStyle(isOpen ? styleOn : styleOff);
                tfBuka.setDisable(!isOpen);
                tfTutup.setDisable(!isOpen);
                layananData.simpanJamOperasional(hari, isOpen ? 1 : 0, tfBuka.getText(), tfTutup.getText());
            });

            tfBuka.textProperty().addListener((obs, o, n) ->
                    layananData.simpanJamOperasional(hari, toggleBuka.isSelected() ? 1 : 0, n, tfTutup.getText()));
            tfTutup.textProperty().addListener((obs, o, n) ->
                    layananData.simpanJamOperasional(hari, toggleBuka.isSelected() ? 1 : 0, tfBuka.getText(), n));

            row.getChildren().addAll(lblHari, toggleBuka, tfBuka, lblSampai, tfTutup);
            jamOperasionalContainer.getChildren().add(row);
        }
    }

    @FXML public void showDataProduk() {
        tampilkanPanel();
        panelDataProduk.setVisible(true);
        panelInfoToko.setVisible(false);
        setSidebarActive(btnDataProduk, btnInfoToko);
    }

    @FXML public void showInfoToko() {
        panelDataProduk.setVisible(false);
        panelInfoToko.setVisible(true);
        setSidebarActive(btnInfoToko, btnDataProduk);
    }

    private void setSidebarActive(Button active, Button inactive) {
        active.getStyleClass().remove("sidebar-btn");
        if (!active.getStyleClass().contains("sidebar-btn-active"))
            active.getStyleClass().add("sidebar-btn-active");
        inactive.getStyleClass().remove("sidebar-btn-active");
        if (!inactive.getStyleClass().contains("sidebar-btn"))
            inactive.getStyleClass().add("sidebar-btn");
    }

    @FXML public void showTambahProduk() { showFormProduk(null); }

    @FXML
    public void importGambarMassal() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Pilih Folder Berisi Gambar Produk");
        Stage owner = (Stage) tabelProduk.getScene().getWindow();
        File folder = dc.showDialog(owner);
        if (folder == null) return;

        File[] files = folder.listFiles(f -> {
            String n = f.getName().toLowerCase();
            return f.isFile() && (n.endsWith(".jpg") || n.endsWith(".jpeg")
                    || n.endsWith(".png") || n.endsWith(".gif") || n.endsWith(".webp"));
        });

        if (files == null || files.length == 0) {
            new Alert(Alert.AlertType.WARNING,
                    "Tidak ada file gambar (jpg/png/gif/webp) di folder tersebut.",
                    ButtonType.OK).showAndWait();
            return;
        }

        ensureImageDir();
        List<Produk> semuaProduk = layananData.getAllProduk();

        int cocok = 0, gagal = 0;
        List<String> logCocok = new ArrayList<>();
        List<String> logGagal = new ArrayList<>();

        for (File imgFile : files) {
            String namaFile = stripExtension(imgFile.getName()).toLowerCase()
                    .replaceAll("[^a-z0-9]", ""); // hanya huruf & angka

            Produk target = null;
            int bestScore = -1;

            for (Produk p : semuaProduk) {
                String namaProduk = p.getNamaProduk().toLowerCase()
                        .replaceAll("[^a-z0-9]", "");
                int score = 0;
                if (namaProduk.equals(namaFile))          score = 100;
                else if (namaProduk.contains(namaFile))   score = 60;
                else if (namaFile.contains(namaProduk))   score = 40;
                else if (namaProduk.startsWith(namaFile)
                        || namaFile.startsWith(namaProduk)) score = 30;

                if (score > bestScore) { bestScore = score; target = p; }
            }

            if (target != null && bestScore >= 30) {
                try {
                    String dest = IMAGE_DIR + sanitizeFileName(imgFile.getName());
                    Files.copy(imgFile.toPath(), Paths.get(dest),
                            StandardCopyOption.REPLACE_EXISTING);
                    target.setGambarUrl(dest);
                    layananData.updateProduk(target);
                    logCocok.add("✅ " + imgFile.getName() + "  →  " + target.getNamaProduk());
                    cocok++;
                } catch (IOException ex) {
                    logGagal.add("❌ " + imgFile.getName() + " (gagal salin: " + ex.getMessage() + ")");
                    gagal++;
                }
            } else {
                logGagal.add("⚠️ " + imgFile.getName() + " (tidak cocok dengan produk manapun)");
                gagal++;
            }
        }

        tampilkanPanel();

        Stage summary = new Stage();
        summary.initModality(Modality.APPLICATION_MODAL);
        summary.setTitle("Hasil Import Gambar Massal");

        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color:white;");

        Label judul = new Label("Import Gambar Massal Selesai");
        judul.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        Label stat = new Label("✅ Berhasil: " + cocok + "   ⚠️ Gagal/Tidak cocok: " + gagal);
        stat.setStyle("-fx-font-size:13px;-fx-text-fill:#444;");

        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(300);
        log.setStyle("-fx-font-size:12px;-fx-font-family:monospace;");
        StringBuilder sb = new StringBuilder();
        for (String s : logCocok) sb.append(s).append("\n");
        if (!logGagal.isEmpty()) {
            sb.append("\n--- Tidak Cocok / Gagal ---\n");
            for (String s : logGagal) sb.append(s).append("\n");
        }
        log.setText(sb.toString());

        Label hint = new Label("💡 Tips: Beri nama file gambar sesuai nama produk agar cocok otomatis.\nContoh: \"Somethinc Niacinamide 10% Serum.jpg\"");
        hint.setStyle("-fx-font-size:11px;-fx-text-fill:#888;-fx-wrap-text:true;");
        hint.setWrapText(true);

        Button btnOk = new Button("OK");
        btnOk.setStyle("-fx-background-color:#4B3FC8;-fx-text-fill:white;-fx-padding:7 24;-fx-background-radius:6;-fx-cursor:hand;");
        btnOk.setOnAction(e -> summary.close());

        vbox.getChildren().addAll(judul, stat, log, hint, btnOk);
        summary.setScene(new Scene(vbox, 520, 450));
        summary.showAndWait();
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    @FXML
    public void simpanInfoToko() {
        layananData.simpanInfoToko(
                namaToko     != null ? namaToko.getText()     : "",
                taglineToko  != null ? taglineToko.getText()  : "",
                deskripsiToko != null ? deskripsiToko.getText() : "",
                alamatToko   != null ? alamatToko.getText()   : "",
                kotaToko     != null ? kotaToko.getText()     : "",
                "",
                linkPeta     != null ? linkPeta.getText()     : ""
        );
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Info toko berhasil disimpan!", ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) tabelProduk.getScene().getWindow();
            new Main().showAdminLogin(stage);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void tampilkanPanel() {
        produkObservable.clear();
        String kat = filterKategori != null ? filterKategori.getValue() : "Semua Kategori";
        List<Produk> list = (kat == null || kat.equals("Semua Kategori"))
                ? layananData.getAllProduk()
                : layananData.getProdukByKategori(kat);
        produkObservable.addAll(list);
        if (labelJumlahProduk != null)
            labelJumlahProduk.setText(String.valueOf(list.size()));
    }

    private void showFormProduk(Produk existingProduk) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existingProduk == null ? "Tambah Produk" : "Ubah Produk");

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label titleLbl = new Label(existingProduk == null ? "Tambah Produk Baru" : "Ubah Produk");
        titleLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        TextField fNama = new TextField();
        fNama.setPromptText("Nama Produk");
        fNama.getStyleClass().add("info-field");

        ComboBox<String> fKategori = new ComboBox<>();
        fKategori.getItems().addAll(
                "Pelembab", "Toner", "Serum", "Facial Wash",
                "Sunscreen", "Exfoliator", "Body Care", "Eye Care",
                "Lip Care", "Hair Care", "Acne Care", "Hand Care");
        fKategori.setPromptText("Pilih Kategori");
        fKategori.setMaxWidth(Double.MAX_VALUE);

        TextField fHarga = new TextField();
        fHarga.setPromptText("Harga (angka)");
        fHarga.getStyleClass().add("info-field");

        TextArea fKandungan = new TextArea();
        fKandungan.setPromptText("Kandungan (pisah koma)");
        fKandungan.setPrefRowCount(2);
        fKandungan.setWrapText(true);
        fKandungan.getStyleClass().add("info-textarea");

        TextArea fDeskripsi = new TextArea();
        fDeskripsi.setPromptText("Deskripsi produk (opsional)...");
        fDeskripsi.setPrefRowCount(2);
        fDeskripsi.setWrapText(true);
        fDeskripsi.getStyleClass().add("info-textarea");

        ComboBox<String> fJenisKulit = new ComboBox<>();
        fJenisKulit.getItems().addAll("Semua Jenis Kulit","Kulit Sensitif",
                "Kulit Berminyak","Kulit Kering","Kulit Normal",
                "Kulit Berjerawat","Kulit Menua");
        fJenisKulit.setPromptText("Pilih Jenis Kulit");
        fJenisKulit.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> fAreaTubuh = new ComboBox<>();
        fAreaTubuh.getItems().addAll("Muka","Badan","Rambut","Tangan & Kaki","Bibir","Mata");
        fAreaTubuh.setPromptText("Pilih Area Tubuh");
        fAreaTubuh.setMaxWidth(Double.MAX_VALUE);

        CheckBox fAktif = new CheckBox("Aktif");
        fAktif.setSelected(true);

        final String[] imagePath = {""};

        StackPane previewPane = new StackPane();
        previewPane.setMinSize(110, 110);
        previewPane.setMaxSize(110, 110);
        previewPane.setStyle(
                "-fx-background-color:#f0eeff;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:#c5c0ee;" +
                        "-fx-border-radius:10;" +
                        "-fx-border-style:dashed;");

        Label noImgLbl = new Label("Belum ada\ngambar");
        noImgLbl.setStyle("-fx-text-fill:#9990dd;-fx-font-size:11px;-fx-text-alignment:center;");
        noImgLbl.setWrapText(true);
        noImgLbl.setAlignment(Pos.CENTER);

        ImageView previewImg = new ImageView();
        previewImg.setFitWidth(110);
        previewImg.setFitHeight(110);
        previewImg.setPreserveRatio(true);
        Rectangle clip = new Rectangle(110, 110);
        clip.setArcWidth(16); clip.setArcHeight(16);
        previewImg.setClip(clip);
        previewImg.setVisible(false);

        previewPane.getChildren().addAll(noImgLbl, previewImg);

        Runnable updatePreview = () -> {
            String p = imagePath[0];
            if (p == null || p.isBlank()) {
                previewImg.setVisible(false);
                noImgLbl.setVisible(true);
                return;
            }
            try {
                String url;
                if (p.startsWith("http://") || p.startsWith("https://") || p.startsWith("file:")) {
                    url = p;
                } else {
                    File f = new File(p);
                    url = f.exists() ? f.toURI().toString() : p;
                }
                Image img = new Image(url, 110, 110, true, true, false);
                if (!img.isError()) {
                    previewImg.setImage(img);
                    previewImg.setVisible(true);
                    noImgLbl.setVisible(false);
                } else {
                    previewImg.setVisible(false);
                    noImgLbl.setVisible(true);
                }
            } catch (Exception ex) {
                previewImg.setVisible(false);
                noImgLbl.setVisible(true);
            }
        };

        Label fileNameLbl = new Label("Tidak ada gambar dipilih");
        fileNameLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#888;-fx-wrap-text:true;");
        fileNameLbl.setWrapText(true);
        fileNameLbl.setMaxWidth(250);

        TextField urlField = new TextField();
        urlField.setPromptText("https://example.com/gambar.jpg  atau  path/lokal/gambar.jpg");
        urlField.getStyleClass().add("info-field");
        urlField.setMaxWidth(Double.MAX_VALUE);

        Button btnUpload = new Button("📁  Pilih dari Komputer...");
        btnUpload.setStyle(
                "-fx-background-color:#4B3FC8;-fx-text-fill:white;" +
                        "-fx-font-size:12px;-fx-padding:7 14;" +
                        "-fx-background-radius:6;-fx-cursor:hand;");

        Button btnHapusGambar = new Button("✕ Hapus");
        btnHapusGambar.setStyle(
                "-fx-background-color:#e53e3e;-fx-text-fill:white;" +
                        "-fx-font-size:11px;-fx-padding:6 10;" +
                        "-fx-background-radius:6;-fx-cursor:hand;");

        HBox uploadRow = new HBox(8, btnUpload, btnHapusGambar);
        uploadRow.setAlignment(Pos.CENTER_LEFT);

        VBox uploadTab = new VBox(8, uploadRow, fileNameLbl);
        uploadTab.setPadding(new Insets(10, 0, 0, 0));

        btnUpload.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Pilih Gambar Produk");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("File Gambar", "*.png","*.jpg","*.jpeg","*.gif","*.webp"),
                    new FileChooser.ExtensionFilter("Semua File", "*.*")
            );
            File chosen = fc.showOpenDialog(dialog);
            if (chosen != null) {
                try {
                    String dest = IMAGE_DIR + sanitizeFileName(chosen.getName());
                    Files.copy(chosen.toPath(), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
                    imagePath[0] = dest;
                    fileNameLbl.setText("📁 " + chosen.getName());
                    urlField.setText(""); // kosongkan URL jika ada
                    updatePreview.run();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Gagal menyalin gambar:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
                }
            }
        });

        btnHapusGambar.setOnAction(e -> {
            imagePath[0] = "";
            fileNameLbl.setText("Tidak ada gambar dipilih");
            urlField.setText("");
            updatePreview.run();
        });

        Button btnLoadUrl = new Button("🔍 Muat Gambar");
        btnLoadUrl.setStyle(
                "-fx-background-color:#1D9E75;-fx-text-fill:white;" +
                        "-fx-font-size:12px;-fx-padding:7 14;" +
                        "-fx-background-radius:6;-fx-cursor:hand;");

        Label urlInfoLbl = new Label("Masukkan URL gambar dari internet atau path file di komputer.");
        urlInfoLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#888;-fx-wrap-text:true;");
        urlInfoLbl.setWrapText(true);

        HBox urlBtnRow = new HBox(8, btnLoadUrl);
        urlBtnRow.setAlignment(Pos.CENTER_LEFT);

        VBox urlTab = new VBox(8, urlField, urlBtnRow, urlInfoLbl);
        urlTab.setPadding(new Insets(10, 0, 0, 0));

        btnLoadUrl.setOnAction(e -> {
            String url = urlField.getText().trim();
            if (url.isEmpty()) return;
            imagePath[0] = url;
            fileNameLbl.setText("🌐 " + (url.length() > 45 ? url.substring(0, 45) + "…" : url));
            updatePreview.run();
        });

        urlField.setOnAction(e -> btnLoadUrl.fire());

        VBox metodBox = new VBox(0);

        Label lblUploadHeader = new Label("Upload File");
        lblUploadHeader.setStyle(
                "-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#4B3FC8;" +
                        "-fx-background-color:#f0eeff;-fx-background-radius:6;" +
                        "-fx-padding:4 10;");

        Separator sepGambar = new Separator();
        sepGambar.setPadding(new Insets(6, 0, 6, 0));

        Label lblUrlHeader = new Label("Atau Gunakan URL Gambar");
        lblUrlHeader.setStyle(
                "-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#1D9E75;" +
                        "-fx-background-color:#e8f8f3;-fx-background-radius:6;" +
                        "-fx-padding:4 10;");

        metodBox.getChildren().addAll(lblUploadHeader, uploadTab, sepGambar, lblUrlHeader, urlTab);
        metodBox.setPadding(new Insets(0, 0, 0, 14));
        HBox.setHgrow(metodBox, Priority.ALWAYS);

        HBox gambarRow = new HBox(12, previewPane, metodBox);
        gambarRow.setAlignment(Pos.TOP_LEFT);
        gambarRow.setPadding(new Insets(2, 0, 2, 0));
        gambarRow.setStyle(
                "-fx-background-color:#fafafa;" +
                        "-fx-border-color:#e8e8f0;-fx-border-width:1;" +
                        "-fx-border-radius:10;-fx-background-radius:10;" +
                        "-fx-padding:12;");

        if (existingProduk != null) {
            fNama.setText(existingProduk.getNamaProduk());
            fKategori.setValue(existingProduk.getKategori());
            fHarga.setText(String.valueOf(existingProduk.getHarga()));
            fKandungan.setText(existingProduk.getKandungan());
            fJenisKulit.setValue(existingProduk.getJenisKulit());
            fAreaTubuh.setValue(existingProduk.getAreaTubuh() != null ? existingProduk.getAreaTubuh() : "Muka");
            fAktif.setSelected(existingProduk.isAktif());
            fDeskripsi.setText(existingProduk.getDeskripsi() != null ? existingProduk.getDeskripsi() : "");
            String existingImg = existingProduk.getGambarUrl();
            if (existingImg != null && !existingImg.isBlank()) {
                imagePath[0] = existingImg;
                if (existingImg.startsWith("http://") || existingImg.startsWith("https://")) {
                    urlField.setText(existingImg);
                    fileNameLbl.setText("🌐 " + (existingImg.length() > 45
                            ? existingImg.substring(0, 45) + "…" : existingImg));
                } else {
                    fileNameLbl.setText("📁 " + new File(existingImg).getName());
                }
                updatePreview.run();
            }
        }

        Label errMsg = new Label();
        errMsg.setStyle("-fx-text-fill:#e53e3e;-fx-font-size:12px;");

        Button btnBatal  = new Button("Batal");
        Button btnSimpan = new Button("Simpan Produk");
        btnBatal.setStyle("-fx-background-color:#888;-fx-text-fill:white;-fx-padding:7 18;-fx-background-radius:6;-fx-cursor:hand;");
        btnSimpan.getStyleClass().add("btn-tambah");
        btnSimpan.setPadding(new Insets(7, 18, 7, 18));

        HBox btnRow = new HBox(12, btnBatal, btnSimpan);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        btnBatal.setOnAction(e -> dialog.close());

        btnSimpan.setOnAction(e -> {
            if (fNama.getText().isEmpty() || fKategori.getValue() == null || fHarga.getText().isEmpty()) {
                errMsg.setText("Nama, Kategori, dan Harga wajib diisi.");
                return;
            }
            long harga;
            try { harga = Long.parseLong(fHarga.getText()); }
            catch (NumberFormatException ex) { errMsg.setText("Harga harus berupa angka."); return; }

            String urlInput = urlField.getText().trim();
            if (!urlInput.isEmpty()) {
                imagePath[0] = urlInput;
            }

            String jenisKulit  = fJenisKulit.getValue() != null ? fJenisKulit.getValue() : "Semua Jenis Kulit";
            String areaTubuh   = fAreaTubuh.getValue()  != null ? fAreaTubuh.getValue()  : "Muka";
            String gambarFinal = imagePath[0];

            if (existingProduk == null) {
                Produk p = new Produk(layananData.generateKodeProduk(),
                        fNama.getText(), fKategori.getValue(),
                        harga, fKandungan.getText(), fAktif.isSelected(),
                        jenisKulit, areaTubuh, gambarFinal);
                layananData.tambahProduk(p);
            } else {
                existingProduk.setNamaProduk(fNama.getText());
                existingProduk.setKategori(fKategori.getValue());
                existingProduk.setHarga(harga);
                existingProduk.setKandungan(fKandungan.getText());
                existingProduk.setJenisKulit(jenisKulit);
                existingProduk.setAreaTubuh(areaTubuh);
                existingProduk.setGambarUrl(gambarFinal);
                existingProduk.setAktif(fAktif.isSelected());
                existingProduk.setDeskripsi(fDeskripsi.getText());
                layananData.updateProduk(existingProduk);
            }
            filterKategori.getItems().setAll(layananData.getAllKategori());
            filterKategori.setValue("Semua Kategori");
            tampilkanPanel();
            dialog.close();
        });

        root.getChildren().addAll(
                titleLbl,
                labeledField("Nama Produk",  fNama),
                labeledField("Kategori",     fKategori),
                labeledField("Harga (Rp)",   fHarga),
                labeledField("Kandungan",    fKandungan),
                labeledField("Deskripsi",    fDeskripsi),
                labeledField("Jenis Kulit",  fJenisKulit),
                labeledField("Area Tubuh",   fAreaTubuh),
                labeledField("Gambar Produk", gambarRow),
                fAktif, errMsg, btnRow
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;-fx-border-color:transparent;");

        Scene scene = new Scene(scroll, 480, 720);
        scene.getStylesheets().add(
                getClass().getResource("/org/sahabatlaris/chatbot/css/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox labeledField(String label, javafx.scene.Node field) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label-sm");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot) : "";
    }

    private String sanitizeFileName(String original) {
        String base = original.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        String ext  = getExtension(base);
        String name = base.substring(0, base.length() - ext.length());
        return name + "_" + System.currentTimeMillis() + ext;
    }
}
