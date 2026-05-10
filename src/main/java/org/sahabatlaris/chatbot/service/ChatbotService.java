package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.ArrayList;
import java.util.List;

public class ChatbotService {

    private DatabaseService db = DatabaseService.getInstance();

    public String cariJawaban(String pesan) {
        String p = pesan.toLowerCase().trim();


        if (p.matches(".*\\b(halo|hai|hello|hi|hei|selamat)\\b.*")) {
            return "Halo! Selamat datang di SahabatLaris \uD83D\uDC4B\n"
                    + "Saya bisa membantu Anda mencari informasi produk skincare.\n\n"
                    + "Coba tanyakan:\n"
                    + "\u2022 Tampilkan produk skincare untuk kulit sensitif\n"
                    + "\u2022 Tampilkan deskripsi, kandungan, dan harga untuk Moisturizer\n"
                    + "\u2022 Apakah Wardah Hydra Rose cocok untuk kulit sensitif?\n"
                    + "\u2022 Dimana lokasi toko di daerah Sleman?\n"
                    + "\u2022 Tampilkan link maps untuk lokasi Tugu\n"
                    + "\u2022 Tampilkan jam buka dan tutup toko\n"
                    + "\u2022 Tampilkan semua produk untuk kategori Sabun Wajah\n"
                    + "\u2022 Apakah Somethinc Calm Down masih tersedia?";
        }


        if (p.contains("bantuan") || p.contains("help") || p.contains("bisa apa")) {
            return "Saya bisa membantu Anda:\n"
                    + "\u2022 Cek harga produk\n"
                    + "\u2022 Cari produk berdasarkan kategori\n"
                    + "\u2022 Rekomendasi berdasarkan jenis kulit\n"
                    + "\u2022 Info lokasi & jam buka toko\n\n"
                    + "Contoh: 'harga toner berapa?' atau cukup ketik 'toner'";
        }

        if (p.contains("lokasi") || p.contains("alamat") || p.contains("dimana") || p.contains("di mana")) {
            String[] info = db.getInfoToko();
            return "\uD83D\uDCCD Lokasi " + info[0] + ":\n" + info[3] + "\n\uD83C\uDFD9\uFE0F " + info[4];
        }


        if (p.contains("maps") || p.contains("peta") || p.contains("navigasi") || p.contains("link")) {
            String[] info = db.getInfoToko();
            String link = info[6];
            return "\uD83D\uDDFA\uFE0F Link Maps " + info[0] + ":\n" + (link.isEmpty() ? "Belum tersedia" : link);
        }

        if (p.contains("jam") || p.contains("buka") || p.contains("tutup") || p.contains("operasional")) {
            StringBuilder sb = new StringBuilder("\uD83D\uDD50 Jam Operasional Toko:\n\n");
            List<String[]> jamList = db.getJamOperasional();
            for (String[] jam : jamList) {
                String status = "1".equals(jam[1]) ? "Buka " + jam[2] + " - " + jam[3] : "Tutup";
                sb.append(String.format("%-8s : %s\n", jam[0], status));
            }
            return sb.toString().trim();
        }

        if (p.contains("kulit sensitif")) {
            return cariProdukByKategori("Kulit Sensitif");
        }
        if (p.contains("kulit berminyak") || p.contains("berminyak")) {
            return cariProdukByKategori("Kulit Berminyak");
        }
        if (p.contains("kulit kering") || p.contains("kering")) {
            return cariProdukByKategori("Kulit Kering");
        }
        if (p.contains("kulit berjerawat") || p.contains("berjerawat") || p.contains("jerawat")) {
            return cariProdukByKategori("Kulit Berjerawat");
        }
        if (p.contains("kulit menua") || p.contains("anti aging") || p.contains("penuaan")) {
            return cariProdukByKategori("Kulit Menua");
        }


        // ── Deteksi kategori ─────────────────────────────────────────────────
        if (mengandungKategori(p, "pelembab", "moisturizer", "lotion", "krim wajah")) {
            return cariProdukByKategori("Pelembab");
        }
        if (mengandungKategori(p, "toner", "toning")) {
            return cariProdukByKategori("Toner");
        }
        if (mengandungKategori(p, "serum")) {
            return cariProdukByKategori("Serum");
        }
        if (mengandungKategori(p, "pembersih", "face wash", "sabun muka", "cleanser")) {
            return cariProdukByKategori("Pembersih Muka");
        }
        if (mengandungKategori(p, "sunscreen", "spf", "tabir surya", "sun protection")) {
            return cariProdukByKategori("Chemical Sunscreen");
        }
        if (mengandungKategori(p, "exfoliat", "scrub", "aha", "bha", "exfo")) {
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
