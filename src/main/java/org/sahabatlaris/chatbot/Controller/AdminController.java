package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.model.Produk;
import org.sahabatlaris.chatbot.ui.AppUI;
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
import java.util.List;

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

    @FXML
    public void initialize() {
        ensureImageDir();
        setupTable();
        setupFilterKategori();
        loadInfoToko();
        setupJamOperasional();
        tampilkanPanel();
    }

    /** Buat folder images/produk/ jika belum ada */
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
            new AppUI().showAdminLogin(stage);
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

    // ════════════════════════════════════════════════════════════════════════
    //  FORM TAMBAH / UBAH PRODUK  (dengan fitur Upload Gambar)
    // ════════════════════════════════════════════════════════════════════════

    private void showFormProduk(Produk existingProduk) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existingProduk == null ? "Tambah Produk" : "Ubah Produk");

        // ── ScrollPane supaya form tidak terpotong di layar kecil ────────────
        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label titleLbl = new Label(existingProduk == null ? "Tambah Produk Baru" : "Ubah Produk");
        titleLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        // ── Field teks ───────────────────────────────────────────────────────
        TextField fNama = new TextField();
        fNama.setPromptText("Nama Produk");
        fNama.getStyleClass().add("info-field");

        ComboBox<String> fKategori = new ComboBox<>();
        fKategori.getItems().addAll("Pelembab","Toner","Serum","Pembersih Muka",
                "Chemical Sunscreen","Tinted Sunscreen","Exfoliator");
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

        // ── Gambar: preview + tombol Upload ─────────────────────────────────
        // imagePath menyimpan path relatif (images/produk/xxx.jpg)
        final String[] imagePath = {""};

        // Preview thumbnail
        StackPane previewPane = new StackPane();
        previewPane.setMinSize(100, 100);
        previewPane.setMaxSize(100, 100);
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
        previewImg.setFitWidth(100);
        previewImg.setFitHeight(100);
        previewImg.setPreserveRatio(true);
        Rectangle clip = new Rectangle(100, 100);
        clip.setArcWidth(16); clip.setArcHeight(16);
        previewImg.setClip(clip);
        previewImg.setVisible(false);

        previewPane.getChildren().addAll(noImgLbl, previewImg);

        // Nama file yang ditampilkan
        Label fileNameLbl = new Label("Tidak ada file dipilih");
        fileNameLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#888;");

        // Tombol upload
        Button btnUpload = new Button("📁  Pilih Gambar...");
        btnUpload.setStyle(
                "-fx-background-color:#4B3FC8;-fx-text-fill:white;" +
                "-fx-font-size:12px;-fx-padding:6 14;" +
                "-fx-background-radius:6;-fx-cursor:hand;");

        // Tombol hapus gambar
        Button btnHapusGambar = new Button("✕ Hapus Gambar");
        btnHapusGambar.setStyle(
                "-fx-background-color:#e53e3e;-fx-text-fill:white;" +
                "-fx-font-size:11px;-fx-padding:5 10;" +
                "-fx-background-radius:6;-fx-cursor:hand;");
        btnHapusGambar.setVisible(false);

        // Helper: update preview dari path file
        Runnable updatePreview = () -> {
            String p = imagePath[0];
            if (p == null || p.isBlank()) {
                previewImg.setVisible(false);
                noImgLbl.setVisible(true);
                btnHapusGambar.setVisible(false);
                return;
            }
            try {
                File imgFile = new File(p);
                if (imgFile.exists()) {
                    previewImg.setImage(new Image(imgFile.toURI().toString(), 100, 100, true, true));
                } else {
                    // Coba sebagai URL langsung (http/https)
                    previewImg.setImage(new Image(p, 100, 100, true, true, true));
                }
                previewImg.setVisible(true);
                noImgLbl.setVisible(false);
                btnHapusGambar.setVisible(true);
            } catch (Exception ex) {
                previewImg.setVisible(false);
                noImgLbl.setVisible(true);
            }
        };

        // Aksi klik Upload
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
                    // Salin ke folder images/produk/ dengan nama unik
                    String ext  = getExtension(chosen.getName());
                    String dest = IMAGE_DIR + sanitizeFileName(chosen.getName());
                    Files.copy(chosen.toPath(), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
                    imagePath[0] = dest;
                    fileNameLbl.setText(chosen.getName());
                    updatePreview.run();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR,
                            "Gagal menyalin gambar:\n" + ex.getMessage(),
                            ButtonType.OK).showAndWait();
                }
            }
        });

        btnHapusGambar.setOnAction(e -> {
            imagePath[0] = "";
            fileNameLbl.setText("Tidak ada file dipilih");
            updatePreview.run();
        });

        HBox uploadRow = new HBox(10, btnUpload, btnHapusGambar);
        uploadRow.setAlignment(Pos.CENTER_LEFT);

        VBox gambarBox = new VBox(8, previewPane, uploadRow, fileNameLbl);
        gambarBox.setAlignment(Pos.CENTER_LEFT);

        // ── Isi form jika mode Edit ──────────────────────────────────────────
        if (existingProduk != null) {
            fNama.setText(existingProduk.getNamaProduk());
            fKategori.setValue(existingProduk.getKategori());
            fHarga.setText(String.valueOf(existingProduk.getHarga()));
            fKandungan.setText(existingProduk.getKandungan());
            fJenisKulit.setValue(existingProduk.getJenisKulit());
            fAreaTubuh.setValue(existingProduk.getAreaTubuh() != null ? existingProduk.getAreaTubuh() : "Muka");
            fAktif.setSelected(existingProduk.isAktif());
            String existingImg = existingProduk.getGambarUrl();
            if (existingImg != null && !existingImg.isBlank()) {
                imagePath[0] = existingImg;
                // Tampilkan nama file saja
                fileNameLbl.setText(new File(existingImg).getName());
                updatePreview.run();
            }
        }

        // ── Error & Tombol Aksi ──────────────────────────────────────────────
        Label errMsg = new Label();
        errMsg.setStyle("-fx-text-fill:#e53e3e;-fx-font-size:12px;");

        Button btnBatal  = new Button("Batal");
        Button btnSimpan = new Button("Simpan");
        btnBatal.setStyle("-fx-background-color:#888;-fx-text-fill:white;-fx-padding:6 16;-fx-background-radius:4;-fx-cursor:hand;");
        btnSimpan.getStyleClass().add("btn-tambah");

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

            String jenisKulit = fJenisKulit.getValue() != null ? fJenisKulit.getValue() : "Semua Jenis Kulit";
            String areaTubuh  = fAreaTubuh.getValue()  != null ? fAreaTubuh.getValue()  : "Muka";
            String gambarFinal = imagePath[0];

            if (existingProduk == null) {
                Produk p = new Produk(layananData.generateKodeProduk(),
                        fNama.getText(), fKategori.getValue(), harga, fKandungan.getText(), fAktif.isSelected(),
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
                layananData.updateProduk(existingProduk);
            }
            filterKategori.getItems().setAll(layananData.getAllKategori());
            filterKategori.setValue("Semua Kategori");
            tampilkanPanel();
            dialog.close();
        });

        // ── Susun form ───────────────────────────────────────────────────────
        root.getChildren().addAll(
                titleLbl,
                labeledField("Nama Produk", fNama),
                labeledField("Kategori",    fKategori),
                labeledField("Harga",       fHarga),
                labeledField("Kandungan",   fKandungan),
                labeledField("Jenis Kulit", fJenisKulit),
                labeledField("Area Tubuh",  fAreaTubuh),
                labeledField("Gambar Produk", gambarBox),
                fAktif, errMsg, btnRow
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:white;-fx-border-color:transparent;");

        Scene scene = new Scene(scroll, 420, 680);
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

    // ── Helper: ambil ekstensi file ──────────────────────────────────────────
    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot) : "";
    }

    // ── Helper: bersihkan nama file agar aman sebagai path ──────────────────
    private String sanitizeFileName(String original) {
        // Ganti spasi dan karakter khusus, tambah timestamp supaya unik
        String base = original.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        String ext  = getExtension(base);
        String name = base.substring(0, base.length() - ext.length());
        return name + "_" + System.currentTimeMillis() + ext;
    }
}
