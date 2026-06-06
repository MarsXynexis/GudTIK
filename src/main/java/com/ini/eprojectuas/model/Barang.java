/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ini.eprojectuas.model;


public class Barang {
    private String idBarang;
    private String namaBarang;
    private Kategori kategori;
    private TipeBarang tipeBarang;
    
    private int stokTersedia;
    private int sedangDipinjam;
    
    private String kondisiSaatIni; 

    // Tambahkan parameter kondisiUI di konstruktor karena data rusak/hilang tidak ada di class ini
    public Barang(String idBarang, String namaBarang, Kategori kategori, TipeBarang tipeBarang, 
                  int stokTersedia, int sedangDipinjam, String kondisiUI) {
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.tipeBarang = tipeBarang;
        this.stokTersedia = stokTersedia;
        this.sedangDipinjam = sedangDipinjam;
        this.kondisiSaatIni = kondisiUI;
    }

    public String getIdBarang() { return idBarang; }
    public String getNamaBarang() { return namaBarang; }
    public Kategori getKategori() { return kategori; }
    public TipeBarang getTipeBarang() { return tipeBarang; }
    public int getStokTersedia() { return stokTersedia; }
    public int getSedangDipinjam() { return sedangDipinjam; }
    public String getKondisiSaatIni() { return kondisiSaatIni; }

    public void setIdBarang(String idBarang) { this.idBarang = idBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
    public void setKategori(Kategori kategori) { this.kategori = kategori; }
    public void setTipeBarang(TipeBarang tipeBarang) { this.tipeBarang = tipeBarang; }
    public void setStokTersedia(int stokTersedia) { this.stokTersedia = stokTersedia; }
    public void setSedangDipinjam(int sedangDipinjam) { this.sedangDipinjam = sedangDipinjam; }
    public void setKondisiSaatIni(String kondisiSaatIni) { this.kondisiSaatIni = kondisiSaatIni; }
    
//    private void tentukanKondisiUI() {
//    if (this.stokTersedia > 0) {
//        this.kondisiSaatIni = "Tersedia";
//    } else if (this.stokTersedia == 0 && this.sedangDipinjam > 0) {
//        this.kondisiSaatIni = "Dipinjam"; // Stok gudang kosong karena sedang dipinjam mahasiswa
//    } else {
//        this.kondisiSaatIni = "Habis"; // Stok kosong murni (karena habis terpakai atau hilang/rusak total)
//    }
//}
}