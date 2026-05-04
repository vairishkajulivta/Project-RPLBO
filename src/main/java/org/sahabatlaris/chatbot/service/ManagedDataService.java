package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.List;

public class ManagedDataService {
    private DatabaseService db = DatabaseService.getInstance();

    public void tambahProduk(Produk p) { db.tambahProduk(p); }
    public void hapusProduk(String kodeProduk) { db.hapusProduk(kodeProduk); }
    public void updateProduk(Produk p) { db.updateProduk(p); }
    public List<Produk> getAllProduk() { return db.getAllProduk(); }
    public List<Produk> getProdukByKategori(String k) { return db.getProdukByKategori(k); }
    public List<String> getAllKategori() { return db.getAllKategori(); }
    public String generateKodeProduk() { return db.generateKodeProduk(); }

    // Info Toko
    public String[] getInfoToko() { return db.getInfoToko(); }
    public void simpanInfoToko(String nama, String tagline, String deskripsi,
                               String alamat, String kota, String kodePos, String linkPeta) {
        db.simpanInfoToko(nama, tagline, deskripsi, alamat, kota, kodePos, linkPeta);
    }

    // Jam Operasional
    public List<String[]> getJamOperasional() { return db.getJamOperasional(); }
    public void simpanJamOperasional(String hari, int buka, String jamBuka, String jamTutup) {
        db.simpanJamOperasional(hari, buka, jamBuka, jamTutup);
    }
}

