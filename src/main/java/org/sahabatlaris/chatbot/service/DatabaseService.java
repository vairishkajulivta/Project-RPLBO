package org.sahabatlaris.chatbot.service;

import org.sahabatlaris.chatbot.model.Produk;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static DatabaseService instance;
    private static final String URL = "jdbc:sqlite:D:/SahabatLaris/sahabatlaris.db";

    private DatabaseService() {
        try {
            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) instance = new DatabaseService();
        return instance;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }


    private void initDatabase() {
        String createProduk =
                "CREATE TABLE IF NOT EXISTS produk (" +
                        "id              INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "kode_produk     TEXT UNIQUE, " +
                        "nama_produk     TEXT NOT NULL, " +
                        "kategori        TEXT, " +
                        "harga           INTEGER, " +
                        "kandungan       TEXT, " +
                        "deskripsi       TEXT, " +
                        "jenis_kulit     TEXT, " +
                        "area_tubuh      TEXT, " +
                        "gambar_url      TEXT);";

        String createInfoToko =
                "CREATE TABLE IF NOT EXISTS info_toko (" +
                        "id          INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nama        TEXT, " +
                        "tagline     TEXT, " +
                        "deskripsi   TEXT, " +
                        "alamat      TEXT, " +
                        "kota        TEXT, " +
                        "kode_pos    TEXT, " +
                        "link_peta   TEXT);";

        String createJamOperasional =
                "CREATE TABLE IF NOT EXISTS jam_operasional (" +
                        "id          INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "hari        TEXT UNIQUE, " +
                        "buka        INTEGER DEFAULT 1, " +
                        "jam_buka    TEXT, " +
                        "jam_tutup   TEXT);";

        String createHariLibur =
                "CREATE TABLE IF NOT EXISTS hari_libur (" +
                        "id          INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "tanggal     TEXT UNIQUE NOT NULL, " +
                        "nama        TEXT, " +
                        "status      TEXT, " +
                        "keterangan  TEXT);";

        String createRiwayat =
                "CREATE TABLE IF NOT EXISTS riwayat_chat (" +
                        "id          INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "pesan       TEXT, " +
                        "balasan     TEXT, " +
                        "tag         TEXT, " +
                        "waktu       TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createProduk);
            stmt.execute(createInfoToko);
            stmt.execute(createJamOperasional);
            stmt.execute(createHariLibur);
            stmt.execute(createRiwayat);

            if (getAllProduk().isEmpty()) {
                isiDataAwal();
            }
            isiJamOperasionalDefault();
            isiInfoTokoDefault();
            isiHariLiburDefault();
            isiContohPertanyaanDefault();
            // Tambah kolom deskripsi jika belum ada (upgrade database lama)
            try { stmt.execute("ALTER TABLE produk ADD COLUMN deskripsi TEXT"); }
            catch (SQLException ignored) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void tambahProduk(Produk p) {
        String kode = (p.getKodeProduk() == null || p.getKodeProduk().isBlank())
                ? generateKodeProduk() : p.getKodeProduk();

        String sql = "INSERT INTO produk(kode_produk, nama_produk, kategori, harga, " +
                "kandungan, deskripsi, jenis_kulit, area_tubuh, gambar_url) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kode);
            pstmt.setString(2, p.getNamaProduk());
            pstmt.setString(3, p.getKategori());
            pstmt.setLong(4, p.getHarga());
            pstmt.setString(5, p.getKandungan());
            pstmt.setString(6, p.getDeskripsi() != null ? p.getDeskripsi() : "");
            pstmt.setString(7, p.getJenisKulit());
            pstmt.setString(8, p.getAreaTubuh());
            pstmt.setString(9, p.getGambarUrl());
            pstmt.executeUpdate();
            System.out.println("Berhasil menambah produk ke database SQLite!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProduk(Produk p) {
        String sql = "UPDATE produk SET nama_produk=?, kategori=?, harga=?, kandungan=?, " +
                "deskripsi=?, jenis_kulit=?, area_tubuh=?, gambar_url=? WHERE kode_produk=?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNamaProduk());
            pstmt.setString(2, p.getKategori());
            pstmt.setLong(3, p.getHarga());
            pstmt.setString(4, p.getKandungan());
            pstmt.setString(5, p.getDeskripsi() != null ? p.getDeskripsi() : "");
            pstmt.setString(6, p.getJenisKulit());
            pstmt.setString(7, p.getAreaTubuh());
            pstmt.setString(8, p.getGambarUrl());
            pstmt.setString(9, p.getKodeProduk());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void hapusProduk(String kodeProduk) {
        String sql = "DELETE FROM produk WHERE kode_produk = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kodeProduk);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Produk> getAllProduk() {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Produk p = new Produk(
                        rs.getString("kode_produk"),
                        rs.getString("nama_produk"),
                        rs.getString("kategori"),
                        rs.getLong("harga"),
                        rs.getString("kandungan"),
                        true,
                        rs.getString("jenis_kulit"),
                        rs.getString("area_tubuh"),
                        rs.getString("gambar_url")
                );
                try { p.setDeskripsi(rs.getString("deskripsi")); } catch (Exception ignored) {}
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Produk> getProdukByKategori(String kategori) {
        List<Produk> list = new ArrayList<>();
        String sql = "SELECT * FROM produk WHERE kategori = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kategori);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Produk pp = new Produk(
                        rs.getString("kode_produk"),
                        rs.getString("nama_produk"),
                        rs.getString("kategori"),
                        rs.getLong("harga"),
                        rs.getString("kandungan"),
                        true,
                        rs.getString("jenis_kulit"),
                        rs.getString("area_tubuh"),
                        rs.getString("gambar_url")
                );
                try { pp.setDeskripsi(rs.getString("deskripsi")); } catch (Exception ignored) {}
                list.add(pp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getAllKategori() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT kategori FROM produk ORDER BY kategori";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String k = rs.getString("kategori");
                if (k != null && !k.isBlank()) list.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String generateKodeProduk() {
        String sql = "SELECT COUNT(*) AS total FROM produk";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total") + 1;
                return String.format("P%03d", total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "P001";
    }


    public String[] getInfoToko() {
        String sql = "SELECT * FROM info_toko LIMIT 1";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new String[]{
                        rs.getString("nama"),
                        rs.getString("tagline"),
                        rs.getString("deskripsi"),
                        rs.getString("alamat"),
                        rs.getString("kota"),
                        rs.getString("kode_pos"),
                        rs.getString("link_peta")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[]{"", "", "", "", "", "", ""};
    }

    public void simpanInfoToko(String nama, String tagline, String deskripsi,
                               String alamat, String kota, String kodePos, String linkPeta) {
        try (Connection conn = this.connect()) {
            int jumlah = 0;
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM info_toko")) {
                if (rs.next()) jumlah = rs.getInt(1);
            }
            String sql = (jumlah == 0)
                    ? "INSERT INTO info_toko(nama,tagline,deskripsi,alamat,kota,kode_pos,link_peta) VALUES(?,?,?,?,?,?,?)"
                    : "UPDATE info_toko SET nama=?,tagline=?,deskripsi=?,alamat=?,kota=?,kode_pos=?,link_peta=? WHERE id=1";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nama);
                pstmt.setString(2, tagline);
                pstmt.setString(3, deskripsi);
                pstmt.setString(4, alamat);
                pstmt.setString(5, kota);
                pstmt.setString(6, kodePos);
                pstmt.setString(7, linkPeta);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void isiJamOperasionalDefault() {
        try (Connection conn = this.connect();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM jam_operasional")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        String[] hariList = {"Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu"};
        for (String hari : hariList) {
            int buka = hari.equals("Minggu") ? 0 : 1;
            String jamBuka  = hari.equals("Minggu") ? "" : "08:00";
            String jamTutup = hari.equals("Minggu") ? "" : "21:00";
            simpanJamOperasional(hari, buka, jamBuka, jamTutup);
        }
    }

    public List<String[]> getJamOperasional() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT hari, buka, jam_buka, jam_tutup FROM jam_operasional";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("hari"),
                        String.valueOf(rs.getInt("buka")),
                        rs.getString("jam_buka"),
                        rs.getString("jam_tutup")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void simpanJamOperasional(String hari, int buka, String jamBuka, String jamTutup) {
        String sql = "INSERT INTO jam_operasional(hari,buka,jam_buka,jam_tutup) VALUES(?,?,?,?) " +
                "ON CONFLICT(hari) DO UPDATE SET buka=excluded.buka," +
                "jam_buka=excluded.jam_buka,jam_tutup=excluded.jam_tutup";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hari);
            pstmt.setInt(2, buka);
            pstmt.setString(3, jamBuka);
            pstmt.setString(4, jamTutup);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<HariLibur> getAllHariLibur() {
        List<HariLibur> list = new ArrayList<>();
        String sql = "SELECT tanggal, nama, status, keterangan FROM hari_libur ORDER BY tanggal";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new HariLibur(
                        rs.getString("tanggal"),
                        rs.getString("nama"),
                        rs.getString("status"),
                        rs.getString("keterangan")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void tambahHariLibur(HariLibur hl) {
        String sql = "INSERT OR IGNORE INTO hari_libur(tanggal,nama,status,keterangan) VALUES(?,?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hl.getTanggal());
            pstmt.setString(2, hl.getNama());
            pstmt.setString(3, hl.getStatus());
            pstmt.setString(4, hl.getKeterangan());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void hapusHariLibur(String tanggal) {
        String sql = "DELETE FROM hari_libur WHERE tanggal = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tanggal);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String[] cekStatusTokoHariIni() {
        LocalDate hari = LocalDate.now();
        LocalTime sekarang = LocalTime.now();

        // Cek hari libur khusus
        for (HariLibur hl : getAllHariLibur()) {
            if (hl.cocokDengan(hari)) {
                return new String[]{"tutup", hl.getNama(), "-", "-"};
            }
        }

        // Nama hari (Bahasa Indonesia)
        String namaHari;
        switch (hari.getDayOfWeek()) {
            case MONDAY:    namaHari = "Senin";   break;
            case TUESDAY:   namaHari = "Selasa";  break;
            case WEDNESDAY: namaHari = "Rabu";    break;
            case THURSDAY:  namaHari = "Kamis";   break;
            case FRIDAY:    namaHari = "Jumat";   break;
            case SATURDAY:  namaHari = "Sabtu";   break;
            default:        namaHari = "Minggu";  break;
        }

        String sql = "SELECT buka, jam_buka, jam_tutup FROM jam_operasional WHERE hari = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, namaHari);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt("buka") == 0) {
                    return new String[]{"tutup", namaHari, "-", "-"};
                }
                String jamBuka  = rs.getString("jam_buka");
                String jamTutup = rs.getString("jam_tutup");
                if (jamBuka != null && !jamBuka.isBlank()) {
                    LocalTime tBuka  = LocalTime.parse(jamBuka);
                    LocalTime tTutup = LocalTime.parse(jamTutup != null ? jamTutup : "21:00");
                    if (sekarang.isBefore(tBuka)) {
                        return new String[]{"belum_buka", namaHari, jamBuka, jamTutup != null ? jamTutup : "21:00"};
                    } else if (sekarang.isAfter(tTutup)) {
                        return new String[]{"sudah_tutup", namaHari, jamBuka, jamTutup != null ? jamTutup : "21:00"};
                    } else {
                        return new String[]{"buka", namaHari, jamBuka, jamTutup != null ? jamTutup : "21:00"};
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[]{"buka", namaHari, "08:00", "21:00"};
    }


    public void tambahRiwayat(String pesan, String balasan, String tag) {
        String sql = "INSERT INTO riwayat_chat(pesan,balasan,tag) VALUES(?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pesan);
            pstmt.setString(2, balasan);
            pstmt.setString(3, tag);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getRiwayatTerakhir(int n) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT pesan, balasan, tag, waktu FROM riwayat_chat ORDER BY id DESC LIMIT ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, n);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("pesan"),
                        rs.getString("balasan"),
                        rs.getString("tag"),
                        rs.getString("waktu")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void hapusSemuaRiwayat() {
        String sql = "DELETE FROM riwayat_chat";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // DATA DEFAULT INFO TOKO & HARI LIBUR
    // =========================================================================
    private void isiInfoTokoDefault() {
        try (Connection conn = this.connect();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM info_toko")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) { return; }
        simpanInfoToko(
                "SahabatLaris",
                "Toko Skincare Terpercaya",
                "SahabatLaris adalah toko skincare terpercaya yang menyediakan berbagai produk perawatan kulit berkualitas dengan harga terjangkau.",
                "Jl. Malioboro No. 123",
                "Yogyakarta",
                "55271",
                "https://maps.google.com/?q=SahabatLaris+Yogyakarta"
        );
    }

    private void isiHariLiburDefault() {
        if (!getAllHariLibur().isEmpty()) return;
        HariLibur[] liburDefault = {
                new HariLibur("2026-01-01", "Tahun Baru Masehi",             "tutup", "Libur Nasional"),
                new HariLibur("2026-01-29", "Tahun Baru Imlek",              "tutup", "Libur Nasional"),
                new HariLibur("2026-03-20", "Isra Miraj",                    "tutup", "Libur Nasional"),
                new HariLibur("2026-03-31", "Idul Fitri 1447 H",             "tutup", "Libur Lebaran"),
                new HariLibur("2026-04-01", "Idul Fitri Hari ke-2",          "tutup", "Libur Lebaran"),
                new HariLibur("2026-04-03", "Cuti Bersama Idul Fitri",       "tutup", "Cuti Bersama"),
                new HariLibur("2026-05-01", "Hari Buruh Internasional",      "buka",  "Tetap buka normal"),
                new HariLibur("2026-05-14", "Kenaikan Isa Almasih",          "tutup", "Libur Nasional"),
                new HariLibur("2026-06-01", "Hari Pancasila",                "buka",  "Tetap buka normal"),
                new HariLibur("2026-08-17", "HUT Kemerdekaan RI",            "buka",  "Buka dengan promo kemerdekaan"),
                new HariLibur("2026-12-25", "Hari Natal",                    "tutup", "Libur Nasional"),
                new HariLibur("2026-12-31", "Malam Tahun Baru",              "buka",  "Buka sampai pukul 22:00")
        };
        for (HariLibur hl : liburDefault) tambahHariLibur(hl);
    }

    // =========================================================================
    // CONTOH PERTANYAAN
    // =========================================================================
    private void isiContohPertanyaanDefault() {
        try (Connection conn = this.connect();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM contoh_pertanyaan")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) { return; }

        String[][] pertanyaan = {
                {"Tampilkan produk skincare untuk kulit sensitif", "Produk"},
                {"Tampilkan deskripsi, kandungan, jenis kulit, kategori dan area tubuh pada produk Laneige Lip Sleeping Mask", "Produk"},
                {"Apakah Wardah Hydra Rose cocok untuk kulit sensitif?", "Produk"},
                {"Dimana lokasi toko?", "Info Toko"},
                {"Tampilkan link maps lokasi toko?", "Info Toko"},
                {"Tampilkan jam buka dan tutup toko?", "Info Toko"},
                {"Tampilkan semua produk untuk kategori face wash", "Produk"},
                {"Apakah Somethinc Calm Down masih tersedia?", "Produk"},
                {"Ada tidak rekomendasi Sunscreen yang aman untuk kulit sensitif dan tidak pedih di mata?", "Produk"},
                {"Tampilkan produk serum yang fokus meredakan kemerahan (anti-redness) untuk kulit sensitif", "Produk"}
        };

        String sql = "INSERT INTO contoh_pertanyaan(pertanyaan, kategori) VALUES(?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String[] p : pertanyaan) {
                pstmt.setString(1, p[0]);
                pstmt.setString(2, p[1]);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String[]> getAllContohPertanyaan() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT pertanyaan, kategori FROM contoh_pertanyaan ORDER BY id";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("pertanyaan"), rs.getString("kategori")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // DATA AWAL PRODUK
    // =========================================================================
    private void isiDataAwal() {
        Object[][] data = {
                {"Glad2Glow Centella Allantoin Soothing", "Pelembab", 45000, "Centella, Allantoin", "Kulit Sensitif", "Muka", "images/produk/Glad2Glow Centella Allantoin Soothing.png"},
                {"Skintific 5X Ceramide Soothing Toner", "Toner", 115000, "5X Ceramide", "Kulit Kering", "Muka", "images/produk/Skintific 5X Ceramide Soothing Toner.jpg"},
                {"Labore Sensitive Skin Care Gentlebiome Barrier", "Pelembab", 150000, "Microbiome", "Kulit Sensitif", "Muka", "images/produk/Labore Sensitive Skin Care Gentlebiome Barrier.png"},
                {"YOU AcnePlus Low pH Calming Cleanser", "Facial Wash", 55000, "Centella, Herbal", "Kulit Berjerawat", "Muka", "images/produk/YOU AcnePlus Low pH Calming Cleanser.png"},
                {"Acnaway Mugwort Water Gel Moisturizer", "Pelembab", 40000, "Mugwort", "Kulit Berjerawat", "Muka", "images/produk/Acnaway Mugwort Water Gel Moisturizer.jpg"},
                {"Somethinc Holyshield! UV Watery Sunscreen Gel", "Sunscreen", 48000, "UV Filter", "Semua Jenis Kulit", "Muka", "images/produk/Somethinc Holyshield! UV Watery Sunscreen Gel.png"},
                {"Wardah Hydra Rose Petal Infused Toner", "Toner", 35000, "Rose Oil", "Kulit Kering", "Muka", "images/produk/Wardah Hydra Rose Petal Infused Toner.jpg"},
                {"Skintific 5% AHA BHA PHA Exfoliating Toner", "Toner", 110000, "AHA BHA PHA", "Kulit Berminyak", "Muka", "images/produk/Skintific 5% AHA BHA PHA Exfoliating Toner.jpg"},
                {"Ms Glow Acne Series Facial Wash", "Facial Wash", 60000, "Tea Tree", "Kulit Berjerawat", "Muka", "images/produk/Ms Glow Acne Series Facial Wash.jpg"},
                {"Hanasui Power Bright Expert Serum", "Serum", 25000, "Niacinamide", "Semua Jenis Kulit", "Muka", "images/produk/Hanasui Power Bright Expert Serum.jpg"},
                {"Emina Sun Battle SPF 35 PA +++", "Sunscreen", 30000, "Aloe Vera", "Semua Jenis Kulit", "Muka", "images/produk/Emina Sun Battle SPF 35 PA +++.png"},
                {"The Ordinary Niacinamide 10% + Zinc 1%", "Serum", 100000, "Niacinamide, Zinc", "Kulit Berminyak", "Muka", "images/produk/The Ordinary Niacinamide 10% + Zinc 1%.jpg"},
                {"Garnier Sakura White Pinkish Radiance Sleeping", "Pelembab", 28000, "Sakura Extract", "Semua Jenis Kulit", "Muka", "images/produk/Garnier Sakura White Pinkish Radiance Sleeping.jpg"},
                {"Nivea Body Serum Care & Protect", "Body Care", 35000, "Vitamin C, SPF 15", "Semua Jenis Kulit", "Badan", "images/produk/Nivea_Body_Serum.jpg"},
                {"Vaseline Gluta-Hya Serum Burst", "Body Care", 68000, "Hyaluron, Niacinamide", "Kulit Kering", "Badan", "images/produk/Vaseline_Gluta_Hya.jpg"},
                {"Grace and Glow Black Opium", "Body Care", 54000, "Niacinamide, Shea Butter", "Semua Jenis Kulit", "Badan", "images/produk/Grace_Glow_Body.jpg"},
                {"The Caviar Shampoo", "Hair Care", 75000, "Caviar Extract", "Semua Jenis Kulit", "Rambut", "images/produk/Caviar_Shampoo.jpg"},
                {"Makarizo Advisor Hair Recovery Vitamax", "Hair Care", 25000, "Silk Protein, Vit A,C,E", "Semua Jenis Kulit", "Rambut", "images/produk/Makarizo Advisor Hair Recovery Vitamax.jpg"},
                {"Somethinc Game Changer Tripeptide Eye Concentrate Gel", "Eye Care", 145000, "Peptide, Caffeine", "Semua Jenis Kulit", "Mata", "images/produk/Somethinc Game Changer Tripeptide Eye Concentrate Gel.jpg"},
                {"Skintific 360 Crystal Massager Lifting Eye Cream", "Eye Care", 160000, "Retinol, Peptide", "Kulit Menua", "Mata", "images/produk/Skintific 360 Crystal Massager Lifting Eye Cream.jpg"},
                {"Pure Paw Paw Ointment", "Lip Care", 65000, "Carica Papaya", "Kulit Kering", "Bibir", "images/produk/Pure Paw Paw Ointment.jpg"},
                {"Laneige Lip Sleeping Mask", "Lip Care", 200000, "Berry Mix Complex", "Kulit Kering", "Bibir", "images/produk/Laneige Lip Sleeping Mask.jpg"},
                {"The Body Shop Almond Hand & Nail Cream", "Hand Care", 99000, "Almond Oil", "Kulit Kering", "Tangan & Kaki", "images/produk/The Body Shop Almond Hand & Nail Cream.jpg"},
                {"Bio Oil Skincare Oil", "Body Care", 140000, "PurCellin Oil", "Kulit Sensitif", "Badan", "images/produk/Bio Oil Skincare Oil.jpg"},
                {"COSRX Acne Pimple Master Patch", "Acne Care", 45000, "Hydrocolloid", "Kulit Berjerawat", "Muka", "images/produk/COSRX Acne Pimple Master Patch .jpg"},
                {"Hada Labo Gokujyun Ultimate Moisturizing Lotion", "Toner", 48000, "Hyaluronic Acid", "Kulit Kering", "Muka", "images/produk/Hada Labo Gokujyun Ultimate Moisturizing Lotion.jpg"},
                {"Avoskin Miraculous Retinol Ampoule", "Serum", 249000, "Retinol, Peptide", "Kulit Menua", "Muka", "images/produk/Avoskin Miraculous Retinol Ampoule.jpg"},
                {"Cetaphil Gentle Skin Cleanser", "Facial Wash", 120000, "Glycerin, Panthenol", "Kulit Sensitif", "Muka", "images/produk/Cetaphil Gentle Skin Cleanser.jpg"},
                {"The Originote Hyalucera Moisturizer Gel", "Pelembab", 42000, "Hyaluron, Chlorelina", "Kulit Normal", "Muka", "images/produk/The Originote Hyalucera Moisturizer Gel.jpg"},
                {"Azarine Hydrasoothe Sunscreen Gel SPF45 PA++++", "Sunscreen", 65000, "Aloe Vera, Propolis", "Kulit Berminyak", "Muka", "images/produk/Azarine Hydrasoothe Sunscreen Gel SPF45 PA++++.jpg"}
        };

        for (Object[] row : data) {
            tambahProduk(new Produk(
                    null, (String) row[0], (String) row[1],
                    ((Number) row[2]).longValue(), (String) row[3],
                    true, (String) row[4], (String) row[5], (String) row[6]
            ));
        }
    }


    // =========================================================================
    // TEST HARI LIBUR
    // (logika dari TestHariLibur dipindahkan ke sini agar tidak ada run baru)
    // =========================================================================

    /**
     * Menjalankan uji cek status toko berdasarkan daftar hari libur dari database.
     * Panggil via: DatabaseService.getInstance().testHariLibur()
     */
    public void testHariLibur() {
        System.out.println("========================================");
        System.out.println("   TEST HARI LIBUR - STATUS TOKO");
        System.out.println("========================================\n");

        // Ambil dari database; jika kosong pakai data simulasi sementara
        List<HariLibur> daftarLibur = getAllHariLibur();
        if (daftarLibur.isEmpty()) {
            daftarLibur = new ArrayList<>();
            daftarLibur.add(new HariLibur("2026-01-01", "Tahun Baru Masehi",    "tutup", "Libur Nasional"));
            daftarLibur.add(new HariLibur("2026-03-20", "Isra Miraj",           "tutup", "Libur Nasional"));
            daftarLibur.add(new HariLibur("2026-03-31", "Idul Fitri",           "tutup", "Libur Lebaran"));
            daftarLibur.add(new HariLibur("2026-04-01", "Idul Fitri Hari ke-2", "tutup", "Libur Lebaran"));
            daftarLibur.add(new HariLibur("2026-05-01", "Hari Buruh",           "buka",  "Tetap buka seperti biasa"));
            daftarLibur.add(new HariLibur("2026-08-17", "HUT RI",               "buka",  "Tetap buka, ada promo kemerdekaan"));
            daftarLibur.add(new HariLibur("2026-12-25", "Hari Natal",           "tutup", "Libur Nasional"));
            System.out.println("[INFO] Tabel hari_libur masih kosong, memakai data simulasi.\n");
        }

        LocalDate[] tanggalTest = {
                LocalDate.of(2026, 1,  1),
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 5,  1),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 12, 25),
                LocalDate.of(2026, 6, 15),
        };

        for (LocalDate tanggal : tanggalTest) {
            cetakStatusHariLibur(tanggal, daftarLibur);
        }

        System.out.println("========================================");
        System.out.println("   CEK HARI INI: " + LocalDate.now());
        System.out.println("========================================");
        cetakStatusHariLibur(LocalDate.now(), daftarLibur);
    }

    /**
     * Mencetak status toko untuk satu tanggal tertentu.
     */
    private void cetakStatusHariLibur(LocalDate tanggal, List<HariLibur> daftarLibur) {
        System.out.println("Tanggal : " + tanggal);

        HariLibur hariIni = null;
        for (HariLibur hl : daftarLibur) {
            if (hl.cocokDengan(tanggal)) {
                hariIni = hl;
                break;
            }
        }

        if (hariIni != null) {
            System.out.println("Hari    : " + hariIni.getNama());
            System.out.println("Ket     : " + hariIni.getKeterangan());
            System.out.println("Status  : " + (hariIni.isTutup() ? "\u274C TOKO TUTUP" : "\u2705 TOKO BUKA"));
        } else {
            System.out.println("Hari    : Hari biasa (tidak ada di daftar libur)");
            System.out.println("Status  : \u2705 TOKO BUKA (jam operasional normal)");
        }

        System.out.println("----------------------------------------\n");
    }


    // =========================================================================
    // INNER CLASS: HariLibur
    // (dipindahkan dari model/HariLibur.java - tidak ada file class baru)
    // =========================================================================

    public static class HariLibur {
        private String tanggal;
        private String nama;
        private String status;
        private String keterangan;

        public HariLibur() {}

        public HariLibur(String tanggal, String nama, String status, String keterangan) {
            this.tanggal    = tanggal;
            this.nama       = nama;
            this.status     = status;
            this.keterangan = keterangan;
        }

        /** Cek apakah hari libur ini cocok dengan tanggal yang diberikan. */
        public boolean cocokDengan(LocalDate tanggalCek) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate tgl = LocalDate.parse(this.tanggal, formatter);
                return tgl.equals(tanggalCek);
            } catch (Exception e) {
                return false;
            }
        }

        /** Cek apakah status hari ini adalah tutup. */
        public boolean isTutup() {
            return "tutup".equalsIgnoreCase(this.status);
        }

        public String getTanggal()              { return tanggal; }
        public void   setTanggal(String t)      { this.tanggal = t; }
        public String getNama()                 { return nama; }
        public void   setNama(String n)         { this.nama = n; }
        public String getStatus()               { return status; }
        public void   setStatus(String s)       { this.status = s; }
        public String getKeterangan()           { return keterangan; }
        public void   setKeterangan(String k)   { this.keterangan = k; }

        @Override
        public String toString() {
            return "HariLibur{tanggal='" + tanggal + "', nama='" + nama +
                    "', status='" + status + "', keterangan='" + keterangan + "'}";
        }
    }
}