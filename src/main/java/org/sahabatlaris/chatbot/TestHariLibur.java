package org.sahabatlaris.chatbot;

import org.sahabatlaris.chatbot.model.HariLibur;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestHariLibur {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("   TEST HARI LIBUR - STATUS TOKO");
        System.out.println("========================================\n");

        // -------------------------------------------------------
        // 1. Buat daftar hari libur (simulasi data dari file)
        // -------------------------------------------------------
        List<HariLibur> daftarLibur = new ArrayList<>();
        daftarLibur.add(new HariLibur("2026-01-01", "Tahun Baru Masehi",   "tutup", "Libur Nasional"));
        daftarLibur.add(new HariLibur("2026-03-20", "Isra Miraj",          "tutup", "Libur Nasional"));
        daftarLibur.add(new HariLibur("2026-03-31", "Idul Fitri",          "tutup", "Libur Lebaran"));
        daftarLibur.add(new HariLibur("2026-04-01", "Idul Fitri Hari ke-2","tutup", "Libur Lebaran"));
        daftarLibur.add(new HariLibur("2026-05-01", "Hari Buruh",          "buka",  "Tetap buka seperti biasa"));
        daftarLibur.add(new HariLibur("2026-08-17", "HUT RI",              "buka",  "Tetap buka, ada promo kemerdekaan"));
        daftarLibur.add(new HariLibur("2026-12-25", "Hari Natal",          "tutup", "Libur Nasional"));

        // -------------------------------------------------------
        // 2. Tanggal yang ingin dicek (ubah sesuai kebutuhan)
        // -------------------------------------------------------
        LocalDate[] tanggalTest = {
            LocalDate.of(2026, 1, 1),   // Tahun Baru  -> TUTUP
            LocalDate.of(2026, 3, 31),  // Idul Fitri  -> TUTUP
            LocalDate.of(2026, 5, 1),   // Hari Buruh  -> BUKA
            LocalDate.of(2026, 8, 17),  // HUT RI      -> BUKA
            LocalDate.of(2026, 12, 25), // Natal        -> TUTUP
            LocalDate.of(2026, 6, 15),  // Hari biasa  -> BUKA (tidak ada di daftar)
        };

        // -------------------------------------------------------
        // 3. Cek setiap tanggal
        // -------------------------------------------------------
        for (LocalDate tanggal : tanggalTest) {
            cekStatusToko(tanggal, daftarLibur);
        }

        // -------------------------------------------------------
        // 4. Cek hari ini secara otomatis
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("   CEK HARI INI: " + LocalDate.now());
        System.out.println("========================================");
        cekStatusToko(LocalDate.now(), daftarLibur);
    }

    // -------------------------------------------------------
    // Method: cek status toko berdasarkan tanggal
    // -------------------------------------------------------
    static void cekStatusToko(LocalDate tanggal, List<HariLibur> daftarLibur) {
        System.out.println("Tanggal : " + tanggal);

        HariLibur hariIni = null;
        for (HariLibur hl : daftarLibur) {
            if (hl.cocokDengan(tanggal)) {
                hariIni = hl;
                break;
            }
        }

        if (hariIni != null) {
            // Tanggal ada di daftar hari libur
            System.out.println("Hari    : " + hariIni.getNama());
            System.out.println("Ket     : " + hariIni.getKeterangan());

            if (hariIni.isTutup()) {
                System.out.println("Status  : ❌ TOKO TUTUP");
            } else {
                System.out.println("Status  : ✅ TOKO BUKA");
            }
        } else {
            // Hari biasa, tidak ada di daftar libur
            System.out.println("Hari    : Hari biasa (tidak ada di daftar libur)");
            System.out.println("Status  : ✅ TOKO BUKA (jam operasional normal)");
        }

        System.out.println("----------------------------------------\n");
    }
}
