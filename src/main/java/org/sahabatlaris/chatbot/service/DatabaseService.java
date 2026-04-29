package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static DatabaseService instance;
    private final List<Produk> produkList = new ArrayList<>();

    private DatabaseService() {
        initDataSample();
    }

    public static DatabaseService getInstance() {
        if (instance == null) instance = new DatabaseService();
        return instance;
    }

    private void initDataSample() {
        produkList.add(new Produk("P001", "Gentle Glow Moisturizer", "Pelembab", 48000, "Ceramide, Aloe Vera", true));
        produkList.add(new Produk("P002", "Soothing Toner", "Toner", 65000, "Witch Hazel, Green Tea", true));
        produkList.add(new Produk("P003", "Barrier Repair Serum", "Serum", 120000, "Niacinamide, Panthenol", true));
        produkList.add(new Produk("P004", "Calming Face Wash", "Pembersih muka", 55000, "Centella, Panthenol", true));
        produkList.add(new Produk("P005", "Acnaway Mugwort Water", "Pelembab", 38000, "Mugwort, Centella, Panthenol", true));
        produkList.add(new Produk("P006", "Somethinc Holysnail Gel SPF 50+", "Chemical Sunscreen", 48000, "Encapsulated UV Filter, Niacinamide & Tocopherol (Vitamin E)", true));
    }

    public List<Produk> getAllProduk() { return new ArrayList<>(produkList); }

    public List<Produk> getProdukByKategori(String kategori) {
        if (kategori == null || kategori.equals("Semua Kategori")) return getAllProduk();
        List<Produk> result = new ArrayList<>();
        for (Produk p : produkList) {
            if (p.getKategori().equalsIgnoreCase(kategori)) result.add(p);
        }
        return result;
    }

    public void tambahProduk(Produk p) { produkList.add(p); }

    public void updateProduk(Produk p) {
        for (int i = 0; i < produkList.size(); i++) {
            if (produkList.get(i).getKodeProduk().equals(p.getKodeProduk())) {
                produkList.set(i, p);
                return;
            }
        }
    }

    public void hapusProduk(String kodeProduk) {
        produkList.removeIf(p -> p.getKodeProduk().equals(kodeProduk));
    }

    public List<String> getAllKategori() {
        List<String> cats = new ArrayList<>();
        cats.add("Semua Kategori");
        for (Produk p : produkList) {
            if (!cats.contains(p.getKategori())) cats.add(p.getKategori());
        }
        return cats;
    }

    public String generateKodeProduk() {
        return "P" + String.format("%03d", produkList.size() + 1);
    }
}
