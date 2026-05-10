package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.ArrayList;
import java.util.List;

public class ChatbotService {

    private DatabaseService db = DatabaseService.getInstance();
    private List<Produk> lastProdukResult = null; // simpan hasil produk terakhir

    public String cariJawaban(String pesan) {
        lastProdukResult = null; // reset tiap kali dipanggil
        String p = pesan.toLowerCase().trim();

        // ── Status Toko / Jam Buka ────────────────────────────────────────────
        if (p.contains("buka") || p.contains("tutup") || p.contains("jam")
                || p.contains("operasional") || p.contains("status toko")
                || p.contains("hari libur") || p.contains("libur")
                || p.contains("toko buka") || p.contains("toko tutup")) {
            return cekStatusToko();
        }

        // ── Lokasi / Alamat ───────────────────────────────────────────────────
        if (p.contains("lokasi") || p.contains("alamat") || p.contains("dimana")
                || p.contains("di mana") || p.contains("maps") || p.contains("peta")) {
            return infoLokasi();
        }

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
        if (mengandungKategori(p, "pelembab", "moisturizer", "lotion", "krim wajah")) {
            lastProdukResult = db.getProdukByKategori("Pelembab");
            return cariProdukByKategori("Pelembab");
        }
        if (mengandungKategori(p, "toner", "toning")) {
            lastProdukResult = db.getProdukByKategori("Toner");
            return cariProdukByKategori("Toner");
        }
        if (mengandungKategori(p, "serum")) {
            lastProdukResult = db.getProdukByKategori("Serum");
            return cariProdukByKategori("Serum");
        }
        if (mengandungKategori(p, "pembersih", "face wash", "sabun muka", "cleanser")) {
            lastProdukResult = db.getProdukByKategori("Pembersih muka");
            return cariProdukByKategori("Pembersih muka");
        }
        if (mengandungKategori(p, "sunscreen", "spf", "tabir surya", "sun protection")) {
            lastProdukResult = db.getProdukByKategori("Chemical Sunscreen");
            return cariProdukByKategori("Chemical Sunscreen");
        }
        if (mengandungKategori(p, "exfoliat", "scrub", "aha", "bha", "exfo")) {
            lastProdukResult = db.getProdukByKategori("Exfoliator");
            return cariProdukByKategori("Exfoliator");
        }

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
                || p.contains("list") || p.contains("apa saja")) {
            lastProdukResult = db.getAllProduk();
            return cariSemuaProduk();
        }

        // ── Rekomendasi kulit sensitif ───────────────────────────────────────
        if (p.contains("rekomendasi") || p.contains("saran")
                || p.contains("kulit sensitif") || p.contains("sensitif")) {
            lastProdukResult = db.getAllProduk();
            return "Untuk kulit sensitif, saya rekomendasikan produk berikut:\n\n"
                 + cariSemuaProduk();
        }

        // ── Harga tanpa kategori ─────────────────────────────────────────────
        if (p.contains("harga") || p.contains("berapa") || p.contains("murah") || p.contains("mahal")) {
            lastProdukResult = db.getAllProduk();
            return cariSemuaProduk();
        }

        // ── Fallback ─────────────────────────────────────────────────────────
        return "Maaf, saya belum memahami pertanyaan Anda 🙏\n\n"
             + "Coba ketik salah satu:\n"
             + "• Nama kategori: toner, serum, pelembab, sunscreen\n"
             + "• 'rekomendasi produk'\n"
             + "• 'semua produk'\n"
             + "• Nama produk secara langsung";
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Kembalikan hasil produk dari pemanggilan cariJawaban() terakhir. */
    public List<Produk> getLastProdukResult() {
        return lastProdukResult;
    }

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

    private String cekStatusToko() {
        String[] status = db.cekStatusTokoHariIni();
        String[] info   = db.getInfoToko();
        String namaToko = info.length > 0 ? info[0] : "Toko";

        StringBuilder sb = new StringBuilder();
        sb.append("🏪 Status ").append(namaToko).append("\n\n");

        if ("BUKA".equals(status[0])) {
            sb.append("✅ Toko sedang BUKA\n");
        } else {
            sb.append("❌ Toko sedang TUTUP\n");
        }

        sb.append("📋 ").append(status[1]).append("\n");

        if (!"-".equals(status[2])) {
            sb.append("🕐 Jam operasional: ").append(status[2])
              .append(" - ").append(status[3]).append("\n");
        }

        sb.append("\nKetik 'jam operasional' untuk jadwal lengkap semua hari.");
        return sb.toString();
    }

    private String infoLokasi() {
        String[] info = db.getInfoToko();
        if (info.length < 7) return "Informasi lokasi belum tersedia.";
        return "📍 Lokasi " + info[0] + "\n\n"
             + "🗺 " + info[3] + ", " + info[4] + " " + info[5] + "\n"
             + "🔗 " + info[6];
    }

    /** Semua produk (dipakai ChatManager untuk panel Info Produk). */
    public List<Produk> getAllProduk() {
        return db.getAllProduk();
    }
}
