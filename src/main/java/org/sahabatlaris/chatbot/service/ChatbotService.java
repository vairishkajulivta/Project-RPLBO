package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        // ── Filter harga DULU sebelum cek keyword umum ───────────────────────
        Long maxHarga = deteksiMaxHarga(p);
        Long minHarga = deteksiMinHarga(p);
        if (maxHarga != null || minHarga != null) {
            List<Produk> hasil = filterByHarga(db.getAllProduk(), minHarga, maxHarga);
            lastProdukResult = hasil;
            if (hasil.isEmpty()) return "Tidak ada produk dengan rentang harga tersebut.";
            String label = maxHarga != null && minHarga != null
                    ? "Produk harga Rp " + minHarga / 1000 + "rb - Rp " + maxHarga / 1000 + "rb"
                    : maxHarga != null
                      ? "Produk di bawah Rp " + maxHarga / 1000 + "rb"
                      : "Produk di atas Rp " + minHarga / 1000 + "rb";
            return label + ":";
        }

        // ── Deteksi kategori dari pesan ──────────────────────────────────────
        String kategoriTerdeteksi = deteksiKategori(p);

        // ── Deteksi jenis kulit dari pesan ───────────────────────────────────
        String jenisKulitTerdeteksi = deteksiJenisKulit(p);

        // ── Deteksi area tubuh dari pesan ────────────────────────────────────
        String areaTubuhTerdeteksi = deteksiAreaTubuh(p);

        // ── KOMBINASI: kategori + jenis kulit ────────────────────────────────
        if (kategoriTerdeteksi != null && jenisKulitTerdeteksi != null) {
            List<Produk> hasil = getProdukByKategoriDanJenisKulit(kategoriTerdeteksi, jenisKulitTerdeteksi);
            lastProdukResult = hasil;
            return formatProdukKombinasiKulit(kategoriTerdeteksi, jenisKulitTerdeteksi, hasil);
        }

        // ── Hanya jenis kulit ─────────────────────────────────────────────────
        if (jenisKulitTerdeteksi != null) {
            lastProdukResult = getProdukByJenisKulitDanArea(jenisKulitTerdeteksi, areaTubuhTerdeteksi);
            return formatProdukJenisKulit(jenisKulitTerdeteksi, lastProdukResult);
        }

        // ── KOMBINASI: kategori + area tubuh ─────────────────────────────────
        if (kategoriTerdeteksi != null && areaTubuhTerdeteksi != null) {
            List<Produk> hasil = getProdukByKategoriDanArea(kategoriTerdeteksi, areaTubuhTerdeteksi);
            lastProdukResult = hasil;
            return formatProdukKombinasiArea(kategoriTerdeteksi, areaTubuhTerdeteksi, hasil);
        }

        // ── Hanya kategori ────────────────────────────────────────────────────
        if (kategoriTerdeteksi != null) {
            lastProdukResult = db.getProdukByKategori(kategoriTerdeteksi);
            return cariProdukByKategori(kategoriTerdeteksi);
        }

        // ── Nama produk spesifik ──────────────────────────────────────────────
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

        // ── Rekomendasi umum ──────────────────────────────────────────────────
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
                + "\u2022 Nama produk secara langsung\n"
                + "\u2022 Filter harga: 'produk di bawah 40rb', 'produk di atas 50rb'";
    }

    private String deteksiKategori(String p) {
        if (mengandungKategori(p, "pelembab", "moisturizer", "krim wajah")) return "Pelembab";
        if (mengandungKategori(p, "toner", "toning"))                        return "Toner";
        if (mengandungKategori(p, "serum"))                                  return "Serum";
        if (mengandungKategori(p, "pembersih", "face wash", "sabun muka",
                "cleanser", "facial wash", "sabun wajah"))                   return "Facial Wash";
        if (mengandungKategori(p, "sunscreen", "spf",
                "tabir surya", "sun protection"))                             return "Sunscreen";
        if (mengandungKategori(p, "exfoliat", "scrub", "aha", "bha", "exfo")) return "Exfoliator";
        if (mengandungKategori(p, "body care", "lotion badan",
                "badan", "tubuh", "body"))                                   return "Body Care";
        if (mengandungKategori(p, "eye care", "eye cream", "mata"))          return "Eye Care";
        if (mengandungKategori(p, "lip care", "lip balm", "lip", "bibir"))   return "Lip Care";
        if (mengandungKategori(p, "hair care", "shampoo", "rambut"))         return "Hair Care";
        if (mengandungKategori(p, "hand care", "tangan", "kaki"))            return "Hand Care";
        if (mengandungKategori(p, "acne care", "acne patch", "patch"))       return "Acne Care";
        if (p.contains("lotion") && !p.contains("badan")
                && !p.contains("tubuh"))                                     return "Pelembab";
        return null;
    }

    private String deteksiJenisKulit(String p) {
        if (p.contains("kulit kering")     || p.contains("kering"))         return "Kulit Kering";
        if (p.contains("kulit berminyak")  || p.contains("berminyak"))      return "Kulit Berminyak";
        if (p.contains("kulit berjerawat") || p.contains("berjerawat")
                || p.contains("jerawat"))                                    return "Kulit Berjerawat";
        if (p.contains("kulit sensitif")   || p.contains("sensitif"))       return "Kulit Sensitif";
        if (p.contains("kulit normal")     || p.contains("normal"))         return "Kulit Normal";
        if (p.contains("kulit menua")      || p.contains("menua")
                || p.contains("anti aging") || p.contains("penuaan"))       return "Kulit Menua";
        if (p.contains("semua jenis kulit")
                || (p.contains("semua") && p.contains("kulit")))            return "Semua Jenis Kulit";
        return null;
    }

    private String deteksiAreaTubuh(String p) {
        if (p.contains("badan") || p.contains("tubuh") || p.contains("body")) return "Badan";
        if (p.contains("mata"))                                               return "Mata";
        if (p.contains("bibir"))                                              return "Bibir";
        if (p.contains("rambut"))                                             return "Rambut";
        if (p.contains("tangan") || p.contains("kaki"))                      return "Tangan";
        if (p.contains("muka") || p.contains("wajah"))                       return "Muka";
        return null;
    }

    private Long deteksiMaxHarga(String p) {
        java.util.regex.Pattern[] patterns = {
                java.util.regex.Pattern.compile(
                        "(?:di ?bawah|kurang dari|max|maksimal|under|<=?)\\s*(\\d+)\\s*(?:rb|ribu|k)?"),
                java.util.regex.Pattern.compile(
                        "(?:harga|price)\\s*(?:di ?bawah|<)\\s*(\\d+)\\s*(?:rb|ribu|k)?")
        };
        for (java.util.regex.Pattern pat : patterns) {
            java.util.regex.Matcher m = pat.matcher(p);
            if (m.find()) {
                long val = Long.parseLong(m.group(1));
                if (val < 10000) val *= 1000; // "40" → 40000
                return val;
            }
        }
        return null;
    }

    private Long deteksiMinHarga(String p) {
        java.util.regex.Pattern[] patterns = {
                java.util.regex.Pattern.compile(
                        "(?:di ?atas|lebih dari|min|minimal|above|>=?)\\s*(\\d+)\\s*(?:rb|ribu|k)?"),
                java.util.regex.Pattern.compile(
                        "(?:harga|price)\\s*(?:di ?atas|>)\\s*(\\d+)\\s*(?:rb|ribu|k)?")
        };
        for (java.util.regex.Pattern pat : patterns) {
            java.util.regex.Matcher m = pat.matcher(p);
            if (m.find()) {
                long val = Long.parseLong(m.group(1));
                if (val < 10000) val *= 1000;
                return val;
            }
        }
        return null;
    }

    private List<Produk> filterByHarga(List<Produk> semua, Long minHarga, Long maxHarga) {
        List<Produk> hasil = new ArrayList<>();
        for (Produk prod : semua) {
            try {
                long harga = prod.getHarga(); // sesuaikan jika nama method berbeda
                boolean lolos = true;
                if (minHarga != null && harga < minHarga) lolos = false;
                if (maxHarga != null && harga > maxHarga) lolos = false;
                if (lolos) hasil.add(prod);
            } catch (Exception ignored) {}
        }
        return hasil;
    }

    private List<Produk> getProdukByKategoriDanJenisKulit(String kategori, String jenisKulit) {
        return getProdukByKategoriDanJenisKulitDanArea(kategori, jenisKulit, null);
    }

    private static final java.util.Set<String> KATEGORI_NON_MUKA =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "Body Care", "Hair Care", "Lip Care", "Eye Care", "Hand Care"));

    private List<Produk> getProdukByKategoriDanJenisKulitDanArea(
            String kategori, String jenisKulit, String areaTubuh) {
        List<Produk> byKategori = db.getProdukByKategori(kategori);
        List<Produk> hasil = new ArrayList<>();

        boolean filterMuka = !KATEGORI_NON_MUKA.contains(kategori)
                && (areaTubuh == null
                || areaTubuh.equalsIgnoreCase("Muka")
                || areaTubuh.equalsIgnoreCase("Wajah"));

        for (Produk prod : byKategori) {
            String jk = prod.getJenisKulit();
            boolean cocokKulit = jk != null && (jk.equalsIgnoreCase(jenisKulit)
                    || jk.equalsIgnoreCase("Semua Jenis Kulit"));
            if (!cocokKulit) continue;

            String at = prod.getAreaTubuh() != null ? prod.getAreaTubuh().toLowerCase() : "";
            boolean cocokArea;
            if (filterMuka) {
                cocokArea = at.contains("muka") || at.contains("wajah");
            } else if (areaTubuh != null) {
                cocokArea = at.contains(areaTubuh.toLowerCase());
            } else {
                cocokArea = true;
            }
            if (cocokArea) hasil.add(prod);
        }
        return hasil;
    }

    private List<Produk> getProdukByKategoriDanArea(String kategori, String areaTubuh) {
        List<Produk> byKategori = db.getProdukByKategori(kategori);
        List<Produk> hasil = new ArrayList<>();
        for (Produk prod : byKategori) {
            String at = prod.getAreaTubuh();
            if (at != null && at.toLowerCase().contains(areaTubuh.toLowerCase()))
                hasil.add(prod);
        }
        return hasil;
    }

    private List<Produk> getProdukByJenisKulit(String jenisKulit) {
        return getProdukByJenisKulitDanArea(jenisKulit, null);
    }

    private List<Produk> getProdukByJenisKulitDanArea(String jenisKulit, String areaTubuh) {
        List<Produk> semua = db.getAllProduk();
        List<Produk> hasil = new ArrayList<>();

        boolean filterMuka = (areaTubuh == null
                || areaTubuh.equalsIgnoreCase("Muka")
                || areaTubuh.equalsIgnoreCase("Wajah"));

        for (Produk p : semua) {
            String jk = p.getJenisKulit();
            boolean cocokKulit = jk != null && (jk.equalsIgnoreCase(jenisKulit)
                    || jk.equalsIgnoreCase("Semua Jenis Kulit"));
            if (!cocokKulit) continue;

            if (areaTubuh == null) {
                // Tidak ada area disebut → tampilkan SEMUA area
                hasil.add(p);
            } else {
                String at = p.getAreaTubuh() != null ? p.getAreaTubuh().toLowerCase() : "";
                boolean cocokArea;
                if (filterMuka) {
                    cocokArea = at.contains("muka") || at.contains("wajah");
                } else {
                    cocokArea = at.contains(areaTubuh.toLowerCase());
                }
                if (cocokArea) hasil.add(p);
            }
        }
        return hasil;
    }

    private String formatProdukKombinasiKulit(String kategori, String jenisKulit, List<Produk> list) {
        if (list.isEmpty())
            return "Tidak ada produk " + kategori + " untuk " + jenisKulit + " saat ini.";
        StringBuilder sb = new StringBuilder(
                "Rekomendasi \uD83C\uDF1F " + kategori + " untuk " + jenisKulit + ":\n\n");
        for (Produk prod : list) {
            sb.append("\u2022 ").append(prod.getNamaProduk()).append("\n");
            sb.append("  ").append(prod.getHargaFormatted()).append("\n");
            sb.append("  Kandungan: ").append(prod.getKandungan()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String formatProdukKombinasiArea(String kategori, String areaTubuh, List<Produk> list) {
        if (list.isEmpty())
            return "Tidak ada produk " + kategori + " untuk area " + areaTubuh + " saat ini.";
        StringBuilder sb = new StringBuilder(
                "Rekomendasi \uD83D\uDCA1 " + kategori + " untuk " + areaTubuh + ":\n\n");
        for (Produk prod : list) {
            sb.append("\u2022 ").append(prod.getNamaProduk()).append("\n");
            sb.append("  ").append(prod.getHargaFormatted()).append("\n");
            sb.append("  Kandungan: ").append(prod.getKandungan()).append("\n\n");
        }
        return sb.toString().trim();
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

    public List<Produk> getLastProdukResult() { return lastProdukResult; }

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

        Long maxHarga = deteksiMaxHarga(pesanLower);
        Long minHarga = deteksiMinHarga(pesanLower);
        if (maxHarga != null || minHarga != null)
            return filterByHarga(db.getAllProduk(), minHarga, maxHarga);

        String kategori   = deteksiKategori(pesanLower);
        String jenisKulit = deteksiJenisKulit(pesanLower);
        String areaTubuh  = deteksiAreaTubuh(pesanLower);

        if (kategori != null && jenisKulit != null)
            return getProdukByKategoriDanJenisKulit(kategori, jenisKulit);
        if (kategori != null && areaTubuh != null)
            return getProdukByKategoriDanArea(kategori, areaTubuh);
        if (kategori != null)
            return db.getProdukByKategori(kategori);
        if (jenisKulit != null)
            return getProdukByJenisKulit(jenisKulit);

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

        if ("buka".equalsIgnoreCase(status[0])) {
            sb.append("\u2705 Toko sedang BUKA\n");
        } else if ("belum_buka".equalsIgnoreCase(status[0])) {
            sb.append("\u23F0 Toko belum buka\n");
        } else if ("sudah_tutup".equalsIgnoreCase(status[0])) {
            sb.append("\uD83D\uDD12 Toko sudah tutup\n");
        } else {
            sb.append("\u274C Toko sedang TUTUP\n");
        }

        if (status.length > 2 && status[2] != null)
            sb.append("\uD83D\uDCCB ").append(status[2]).append("\n");

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

    public List<Produk> getAllProduk() { return db.getAllProduk(); }
}