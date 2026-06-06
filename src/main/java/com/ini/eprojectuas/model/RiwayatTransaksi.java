/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ini.eprojectuas.model;

import java.time.LocalDateTime;

/**
 *
 * @author raziq
 */

public class RiwayatTransaksi {
    private String idTransaksi;
    private String idBarang;
    private String idPeminjaman; // Tambahan field baru
    private String pelaku;
    private String jenisTransaksi;
    private int jumlah;
    private LocalDateTime tanggalTransaksi;
    private String keterangan;

    // Tambahan variabel bantuan untuk UI TableView
    private String namaBarang; 
    private String tanggalTransaksiUI; 

    // Konstruktor Kosong (Wajib ada untuk instansiasi via Setter)
    public RiwayatTransaksi() {
    }

    // Konstruktor Penuh
    public RiwayatTransaksi(String idTransaksi, String idBarang, String idPeminjaman, String pelaku, String jenisTransaksi, 
                            int jumlah, LocalDateTime tanggalTransaksi, String keterangan) {
        this.idTransaksi = idTransaksi;
        this.idBarang = idBarang;
        this.idPeminjaman = idPeminjaman;
        this.pelaku = pelaku;
        this.jenisTransaksi = jenisTransaksi;
        this.jumlah = jumlah;
        this.tanggalTransaksi = tanggalTransaksi;
        this.keterangan = keterangan;
    }

    // Getter bawaan database
    public String getIdTransaksi() { return idTransaksi; }
    public String getIdBarang() { return idBarang; }
    public String getIdPeminjaman() { return idPeminjaman; }
    public String getPelaku() { return pelaku; }
    public String getJenisTransaksi() { return jenisTransaksi; }
    public int getJumlah() { return jumlah; }
    public LocalDateTime getTanggalTransaksi() { return tanggalTransaksi; }
    public String getKeterangan() { return keterangan; }

    // Getter bantuan UI
    public String getNamaBarang() { return namaBarang; }
    public String getTanggalTransaksiUI() { return tanggalTransaksiUI; }

    // Setter bawaan database
    public void setIdTransaksi(String idTransaksi) { this.idTransaksi = idTransaksi; }
    public void setIdBarang(String idBarang) { this.idBarang = idBarang; }
    public void setIdPeminjaman(String idPeminjaman) { this.idPeminjaman = idPeminjaman; }
    public void setPelaku(String pelaku) { this.pelaku = pelaku; }
    public void setJenisTransaksi(String jenisTransaksi) { this.jenisTransaksi = jenisTransaksi; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
    public void setTanggalTransaksi(LocalDateTime tanggalTransaksi) { this.tanggalTransaksi = tanggalTransaksi; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    // Setter bantuan UI
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
    public void setTanggalTransaksiUI(String tanggalTransaksiUI) { this.tanggalTransaksiUI = tanggalTransaksiUI; }
}