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
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    @FXML private TableColumn<Produk, Void> colAksi;
    @FXML private Label labelJumlahProduk;
    @FXML private ComboBox<String> filterKategori;
    @FXML private VBox panelDataProduk;
    @FXML private VBox panelInfoToko;
    @FXML private Button btnDataProduk;
    @FXML private Button btnInfoToko;

    @FXML private TextField namaToko;
    @FXML private TextField taglineToko;
    @FXML private TextArea deskripsiToko;
    @FXML private TextArea alamatToko;
    @FXML private TextField linkPeta;
    @FXML private TextField kotaToko;
    @FXML private VBox jamOperasionalContainer;

    private ManagedDataService layananData = new ManagedDataService();
    private ObservableList<Produk> produkObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilterKategori();
        loadInfoToko();
        setupJamOperasional();
        tampilkanPanel();
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
            private final Button btnUbah = new Button("Ubah");
            private final Button btnHapus = new Button("Hapus");
            private final HBox box = new HBox(6, btnUbah, btnHapus);

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

    // ── Load data info toko dari DB ke field ──────────────────────────────
    private void loadInfoToko() {
        String[] info = layananData.getInfoToko();
        if (namaToko != null)     namaToko.setText(info[0]);
        if (taglineToko != null)  taglineToko.setText(info[1]);
        if (deskripsiToko != null) deskripsiToko.setText(info[2]);
        if (alamatToko != null)   alamatToko.setText(info[3]);
        if (kotaToko != null)     kotaToko.setText(info[4]);
        if (linkPeta != null)     linkPeta.setText(info[6]);
    }

    // ── Jam Operasional dengan toggle Buka/Tutup ─────────────────────────
    private void setupJamOperasional() {
        if (jamOperasionalContainer == null) return;
        jamOperasionalContainer.getChildren().clear();

        List<String[]> jamList = layananData.getJamOperasional();
        for (String[] jam : jamList) {
            String hari = jam[0];
            boolean buka = "1".equals(jam[1]);
            String jamBuka = jam[2];
            String jamTutup = jam[3];

            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);

            Label lblHari = new Label(hari);
            lblHari.setMinWidth(70);
            lblHari.getStyleClass().add("field-label-sm");

            // Toggle Button Buka / Tutup
            ToggleButton toggleBuka = new ToggleButton(buka ? "Buka" : "Tutup");
            toggleBuka.setSelected(buka);
            toggleBuka.setStyle(buka
                    ? "-fx-background-color: #4B3FC8; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 14; -fx-cursor: hand; -fx-font-size: 12px;"
                    : "-fx-background-color: #ccc; -fx-text-fill: #555; -fx-background-radius: 12; -fx-padding: 4 14; -fx-cursor: hand; -fx-font-size: 12px;");

            TextField tfBuka = new TextField(jamBuka);
            tfBuka.setPrefWidth(70);
            tfBuka.getStyleClass().add("info-field");
            tfBuka.setDisable(!buka);

            TextField tfTutup = new TextField(jamTutup);
            tfTutup.setPrefWidth(70);
            tfTutup.getStyleClass().add("info-field");
            tfTutup.setDisable(!buka);

            Label lblSampai = new Label("-");

            toggleBuka.setOnAction(e -> {
                boolean isOpen = toggleBuka.isSelected();
                toggleBuka.setText(isOpen ? "Buka" : "Tutup");
                toggleBuka.setStyle(isOpen
                        ? "-fx-background-color: #4B3FC8; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 14; -fx-cursor: hand; -fx-font-size: 12px;"
                        : "-fx-background-color: #ccc; -fx-text-fill: #555; -fx-background-radius: 12; -fx-padding: 4 14; -fx-cursor: hand; -fx-font-size: 12px;");
                tfBuka.setDisable(!isOpen);
                tfTutup.setDisable(!isOpen);
                // Simpan ke DB
                layananData.simpanJamOperasional(hari, isOpen ? 1 : 0, tfBuka.getText(), tfTutup.getText());
            });

            row.getChildren().addAll(lblHari, toggleBuka, tfBuka, lblSampai, tfTutup);
            jamOperasionalContainer.getChildren().add(row);

            // Simpan perubahan jam saat field berubah (listener)
            tfBuka.focusedProperty().addListener((obs, o, n) -> {
                if (!n) layananData.simpanJamOperasional(hari, toggleBuka.isSelected() ? 1 : 0, tfBuka.getText(), tfTutup.getText());
            });
            tfTutup.focusedProperty().addListener((obs, o, n) -> {
                if (!n) layananData.simpanJamOperasional(hari, toggleBuka.isSelected() ? 1 : 0, tfBuka.getText(), tfTutup.getText());
            });
        }
    }

    @FXML
    public void tampilkanPanel() {
        String kategoriFilter = filterKategori != null ? filterKategori.getValue() : "Semua Kategori";
        List<Produk> list = layananData.getProdukByKategori(kategoriFilter);
        produkObservable.setAll(list);
        if (labelJumlahProduk != null)
            labelJumlahProduk.setText(list.size() + " produk");
    }

    @FXML
    public void showDataProduk() {
        panelDataProduk.setVisible(true);
        panelInfoToko.setVisible(false);
        setSidebarActive(btnDataProduk, btnInfoToko);
    }

    @FXML
    public void showInfoToko() {
        panelDataProduk.setVisible(false);
        panelInfoToko.setVisible(true);
        setSidebarActive(btnInfoToko, btnDataProduk);
    }

    private void setSidebarActive(javafx.scene.control.Button active, javafx.scene.control.Button inactive) {
        active.getStyleClass().remove("sidebar-btn");
        if (!active.getStyleClass().contains("sidebar-btn-active"))
            active.getStyleClass().add("sidebar-btn-active");
        inactive.getStyleClass().remove("sidebar-btn-active");
        if (!inactive.getStyleClass().contains("sidebar-btn"))
            inactive.getStyleClass().add("sidebar-btn");
    }

    @FXML
    public void showTambahProduk() {
        showFormProduk(null);
    }

    @FXML
    public void simpanInfoToko() {
        layananData.simpanInfoToko(
                namaToko != null ? namaToko.getText() : "",
                taglineToko != null ? taglineToko.getText() : "",
                deskripsiToko != null ? deskripsiToko.getText() : "",
                alamatToko != null ? alamatToko.getText() : "",
                kotaToko != null ? kotaToko.getText() : "",
                "",
                linkPeta != null ? linkPeta.getText() : ""
        );
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Info toko berhasil disimpan!", ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) tabelProduk.getScene().getWindow();
            new AppUI().showAdminLogin(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFormProduk(Produk existingProduk) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existingProduk == null ? "Tambah Produk" : "Ubah Produk");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label title = new Label(existingProduk == null ? "Tambah Produk Baru" : "Ubah Produk");
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e;");

        TextField fNama = new TextField();
        fNama.setPromptText("Nama Produk");
        fNama.getStyleClass().add("info-field");

        ComboBox<String> fKategori = new ComboBox<>();
        fKategori.getItems().addAll("Pelembab", "Toner", "Serum", "Pembersih Muka",
                "Chemical Sunscreen", "Tinted Sunscreen", "Exfoliator");
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

        // ── Jenis Kulit ──────────────────────────────────────────────────
        ComboBox<String> fJenisKulit = new ComboBox<>();
        fJenisKulit.getItems().addAll("Semua Jenis Kulit", "Kulit Sensitif",
                "Kulit Berminyak", "Kulit Kering", "Kulit Normal",
                "Kulit Berjerawat", "Kulit Menua");
        fJenisKulit.setPromptText("Pilih Jenis Kulit");
        fJenisKulit.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> fAreaTubuh = new ComboBox<>();
        fAreaTubuh.getItems().addAll("Muka", "Badan", "Rambut", "Tangan & Kaki", "Bibir", "Mata");
        fAreaTubuh.setPromptText("Pilih Area Tubuh");
        fAreaTubuh.setMaxWidth(Double.MAX_VALUE);

        TextField fGambarUrl = new TextField();
        fGambarUrl.setPromptText("URL Gambar Produk (opsional)");
        fGambarUrl.getStyleClass().add("info-field");

        CheckBox fAktif = new CheckBox("Aktif");
        fAktif.setSelected(true);

        if (existingProduk != null) {
            fNama.setText(existingProduk.getNamaProduk());
            fKategori.setValue(existingProduk.getKategori());
            fHarga.setText(String.valueOf(existingProduk.getHarga()));
            fKandungan.setText(existingProduk.getKandungan());
            fJenisKulit.setValue(existingProduk.getJenisKulit());
            fAreaTubuh.setValue(existingProduk.getAreaTubuh() != null ? existingProduk.getAreaTubuh() : "Muka");
            fGambarUrl.setText(existingProduk.getGambarUrl());
            fAktif.setSelected(existingProduk.isAktif());
        }

        Label errMsg = new Label();
        errMsg.setStyle("-fx-text-fill: #e53e3e; -fx-font-size:12px;");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("btn-keluar");
        btnBatal.setStyle("-fx-background-color:#888; -fx-text-fill:white; -fx-padding:6 16; -fx-background-radius:4; -fx-cursor:hand;");
        Button btnSimpan = new Button("Simpan");
        btnSimpan.getStyleClass().add("btn-tambah");

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
            String areaTubuh = fAreaTubuh.getValue() != null ? fAreaTubuh.getValue() : "Muka";

            if (existingProduk == null) {
                Produk p = new Produk(layananData.generateKodeProduk(), fNama.getText(),
                        fKategori.getValue(), harga, fKandungan.getText(), fAktif.isSelected(),
                        jenisKulit, areaTubuh, fGambarUrl.getText());
                layananData.tambahProduk(p);
            } else {
                existingProduk.setNamaProduk(fNama.getText());
                existingProduk.setKategori(fKategori.getValue());
                existingProduk.setHarga(harga);
                existingProduk.setKandungan(fKandungan.getText());
                existingProduk.setJenisKulit(jenisKulit);
                existingProduk.setAreaTubuh(areaTubuh);
                existingProduk.setGambarUrl(fGambarUrl.getText());
                existingProduk.setAktif(fAktif.isSelected());
                layananData.updateProduk(existingProduk);
            }
            filterKategori.getItems().setAll(layananData.getAllKategori());
            filterKategori.setValue("Semua Kategori");
            tampilkanPanel();
            dialog.close();
        });

        btnRow.getChildren().addAll(btnBatal, btnSimpan);

        root.getChildren().addAll(title,
                labeledField("Nama Produk", fNama),
                labeledField("Kategori", fKategori),
                labeledField("Harga", fHarga),
                labeledField("Kandungan", fKandungan),
                labeledField("Jenis Kulit", fJenisKulit),
                labeledField("Area Tubuh", fAreaTubuh),
                labeledField("URL Gambar", fGambarUrl),
                fAktif, errMsg, btnRow);

        Scene scene = new Scene(root, 400, 620);
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
}

