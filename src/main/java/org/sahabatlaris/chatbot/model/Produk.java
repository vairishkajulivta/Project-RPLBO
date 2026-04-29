package org.sahabatlaris.chatbot.model;

import javafx.beans.property.*;

public class Produk {
    private final StringProperty kodeProduk = new SimpleStringProperty();
    private final StringProperty namaProduk = new SimpleStringProperty();
    private final StringProperty kategori = new SimpleStringProperty();
    private final LongProperty harga = new SimpleLongProperty();
    private final StringProperty kandungan = new SimpleStringProperty();
    private final BooleanProperty aktif = new SimpleBooleanProperty(true);

    public Produk() {}

    public Produk(String kodeProduk, String namaProduk, String kategori,
                  long harga, String kandungan, boolean aktif) {
        this.kodeProduk.set(kodeProduk);
        this.namaProduk.set(namaProduk);
        this.kategori.set(kategori);
        this.harga.set(harga);
        this.kandungan.set(kandungan);
        this.aktif.set(aktif);
    }

    public String getKodeProduk() { return kodeProduk.get(); }
    public void setKodeProduk(String v) { kodeProduk.set(v); }
    public StringProperty kodeProdukProperty() { return kodeProduk; }

    public String getNamaProduk() { return namaProduk.get(); }
    public void setNamaProduk(String v) { namaProduk.set(v); }
    public StringProperty namaProdukProperty() { return namaProduk; }

    public String getKategori() { return kategori.get(); }
    public void setKategori(String v) { kategori.set(v); }
    public StringProperty kategoriProperty() { return kategori; }

    public long getHarga() { return harga.get(); }
    public void setHarga(long v) { harga.set(v); }
    public LongProperty hargaProperty() { return harga; }

    public String getKandungan() { return kandungan.get(); }
    public void setKandungan(String v) { kandungan.set(v); }
    public StringProperty kandunganProperty() { return kandungan; }

    public boolean isAktif() { return aktif.get(); }
    public void setAktif(boolean v) { aktif.set(v); }
    public BooleanProperty aktifProperty() { return aktif; }

    public String getHargaFormatted() {
        return String.format("Rp. %,d", harga.get()).replace(",", ".");
    }
}
