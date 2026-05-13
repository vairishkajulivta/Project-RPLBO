package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.ArrayList;
import java.util.List;

public class ChatbotService {

    private DatabaseService db = DatabaseService.getInstance();
    private List<Produk> lastProdukResult = null;

    public String cariJawaban(String pesan) {
        lastProdukResult = null;
        String p = pesan.toLowerCase().trim();

        if (p.contains("buka") || p.contains("tutup") || p.contains("jam")
                || p.contains("operasional") || p.contains("status toko")
                || p.contains("hari libur") || p.contains("libur")
                || p.contains("toko buka") || p.contains("toko tutup")) {
            return cekStatusToko();
        }


        if (p.contains("lokasi") || p.contains("alamat") || p.contains("dimana")
                || p.contains("di mana") || p.contains("maps") || p.contains("peta")) {
            return infoLokasi();
        }


        if (p.matches(".*\\b(halo|hai|hello|hi|hei|selamat)\\b.*")) {
            return "Halo! Selamat datang di SahabatLaris \uD83D\uDC4B\n"
                    + "Saya bisa membantu Anda mencari informasi produk skincare.\n\n"
                    + "Coba tanyakan:\n"
                    + "\u2022 Tampilkan produk skincare untuk kulit sensitif\n"
                    + "\u2022 Tampilkan deskripsi, kandungan, dan harga untuk Moisturizer\n"
                    + "\u2022 Apakah Wardah Hydra Rose cocok untuk kulit sensitif?\n"
                    + "\u2022 Dimana lokasi toko?\n"
                    + "\u2022 Tampilkan link maps lokasi toko\n"
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


        if (p.contains("kulit kering") || p.contains("kering")) {
            lastProdukResult = getProdukByJenisKulit("Kulit Kering");
            return formatProdukJenisKulit("Kulit Kering", lastProdukResult);
        }
        if (p.contains("kulit berminyak") || p.contains("berminyak")) {
            lastProdukResult = getProdukByJenisKulit("Kulit Berminyak");
            return formatProdukJenisKulit("Kulit Berminyak", lastProdukResult);
        }
        if (p.contains("kulit berjerawat") || p.contains("berjerawat") || p.contains("jerawat")) {
            lastProdukResult = getProdukByJenisKulit("Kulit Berjerawat");
            return formatProdukJenisKulit("Kulit Berjerawat", lastProdukResult);
        }
        if (p.contains("kulit sensitif") || p.contains("sensitif")) {
            lastProdukResult = getProdukByJenisKulit("Kulit Sensitif");
            return formatProdukJenisKulit("Kulit Sensitif", lastProdukResult);
        }
        if (p.contains("kulit normal") || p.contains("normal")) {
            lastProdukResult = getProdukByJenisKulit("Kulit Normal");
            return formatProdukJenisKulit("Kulit Normal", lastProdukResult);
        }
        if (p.contains("kulit menua") || p.contains("menua") || p.contains("anti aging") || p.contains("penuaan")) {
            lastProdukResult = getProdukByJenisKulit("Kulit Menua");
            return formatProdukJenisKulit("Kulit Menua", lastProdukResult);
        }
        if (p.contains("semua jenis kulit") || (p.contains("semua") && p.contains("kulit"))) {
            lastProdukResult = getProdukByJenisKulit("Semua Jenis Kulit");
            return formatProdukJenisKulit("Semua Jenis Kulit", lastProdukResult);
        }


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
        if (mengandungKategori(p, "pembersih", "face wash", "sabun muka", "cleanser", "facial wash")) {
            lastProdukResult = db.getProdukByKategori("Facial Wash");
            return cariProdukByKategori("Facial Wash");
        }
        if (mengandungKategori(p, "sunscreen", "spf", "tabir surya", "sun protection")) {
            lastProdukResult = db.getProdukByKategori("Sunscreen");
            return cariProdukByKategori("Sunscreen");
        }
        if (mengandungKategori(p, "exfoliat", "scrub", "aha", "bha", "exfo")) {
            lastProdukResult = db.getProdukByKategori("Exfoliator");
            return cariProdukByKategori("Exfoliator");
        }
        if (mengandungKategori(p, "body care", "badan", "tubuh", "lotion badan")) {
            lastProdukResult = db.getProdukByKategori("Body Care");
            return cariProdukByKategori("Body Care");
        }
        if (mengandungKategori(p, "eye care", "mata", "eye cream")) {
            lastProdukResult = db.getProdukByKategori("Eye Care");
            return cariProdukByKategori("Eye Care");
        }
        if (mengandungKategori(p, "lip care", "bibir", "lip")) {
            lastProdukResult = db.getProdukByKategori("Lip Care");
            return cariProdukByKategori("Lip Care");
        }
        if (mengandungKategori(p, "hair care", "rambut", "shampoo")) {
            lastProdukResult = db.getProdukByKategori("Hair Care");
            return cariProdukByKategori("Hair Care");
        }
        if (mengandungKategori(p, "acne care", "acne patch", "patch")) {
            lastProdukResult = db.getProdukByKategori("Acne Care");
            return cariProdukByKategori("Acne Care");
        }
        if (mengandungKategori(p, "hand care", "tangan", "kaki")) {
            lastProdukResult = db.getProdukByKategori("Hand Care");
            return cariProdukByKategori("Hand Care");
        }


        List<Produk> cocok = getProdukMentioned(pesan);
        if (!cocok.isEmpty()) {
            StringBuilder sb = new StringBuilder("Informasi produk yang Anda cari:\n\n");
            for (Produk prod : cocok) {
                sb.append("\u2022 ").append(prod.getNamaProduk()).append("\n");
                sb.append("  ").append(prod.getHargaFormatted()).append("\n");
                sb.append("  Kandungan: ").append(prod.getKandungan()).append("\n\n");
            }
            return sb.toString().trim();
        }


        if (p.contains("rekomendasi") || p.contains("saran") || p.contains("rekomen")) {
            lastProdukResult = db.getAllProduk();
            return "Berikut rekomendasi produk skincare kami:\n\n" + cariSemuaProduk();
        }


        if (p.contains("produk") || p.contains("ada apa")
                || p.contains("semua") || p.contains("daftar")
                || p.contains("list") || p.contains("apa saja")) {
            lastProdukResult = db.getAllProduk();
            return cariSemuaProduk();
        }


        if (p.contains("harga") || p.contains("berapa") || p.contains("murah") || p.contains("mahal")) {
            lastProdukResult = db.getAllProduk();
            return cariSemuaProduk();
        }


        return "Maaf, saya belum memahami pertanyaan Anda \uD83D\uDE4F\n\n"
                + "Coba ketik salah satu:\n"
                + "\u2022 Jenis kulit: kulit kering, kulit berminyak, kulit berjerawat, kulit sensitif\n"
                + "\u2022 Kategori: toner, serum, pelembab, sunscreen, facial wash\n"
                + "\u2022 'rekomendasi produk' atau 'semua produk'\n"
                + "\u2022 Nama produk secara langsung";
    }



    public List<Produk> getLastProdukResult() {
        return lastProdukResult;
    }

    private boolean mengandungKategori(String pesan, String... keywords) {
        for (String kw : keywords) if (pesan.contains(kw)) return true;
        return false;
    }

    /** Cari produk berdasarkan jenis kulit */
    private List<Produk> getProdukByJenisKulit(String jenisKulit) {
        List<Produk> semua = db.getAllProduk();
        List<Produk> hasil = new ArrayList<>();
        for (Produk p : semua) {
            String jk = p.getJenisKulit();
            if (jk != null && (jk.equalsIgnoreCase(jenisKulit)
                    || jk.equalsIgnoreCase("Semua Jenis Kulit"))) {
                hasil.add(p);
            }
        }
        return hasil;
    }

    private String formatProdukJenisKulit(String jenisKulit, List<Produk> list) {
        if (list.isEmpty()) return "Tidak ada produk untuk " + jenisKulit + " saat ini.";
        StringBuilder sb = new StringBuilder(
                "Rekomendasi produk untuk \uD83C\uDF1F " + jenisKulit + ":\n\n");
        for (Produk prod : list) {
            sb.append("\u2022 ").append(prod.getNamaProduk()).append("\n");
            sb.append("  ").append(prod.getHargaFormatted()).append("\n");
            sb.append("  Kategori: ").append(prod.getKategori()).append("\n");
            sb.append("  Kandungan: ").append(prod.getKandungan()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String cariProdukByKategori(String kategori) {
        List<Produk> list = db.getProdukByKategori(kategori);
        if (list.isEmpty()) return "Tidak ada produk " + kategori + " tersedia saat ini.";
        StringBuilder sb = new StringBuilder(
                "Berikut produk " + kategori.toLowerCase() + " yang tersedia:\n\n");
        for (Produk prod : list) {
            sb.append("\u2022 ").append(prod.getNamaProduk()).append("\n");
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
            sb.append("\u2022 ").append(prod.getNamaProduk())
                    .append(" - ").append(prod.getHargaFormatted()).append("\n");
        }
        return sb.toString().trim();
    }

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

    public List<Produk> getProdukDariJawaban(String pesan, String jawaban) {
        String pesanLower = pesan.toLowerCase();

        if (mengandungKategori(pesanLower, "pelembab", "moisturizer", "lotion", "krim wajah"))
            return db.getProdukByKategori("Pelembab");
        if (mengandungKategori(pesanLower, "toner", "toning"))
            return db.getProdukByKategori("Toner");
        if (mengandungKategori(pesanLower, "serum"))
            return db.getProdukByKategori("Serum");
        if (mengandungKategori(pesanLower, "pembersih", "face wash", "sabun muka", "cleanser", "facial wash"))
            return db.getProdukByKategori("Facial Wash");
        if (mengandungKategori(pesanLower, "sunscreen", "spf", "tabir surya", "sun protection"))
            return db.getProdukByKategori("Sunscreen");

        if (pesanLower.contains("kulit kering") || pesanLower.contains("kering"))
            return getProdukByJenisKulit("Kulit Kering");
        if (pesanLower.contains("kulit berminyak") || pesanLower.contains("berminyak"))
            return getProdukByJenisKulit("Kulit Berminyak");
        if (pesanLower.contains("kulit berjerawat") || pesanLower.contains("jerawat"))
            return getProdukByJenisKulit("Kulit Berjerawat");
        if (pesanLower.contains("kulit sensitif") || pesanLower.contains("sensitif"))
            return getProdukByJenisKulit("Kulit Sensitif");

        if (pesanLower.contains("semua") || pesanLower.contains("produk")
                || pesanLower.contains("rekomendasi") || pesanLower.contains("daftar")
                || pesanLower.contains("harga") || pesanLower.contains("berapa"))
            return db.getAllProduk();

        return getProdukMentioned(pesan);
    }

    private String cekStatusToko() {
        String[] status = db.cekStatusTokoHariIni();
        String[] info   = db.getInfoToko();
        String namaToko = (info.length > 0 && info[0] != null && !info[0].isBlank())
                ? info[0] : "Toko";

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDFEA Status ").append(namaToko).append("\n\n");

        // FIX: pakai equalsIgnoreCase agar tidak case-sensitive
        if ("buka".equalsIgnoreCase(status[0])) {
            sb.append("✅ Toko sedang BUKA\n");
        } else if ("belum_buka".equalsIgnoreCase(status[0])) {
            sb.append("⏰ Toko belum buka\n");
        } else if ("sudah_tutup".equalsIgnoreCase(status[0])) {
            sb.append("🔒 Toko sudah tutup\n");
        } else {
            sb.append("❌ Toko sedang TUTUP\n");
        }

        if (status.length > 2 && status[2] != null) {
            sb.append("\uD83D\uDCCB ").append(status[2]).append("\n");
        }

        sb.append("\nKetik 'jam operasional' untuk jadwal lengkap semua hari.");
        return sb.toString();
    }

    private String infoLokasi() {
        String[] info = db.getInfoToko();
        if (info.length < 7 || (info[0] == null || info[0].isBlank()))
            return "Informasi lokasi belum tersedia. Silakan tanya admin untuk mengisi info toko.";
        return "\uD83D\uDCCD Lokasi " + info[0] + "\n\n"
                + "\uD83D\uDDFA " + info[3] + ", " + info[4] + " " + info[5] + "\n"
                + "\uD83D\uDD17 " + info[6];
    }

    public List<Produk> getAllProduk() {
        return db.getAllProduk();
    }
}