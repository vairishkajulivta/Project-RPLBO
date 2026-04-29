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
}
