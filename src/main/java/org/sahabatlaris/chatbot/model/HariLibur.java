package org.sahabatlaris.chatbot.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HariLibur {
    private String tanggal;
    private String nama;
    private String status;
    private String keterangan;

    // Constructor kosong
    public HariLibur() {}

    // Constructor lengkap
    public HariLibur(String tanggal, String nama, String status, String keterangan) {
        this.tanggal = tanggal;
        this.nama = nama;
        this.status = status;
        this.keterangan = keterangan;
    }

    /**
     * Cek apakah hari libur ini cocok dengan tanggal yang diberikan.
     */
    public boolean cocokDengan(LocalDate tanggalCek) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate tgl = LocalDate.parse(this.tanggal, formatter);
            return tgl.equals(tanggalCek);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cek apakah status hari ini adalah tutup.
     */
    public boolean isTutup() {
        return "tutup".equalsIgnoreCase(this.status);
    }

    // Getters & Setters
    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    // toString untuk debugging
    @Override
    public String toString() {
        return "HariLibur{" +
                "tanggal='" + tanggal + '\'' +
                ", nama='" + nama + '\'' +
                ", status='" + status + '\'' +
                ", keterangan='" + keterangan + '\'' +
                '}';
    }
}
