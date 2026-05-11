package org.sahabatlaris.chatbot.model;

import javafx.beans.property.*;

public class Produk {
    private final StringProperty  kodeProduk  = new SimpleStringProperty();
    private final StringProperty  namaProduk  = new SimpleStringProperty();
    private final StringProperty  kategori    = new SimpleStringProperty();
    private final LongProperty    harga       = new SimpleLongProperty();
    private final StringProperty  kandungan   = new SimpleStringProperty();
    private final BooleanProperty aktif       = new SimpleBooleanProperty(true);
    private final StringProperty  jenisKulit  = new SimpleStringProperty();
    private final StringProperty  areaTubuh   = new SimpleStringProperty();
    private final StringProperty  gambarUrl   = new SimpleStringProperty();

    public Produk() {}

    // Constructor 6 field (lama)
    public Produk(String kodeProduk, String namaProduk, String kategori,
                  long harga, String kandungan, boolean aktif) {
        this.kodeProduk.set(kodeProduk);
        this.namaProduk.set(namaProduk);
        this.kategori.set(kategori);
        this.harga.set(harga);
        this.kandungan.set(kandungan);
        this.aktif.set(aktif);
        this.jenisKulit.set("Semua Jenis Kulit");
        this.areaTubuh.set("Muka");
        this.gambarUrl.set("");
    }

    // Constructor lengkap 9 field
    public Produk(String kodeProduk, String namaProduk, String kategori,
                  long harga, String kandungan, boolean aktif,
                  String jenisKulit, String areaTubuh, String gambarUrl) {
        this.kodeProduk.set(kodeProduk);
        this.namaProduk.set(namaProduk);
        this.kategori.set(kategori);
        this.harga.set(harga);
        this.kandungan.set(kandungan);
        this.aktif.set(aktif);
        this.jenisKulit.set(jenisKulit  != null ? jenisKulit  : "Semua Jenis Kulit");
        this.areaTubuh.set(areaTubuh    != null ? areaTubuh   : "Muka");
        this.gambarUrl.set(gambarUrl    != null ? gambarUrl   : "");
    }

    // ── kodeProduk ───────────────────────────────────────────────────────────
    public String getKodeProduk()              { return kodeProduk.get(); }
    public void setKodeProduk(String v)        { kodeProduk.set(v); }
    public StringProperty kodeProdukProperty() { return kodeProduk; }

    // ── namaProduk ───────────────────────────────────────────────────────────
    public String getNamaProduk()              { return namaProduk.get(); }
    public void setNamaProduk(String v)        { namaProduk.set(v); }
    public StringProperty namaProdukProperty() { return namaProduk; }

    // ── kategori ─────────────────────────────────────────────────────────────
    public String getKategori()                { return kategori.get(); }
    public void setKategori(String v)          { kategori.set(v); }
    public StringProperty kategoriProperty()   { return kategori; }

    // ── harga ────────────────────────────────────────────────────────────────
    public long getHarga()                     { return harga.get(); }
    public void setHarga(long v)               { harga.set(v); }
    public LongProperty hargaProperty()        { return harga; }

    // ── kandungan ────────────────────────────────────────────────────────────
    public String getKandungan()               { return kandungan.get(); }
    public void setKandungan(String v)         { kandungan.set(v); }
    public StringProperty kandunganProperty()  { return kandungan; }

    // ── aktif ────────────────────────────────────────────────────────────────
    public boolean isAktif()                   { return aktif.get(); }
    public void setAktif(boolean v)            { aktif.set(v); }
    public BooleanProperty aktifProperty()     { return aktif; }

    // ── jenisKulit ───────────────────────────────────────────────────────────
    public String getJenisKulit()              { return jenisKulit.get(); }
    public void setJenisKulit(String v)        { jenisKulit.set(v); }
    public StringProperty jenisKulitProperty() { return jenisKulit; }

    // ── areaTubuh ────────────────────────────────────────────────────────────
    public String getAreaTubuh()               { return areaTubuh.get(); }
    public void setAreaTubuh(String v)         { areaTubuh.set(v); }
    public StringProperty areaTubuhProperty()  { return areaTubuh; }

    // ── gambarUrl ────────────────────────────────────────────────────────────
    public String getGambarUrl()               { return gambarUrl.get(); }
    public void setGambarUrl(String v)         { gambarUrl.set(v); }
    public StringProperty gambarUrlProperty()  { return gambarUrl; }

    // ── helper ───────────────────────────────────────────────────────────────
    public String getHargaFormatted() {
        return String.format("Rp. %,d", harga.get()).replace(",", ".");
    }
}
