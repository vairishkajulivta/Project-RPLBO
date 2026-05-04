package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.ArrayList;
import java.util.List;

public class ChatbotService {

    private DatabaseService db = DatabaseService.getInstance();

    public String cariJawaban(String pesan) {
        String p = pesan.toLowerCase().trim();

        // ── Sapaan ───────────────────────────────────────────────────────────
        if (p.matches(".*\\b(halo|hai|hello|hi|hei|selamat)\\b.*")) {
            return "Halo! Selamat datang di SahabatLaris 👋\n"
                 + "Saya bisa membantu Anda mencari informasi produk skincare untuk kulit sensitif.\n\n"
                 + "Coba tanyakan:\n"
                 + "• Harga moisturizer berapa?\n"
                 + "• Rekomendasi serum\n"
                 + "• Produk untuk kulit sensitif";
        }

        // ── Bantuan ──────────────────────────────────────────────────────────
        if (p.contains("bantuan") || p.contains("help") || p.contains("bisa apa")) {
            return "Saya bisa membantu Anda:\n"
                 + "• Cek harga produk\n"
                 + "• Cari produk berdasarkan kategori\n"
                 + "• Rekomendasi untuk kulit sensitif\n\n"
                 + "Contoh: 'harga toner berapa?' atau cukup ketik 'toner'";
        }

        // ── Deteksi kategori ─────────────────────────────────────────────────
        if (mengandungKategori(p, "pelembab", "moisturizer", "lotion", "krim wajah"))
            return cariProdukByKategori("Pelembab");
        if (mengandungKategori(p, "toner", "toning"))
            return cariProdukByKategori("Toner");
        if (mengandungKategori(p, "serum"))
            return cariProdukByKategori("Serum");
        if (mengandungKategori(p, "pembersih", "face wash", "sabun muka", "cleanser"))
            return cariProdukByKategori("Pembersih muka");
        if (mengandungKategori(p, "sunscreen", "spf", "tabir surya", "sun protection"))
            return cariProdukByKategori("Chemical Sunscreen");
        if (mengandungKategori(p, "exfoliat", "scrub", "aha", "bha", "exfo"))
            return cariProdukByKategori("Exfoliator");

        // ── Cari berdasarkan nama produk spesifik ────────────────────────────
        List<Produk> cocok = getProdukMentioned(pesan);
        if (!cocok.isEmpty()) {
            StringBuilder sb = new StringBuilder("Informasi produk yang Anda cari:\n\n");
            for (Produk prod : cocok) {
                sb.append("• ").append(prod.getNamaProduk()).append("\n");
                sb.append("  ").append(prod.getHargaFormatted()).append("\n");
                sb.append("  Kandungan: ").append(prod.getKandungan()).append("\n\n");
            }
            return sb.toString().trim();
        }

        // ── Tampilkan semua produk ───────────────────────────────────────────
        if (p.contains("produk") || p.contains("ada apa")
                || p.contains("semua") || p.contains("daftar")
                || p.contains("list") || p.contains("apa saja"))
            return cariSemuaProduk();

        // ── Rekomendasi kulit sensitif ───────────────────────────────────────
        if (p.contains("rekomendasi") || p.contains("saran")
                || p.contains("kulit sensitif") || p.contains("sensitif"))
            return "Untuk kulit sensitif, saya rekomendasikan produk berikut:\n\n"
                 + cariSemuaProduk();

        // ── Harga tanpa kategori ─────────────────────────────────────────────
        if (p.contains("harga") || p.contains("berapa") || p.contains("murah") || p.contains("mahal"))
            return cariSemuaProduk();

        // ── Fallback ─────────────────────────────────────────────────────────
        return "Maaf, saya belum memahami pertanyaan Anda 🙏\n\n"
             + "Coba ketik salah satu:\n"
             + "• Nama kategori: toner, serum, pelembab, sunscreen\n"
             + "• 'rekomendasi produk'\n"
             + "• 'semua produk'\n"
             + "• Nama produk secara langsung";
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean mengandungKategori(String pesan, String... keywords) {
        for (String kw : keywords) if (pesan.contains(kw)) return true;
        return false;
    }

    private String cariProdukByKategori(String kategori) {
        List<Produk> list = db.getProdukByKategori(kategori);
        if (list.isEmpty()) return "Tidak ada produk " + kategori + " tersedia saat ini.";
        StringBuilder sb = new StringBuilder(
            "Berikut produk " + kategori.toLowerCase() + " yang tersedia:\n\n");
        for (Produk prod : list) {
            sb.append("• ").append(prod.getNamaProduk()).append("\n");
            sb.append("  ").append(prod.getHargaFormatted()).append("\n");
            sb.append("  Kandungan: ").append(prod.getKandungan()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String cariSemuaProduk() {
        List<Produk> list = db.getAllProduk();
        if (list.isEmpty()) return "Belum ada produk tersedia.";
        StringBuilder sb = new StringBuilder("Berikut semua produk yang tersedia:\n\n");
        for (Produk prod : list) {
            sb.append("• ").append(prod.getNamaProduk())
              .append(" - ").append(prod.getHargaFormatted()).append("\n");
        }
        return sb.toString().trim();
    }

    /** Produk yang namanya atau kategorinya disebut dalam pesan. */
    public List<Produk> getProdukMentioned(String pesan) {
        String pesanLower = pesan.toLowerCase();
        List<Produk> result = new ArrayList<>();
        for (Produk prod : db.getAllProduk()) {
            if (pesanLower.contains(prod.getNamaProduk().toLowerCase())
                    || pesanLower.contains(prod.getKategori().toLowerCase())) {
                result.add(prod);
            }
        }
        return result;
    }

    /**
     * Kembalikan produk yang cocok dengan konteks jawaban chatbot,
     * digunakan ChatManager untuk menampilkan gambar.
     */
    public List<Produk> getProdukDariJawaban(String pesan, String jawaban) {
        String pesanLower = pesan.toLowerCase();

        // Kalau pesan menyebut kategori, kembalikan produk kategori itu
        if (mengandungKategori(pesanLower, "pelembab","moisturizer","lotion","krim wajah"))
            return db.getProdukByKategori("Pelembab");
        if (mengandungKategori(pesanLower, "toner","toning"))
            return db.getProdukByKategori("Toner");
        if (mengandungKategori(pesanLower, "serum"))
            return db.getProdukByKategori("Serum");
        if (mengandungKategori(pesanLower, "pembersih","face wash","sabun muka","cleanser"))
            return db.getProdukByKategori("Pembersih muka");
        if (mengandungKategori(pesanLower, "sunscreen","spf","tabir surya","sun protection"))
            return db.getProdukByKategori("Chemical Sunscreen");

        // Kalau jawaban berisi semua produk atau rekomendasi, kembalikan semua
        if (pesanLower.contains("semua") || pesanLower.contains("produk")
                || pesanLower.contains("rekomendasi") || pesanLower.contains("daftar")
                || pesanLower.contains("harga") || pesanLower.contains("berapa"))
            return db.getAllProduk();

        // Fallback: produk yang disebut namanya
        return getProdukMentioned(pesan);
    }

    /** Semua produk (dipakai ChatManager untuk panel Info Produk). */
    public List<Produk> getAllProduk() {
        return db.getAllProduk();
    }
}
