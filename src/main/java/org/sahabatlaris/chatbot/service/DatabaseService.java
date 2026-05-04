package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * DatabaseService - file-based (tab-separated) storage.
 * Kolom PRODUK: kode|nama|kategori|harga|kandungan|aktif|jenisKulit|areaTubuh|gambarUrl
 */
public class DatabaseService {

    private static DatabaseService instance;
    private static final String DB_FILE = "sahabatlaris.db";

    private final List<Produk> produkList      = new ArrayList<>();
    private String[]           infoToko        = new String[]{"","","","","","",""};
    private final List<String[]> jamOperasional = new ArrayList<>();
    private final List<String[]> intentList     = new ArrayList<>();

    private DatabaseService() {
        loadFromFile();
        if (produkList.isEmpty())      initDataDefault();
        if (jamOperasional.isEmpty())  initJamDefault();
        if (intentList.isEmpty())      initIntentDefault();
        saveToFile();
    }

    public static DatabaseService getInstance() {
        if (instance == null) instance = new DatabaseService();
        return instance;
    }

    // ═══════════════ FILE I/O ═══════════════

    private synchronized void loadFromFile() {
        File f = new File(DB_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String section = "";
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length() - 1);
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split("\t", -1);
                switch (section) {
                    case "PRODUK" -> {
                        // Support format lama (8 col) dan baru (9 col dengan areaTubuh)
                        if (cols.length >= 8) {
                            String areaTubuh = cols.length >= 9 ? cols[8] : "Muka";
                            produkList.add(new Produk(
                                    cols[0], cols[1], cols[2],
                                    parseLong(cols[3]),
                                    cols[4], "1".equals(cols[5]),
                                    cols[6], areaTubuh, cols[7]
                            ));
                        }
                    }
                    case "INFO_TOKO" -> {
                        if (cols.length >= 7) infoToko = cols;
                    }
                    case "JAM_OPERASIONAL" -> {
                        if (cols.length >= 4) jamOperasional.add(cols);
                    }
                    case "INTENT" -> {
                        if (cols.length >= 5) intentList.add(cols);
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public synchronized void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(DB_FILE), StandardCharsets.UTF_8))) {
            pw.println("[PRODUK]");
            for (Produk p : produkList) {
                // 9 kolom: kode|nama|kat|harga|kandungan|aktif|jenisKulit|areaTubuh|gambarUrl
                pw.println(tab(p.getKodeProduk(), p.getNamaProduk(), p.getKategori(),
                        String.valueOf(p.getHarga()), p.getKandungan(),
                        p.isAktif() ? "1" : "0",
                        p.getJenisKulit(),
                        p.getAreaTubuh() != null ? p.getAreaTubuh() : "Muka",
                        p.getGambarUrl()));
            }
            pw.println();
            pw.println("[INFO_TOKO]");
            pw.println(tab(infoToko));
            pw.println();
            pw.println("[JAM_OPERASIONAL]");
            for (String[] j : jamOperasional) pw.println(tab(j));
            pw.println();
            pw.println("[INTENT]");
            for (String[] it : intentList) pw.println(tab(it));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String tab(String... cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append('\t');
            sb.append(cols[i] != null ? cols[i].replace("\t", " ").replace("\n", " ") : "");
        }
        return sb.toString();
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0; }
    }

    // ═══════════════ DEFAULT DATA ═══════════════

    private void initDataDefault() {
        // kode, nama, kategori, harga, kandungan, aktif(1), jenisKulit, areaTubuh, gambarUrl
        Object[][] data = {
                {"P001","Gentle Glow Moisturizer","Pelembab",48000,"Ceramide, Aloe Vera",1,"Kulit Sensitif","Muka",""},
                {"P002","Soothing Toner","Toner",65000,"Witch Hazel, Green Tea",1,"Kulit Sensitif","Muka",""},
                {"P003","Barrier Repair Serum","Serum",120000,"Niacinamide, Panthenol",1,"Kulit Sensitif","Muka",""},
                {"P004","Calming Face Wash","Pembersih Muka",55000,"Centella, Panthenol",1,"Kulit Sensitif","Muka",""},
                {"P005","Acnaway Mugwort Water","Pelembab",38000,"Mugwort, Centella, Panthenol",1,"Kulit Berjerawat","Muka",""},
                {"P006","Somethinc Holysnail Gel SPF 50+","Chemical Sunscreen",48000,"UV Filter, Niacinamide, Vitamin E",1,"Semua Jenis Kulit","Muka",""},
                {"P007","Wardah Hydra Rose Toner","Toner",35000,"Rose Water, Hyaluronic Acid",1,"Kulit Kering","Muka",""},
                {"P008","Wardah Instaperfect Face Wash","Pembersih Muka",42000,"Vitamin C, Pearl Extract",1,"Semua Jenis Kulit","Muka",""},
                {"P009","Erha Ultimate Moisturizer","Pelembab",150000,"Ceramide, Glycerin, Shea Butter",1,"Kulit Kering","Muka",""},
                {"P010","Somethinc Calm Down Toner","Toner",89000,"Centella, Niacinamide, Panthenol",1,"Kulit Sensitif","Muka",""},
                {"P011","Glad2Glow Centella Gel Moisturizer","Pelembab",79000,"Centella Asiatica, Allantoin, Aloe Vera",1,"Kulit Berjerawat","Muka",""},
                {"P012","The Ordinary Niacinamide 10%","Serum",145000,"Niacinamide 10%, Zinc 1%",1,"Kulit Berminyak","Muka",""},
                {"P013","Emina Sun Protection SPF 30","Chemical Sunscreen",38000,"Titanium Dioxide, Aloe Vera",1,"Kulit Sensitif","Muka",""},
                {"P014","Hanasui Brightening Serum","Serum",55000,"Vitamin C, Niacinamide, Kojic Acid",1,"Semua Jenis Kulit","Muka",""},
                {"P015","Ms Glow Acne Series Face Wash","Pembersih Muka",65000,"Salicylic Acid, Tea Tree, Zinc",1,"Kulit Berjerawat","Muka",""},
                {"P016","Skintific Mugwort Pore Toner","Toner",95000,"Mugwort, BHA, Centella",1,"Kulit Berminyak","Muka",""},
                {"P017","Cetaphil Gentle Skin Cleanser","Pembersih Muka",120000,"Glycerin, Niacinamide",1,"Kulit Sensitif","Muka",""},
                {"P018","Avoskin Miraculous Retinol Serum","Serum",199000,"Retinol 0.5%, Bakuchiol, Peptide",1,"Kulit Menua","Muka",""},
                {"P019","Dear Me Beauty Sunscreen SPF50","Chemical Sunscreen",89000,"Zinc Oxide, Hyaluronic Acid, Vitamin E",1,"Kulit Kering","Muka",""},
                {"P020","Azarine Hydrasoothe Sunscreen SPF45","Chemical Sunscreen",65000,"Centella, Hyaluronic Acid",1,"Semua Jenis Kulit","Muka",""},
                {"P021","Ertos Acne Spot Gel","Serum",45000,"Salicylic Acid, Tea Tree Oil",1,"Kulit Berjerawat","Muka",""},
                {"P022","Scarlett Whitening Body Lotion","Pelembab",75000,"Glutathione, Vitamin C, Collagen",1,"Semua Jenis Kulit","Badan",""},
                {"P023","COSRX AHA/BHA Clarifying Toner","Toner",165000,"AHA, BHA, Apple Water",1,"Kulit Berminyak","Muka",""},
                {"P024","Innisfree Green Tea Seed Serum","Serum",320000,"Green Tea Extract, Hyaluronic Acid",1,"Kulit Kering","Muka",""},
                {"P025","Bioderma Sensibio H2O Micellar","Pembersih Muka",185000,"Micellar Water, Cucumber Extract",1,"Kulit Sensitif","Muka",""},
                {"P026","Pixy UV Whitening Sunscreen SPF33","Chemical Sunscreen",38000,"SPF 33, Vitamin B3",1,"Semua Jenis Kulit","Muka",""},
                {"P027","Senka Perfect Whip Cleanser","Pembersih Muka",75000,"Hyaluronic Acid, Silk Essence",1,"Kulit Normal","Muka",""},
                {"P028","SKII Facial Treatment Essence","Toner",1850000,"Pitera (Galactomyces), Niacinamide",1,"Semua Jenis Kulit","Muka",""},
                {"P029","Ponds Age Miracle Day Cream","Pelembab",125000,"Retinol-C Complex, SPF 18",1,"Kulit Menua","Muka",""},
                {"P030","Olay Regenerist Micro Serum","Serum",245000,"Amino-Peptide Complex, Niacinamide",1,"Kulit Menua","Muka",""},
                {"P031","Garnier Sakura White Toner","Toner",55000,"Sakura Extract, Vitamin C",1,"Semua Jenis Kulit","Muka",""},
                {"P032","LOreal Revitalift Laser Serum","Serum",289000,"Retinol Pure, Hyaluronic Acid",1,"Kulit Menua","Muka",""},
                {"P033","Neutrogena Ultra Sheer Sunscreen","Chemical Sunscreen",120000,"Helioplex, SPF 50+",1,"Kulit Normal","Muka",""},
                {"P034","Hada Labo Gokujyun Lotion","Toner",125000,"5 Types Hyaluronic Acid",1,"Kulit Kering","Muka",""},
                {"P035","Vivo Essence Renewal Cream","Pelembab",95000,"EGF, Collagen, Ceramide",1,"Kulit Menua","Muka",""},
                {"P036","Lacoco Exfoliating Toner","Toner",85000,"Lemon Extract, AHA 5%",1,"Kulit Berminyak","Muka",""},
                {"P037","Implora Green Tea Face Wash","Pembersih Muka",25000,"Green Tea Extract, Zinc",1,"Kulit Berminyak","Muka",""},
                {"P038","Wardah Hydrating Aloe Toner","Toner",42000,"Aloe Vera 95%, Hyaluronic Acid",1,"Kulit Kering","Muka",""},
                {"P039","Skintific 5X Ceramide Moisturizer","Pelembab",129000,"5 Types Ceramide, Oat Extract",1,"Kulit Sensitif","Muka",""},
                {"P040","Somethinc Niacinamide 10% Serum","Serum",99000,"Niacinamide 10%, Hyaluronic Acid",1,"Kulit Berminyak","Muka",""},
                {"P041","Wardah Spotless White Day Cream","Pelembab",55000,"Vitamin C, Niacinamide, SPF 28",1,"Kulit Normal","Muka",""},
                {"P042","YOU Skin Glow Face Wash","Pembersih Muka",35000,"Vitamin C, Papaya Extract",1,"Semua Jenis Kulit","Muka",""},
                {"P043","Nivea Luminous 630 Serum","Serum",175000,"Luminous 630, Hyaluronic Acid",1,"Kulit Menua","Muka",""},
                {"P044","Esqa Aqua Boost Sunscreen SPF50","Chemical Sunscreen",139000,"Hyaluronic Acid, Vitamin E",1,"Kulit Kering","Muka",""},
                {"P045","Caudalie Vinopure Serum","Serum",450000,"Polyphenols, Salicylic Acid, Niacinamide",1,"Kulit Berminyak","Muka",""},
                {"P046","Madame Gie Soothing Toner","Toner",29000,"Aloe Vera, Cucumber Extract",1,"Kulit Sensitif","Muka",""},
                {"P047","Biore UV Aqua Rich Watery Gel","Chemical Sunscreen",115000,"UV Filter, Hyaluronic Acid, SPF 50+",1,"Kulit Normal","Muka",""},
                {"P048","Elshe Brightening Face Wash","Pembersih Muka",48000,"Glutathione, Vitamin C",1,"Semua Jenis Kulit","Muka",""},
                {"P049","Inez Coloring Lip Balm","Pelembab",55000,"Vitamin E, Shea Butter",1,"Semua Jenis Kulit","Bibir",""},
                {"P050","Garnier Men Acno Fight Face Wash","Pembersih Muka",32000,"Salicylic Acid, Charcoal, Zinc",1,"Kulit Berjerawat","Muka",""},
        };
        for (Object[] row : data) {
            produkList.add(new Produk(
                    (String)row[0], (String)row[1], (String)row[2],
                    ((Number)row[3]).longValue(),
                    (String)row[4], ((Number)row[5]).intValue() == 1,
                    (String)row[6], (String)row[7], (String)row[8]
            ));
        }

        infoToko = new String[]{
                "Toko kosmetik Mutiara",
                "Glow up aggak harus mahal",
                "Mutiara Kosmetik adalah salah satu toko ritel produk kecantikan terpopuler dan terlengkap di Yogyakarta.",
                "Jl. Dokter Sutomo No.64 A, Baciro, Kec. Gondokusuman, Kota Yogyakarta",
                "Yogyakarta", "55211",
                "https://maps.google.com/?q=Mutiara+Kosmetik+Yogyakarta"
        };
    }

    private void initJamDefault() {
        String[] hari = {"Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu"};
        for (String h : hari) jamOperasional.add(new String[]{h, "1", "09.00", "21.00"});
    }

    private void initIntentDefault() {
        intentList.add(new String[]{"Produk","Tampilkan produk skincer untuk <<Kategori>>","<<Kategori>>","Tampilkan produk untuk kulit sensitif","kulit sensitif"});
        intentList.add(new String[]{"Informasi produk","Tampilkan deskripsi, kandungan, dan harga untuk <<produk>>","<<produk>>","Tampilkan deskripsi kandungan dan harga untuk Moisturizer","Moisturizer"});
        intentList.add(new String[]{"Kecocokan produk","Apakah <<produk>> cocok untuk kulit sensitif?","<<produk>>","Apakah Wardah Hydra Rose cocok untuk kulit sensitif?","Wardah Hydra Rose"});
        intentList.add(new String[]{"Lokasi toko","Di mana lokasi toko di daerah <<info_toko>>?","<<info_toko>>","Dimana lokasi toko di daerah Sleman","Sleman"});
        intentList.add(new String[]{"Peta navigasi","Tampilkan link maps untuk lokasi <<info_toko>>","<<info_toko>>","Tampilkan link maps untuk lokasi Tugu","Tugu"});
        intentList.add(new String[]{"Jam operasional","Tampilkan jam buka dan tutup toko untuk <<info_toko>>","<<info_toko>>","Tampilkan jam buka dan tutup toko untuk Toko Mutiara","Toko Mutiara"});
        intentList.add(new String[]{"Rekomendasi","Tampilkan semua produk untuk kategori <<kategori>>","<<kategori>>","Tampilkan semua produk untuk kategori Sabun Wajah","Sabun Wajah"});
        intentList.add(new String[]{"Cek stok","Apakah <<produk>> masih tersedia?","<<produk>>","Apakah Somethinc Calm Down masih tersedia?","Somethinc Calm Down"});
    }

    // ═══════════════ PRODUK CRUD ═══════════════

    public List<Produk> getAllProduk()  { return new ArrayList<>(produkList); }

    public List<Produk> getProdukByKategori(String kategori) {
        if (kategori == null || kategori.equals("Semua Kategori")) return getAllProduk();
        List<Produk> result = new ArrayList<>();
        for (Produk p : produkList)
            if (p.getKategori().equalsIgnoreCase(kategori)) result.add(p);
        return result;
    }

    public List<Produk> getProdukByJenisKulit(String jenisKulit) {
        List<Produk> result = new ArrayList<>();
        for (Produk p : produkList) {
            String jk = p.getJenisKulit();
            if (jk != null && (jk.equalsIgnoreCase(jenisKulit) || jk.equalsIgnoreCase("Semua Jenis Kulit")))
                result.add(p);
        }
        return result;
    }

    public void tambahProduk(Produk p) { produkList.add(p); saveToFile(); }

    public void updateProduk(Produk p) {
        for (int i = 0; i < produkList.size(); i++) {
            if (produkList.get(i).getKodeProduk().equals(p.getKodeProduk())) {
                produkList.set(i, p); saveToFile(); return;
            }
        }
    }

    public void hapusProduk(String kodeProduk) {
        produkList.removeIf(p -> p.getKodeProduk().equals(kodeProduk));
        saveToFile();
    }

    public List<String> getAllKategori() {
        List<String> cats = new ArrayList<>();
        cats.add("Semua Kategori");
        for (Produk p : produkList)
            if (!cats.contains(p.getKategori())) cats.add(p.getKategori());
        return cats;
    }

    public String generateKodeProduk() {
        return "P" + String.format("%03d", produkList.size() + 1);
    }

    // ═══════════════ INFO TOKO ═══════════════

    public String[] getInfoToko() { return infoToko.clone(); }

    public void simpanInfoToko(String nama, String tagline, String deskripsi,
                               String alamat, String kota, String kodePos, String linkPeta) {
        infoToko = new String[]{nama, tagline, deskripsi, alamat, kota, kodePos, linkPeta};
        saveToFile();
    }

    // ═══════════════ JAM OPERASIONAL ═══════════════

    public List<String[]> getJamOperasional() {
        List<String[]> copy = new ArrayList<>();
        for (String[] j : jamOperasional) copy.add(j.clone());
        return copy;
    }

    public void simpanJamOperasional(String hari, int buka, String jamBuka, String jamTutup) {
        for (String[] j : jamOperasional) {
            if (j[0].equalsIgnoreCase(hari)) {
                j[1] = String.valueOf(buka); j[2] = jamBuka; j[3] = jamTutup;
                saveToFile(); return;
            }
        }
    }
}
