package org.sahabatlaris.chatbot.model;

public class Kategori {
    private String namaKategori;

    public Kategori() {}
    public Kategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String v) { this.namaKategori = v; }

    @Override
    public String toString() { return namaKategori; }
}
