package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.util.ArrayList;
import java.util.List;

public class ChatbotService {

    private DatabaseService db = DatabaseService.getInstance();
    private List<Produk> lastProdukResult = null;

    public List<Produk> getLastProdukResult() {
        List<Produk> tmp = lastProdukResult;
        lastProdukResult = null;
        return tmp;
    }

    public String cariJawaban(String pesan) {
        String p = pesan.toLowerCase().trim();
        lastProdukResult = null;

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
                    + "\u2022 Tampilkan produk berdasarkan jenis kulit\n"
                    + "\u2022 Info deskripsi, kandungan, dan harga produk\n"
                    + "\u2022 Cek kecocokan produk untuk jenis kulit\n"
                    + "\u2022 Info lokasi & jam buka toko\n"
                    + "\u2022 Rekomendasi produk berdasarkan kategori\n"
                    + "\u2022 Cek ketersediaan stok produk\n\n"
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
            return cariProdukByJenisKulit("Kulit Sensitif");
        }
        if (p.contains("kulit berminyak") || p.contains("berminyak")) {
            return cariProdukByJenisKulit("Kulit Berminyak");
        }
        if (p.contains("kulit kering") || p.contains("kering")) {
            return cariProdukByJenisKulit("Kulit Kering");
        }
        if (p.contains("kulit berjerawat") || p.contains("berjerawat") || p.contains("jerawat")) {
            return cariProdukByJenisKulit("Kulit Berjerawat");
        }
        if (p.contains("kulit menua") || p.contains("anti aging") || p.contains("penuaan")) {
            return cariProdukByJenisKulit("Kulit Menua");
        }


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


        if (p.contains("produk") || p.contains("ada apa")
                || p.contains("semua") || p.contains("daftar")
                || p.contains("list") || p.contains("apa saja")) {
            return cariSemuaProduk();
        }


        if (p.contains("rekomendasi") || p.contains("saran")) {
            return cariSemuaProduk();
        }


        if (p.contains("harga") || p.contains("berapa") || p.contains("murah") || p.contains("mahal")) {
            return cariSemuaProduk();
        }


        if (p.contains("stok") || p.contains("tersedia") || p.contains("ada")) {
            return "Silakan sebutkan nama produk yang ingin Anda cek ketersediaannya.";
        }


        return "Maaf, saya belum memahami pertanyaan Anda \uD83D\uDE4F\n\n"
                + "Coba ketik salah satu:\n"
                + "\u2022 Nama kategori: toner, serum, pelembab, sunscreen\n"
                + "\u2022 Jenis kulit: kulit sensitif, kulit berminyak, dll\n"
                + "\u2022 'lokasi toko' atau 'jam buka'\n"
                + "\u2022 Nama produk secara langsung";
    }

    private boolean mengandungKategori(String pesan, String... keywords) {
        for (String kw : keywords) {
            if (pesan.contains(kw)) return true;
        }
        return false;
    }

    private String cariProdukByKategori(String kategori) {
        List<Produk> list = db.getProdukByKategori(kategori);
        if (list.isEmpty()) return "Tidak ada produk " + kategori + " tersedia saat ini.";
        lastProdukResult = list;
        return "Berikut produk " + kategori.toLowerCase() + " yang tersedia:";
    }

    private String cariProdukByJenisKulit(String jenisKulit) {
        List<Produk> all = db.getAllProduk();
        List<Produk> result = new ArrayList<>();
        for (Produk p : all) {
            String jk = p.getJenisKulit();
            if (jk != null && (jk.equalsIgnoreCase(jenisKulit) || jk.equalsIgnoreCase("Semua Jenis Kulit"))) {
                result.add(p);
            }
        }
        if (result.isEmpty()) return "Tidak ada produk untuk " + jenisKulit + " saat ini.";
        lastProdukResult = result;
        return "Rekomendasi produk untuk " + jenisKulit + ":";
    }

    private String cariSemuaProduk() {
        List<Produk> list = db.getAllProduk();
        if (list.isEmpty()) return "Belum ada produk tersedia.";
        lastProdukResult = list;
        return "Berikut semua produk yang tersedia:";
    }

    public List<Produk> getProdukMentioned(String pesan) {
        String pesanLower = pesan.toLowerCase();
        List<Produk> result = new ArrayList<>();
        for (Produk prod : db.getAllProduk()) {
            if (pesanLower.contains(prod.getNamaProduk().toLowerCase())) {
                result.add(prod);
            }
        }
        return result;
    }
}
