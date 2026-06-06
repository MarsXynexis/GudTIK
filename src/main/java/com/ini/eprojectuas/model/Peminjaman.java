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
public class Peminjaman {

    private String idPeminjaman;
    private String idBarang;
    private String nimPeminjam;
    private String pelaku;
    private int jumlah;
    private LocalDateTime tanggalPinjam;
    private LocalDateTime tenggatWaktu;
    private LocalDateTime tanggalKembali;
    private String status;

    public Peminjaman() {
    }

    public Peminjaman(String idPeminjaman, String idBarang, String nimPeminjam, String pelaku, int jumlah, LocalDateTime tanggalPinjam, LocalDateTime tenggatWaktu, LocalDateTime tanggalKembali, String status) {
        this.idPeminjaman = idPeminjaman;
        this.idBarang = idBarang;
        this.nimPeminjam = nimPeminjam;
        this.pelaku = pelaku;
        this.jumlah = jumlah;
        this.tanggalPinjam = tanggalPinjam;
        this.tenggatWaktu = tenggatWaktu;
        this.tanggalKembali = tanggalKembali;
        this.status = status;
    }

    public String getIdPeminjaman() {
        return idPeminjaman;
    }

    public void setIdPeminjaman(String idPeminjaman) {
        this.idPeminjaman = idPeminjaman;
    }

    public String getIdBarang() {
        return idBarang;
    }

    public void setIdBarang(String idBarang) {
        this.idBarang = idBarang;
    }

    public String getNimPeminjam() {
        return nimPeminjam;
    }

    public void setNimPeminjam(String nimPeminjam) {
        this.nimPeminjam = nimPeminjam;
    }

    public String getPelaku() {
        return pelaku;
    }

    public void setPelaku(String pelaku) {
        this.pelaku = pelaku;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public LocalDateTime getTanggalPinjam() {
        return tanggalPinjam;
    }

    public void setTanggalPinjam(LocalDateTime tanggalPinjam) {
        this.tanggalPinjam = tanggalPinjam;
    }

    public LocalDateTime getTenggatWaktu() {
        return tenggatWaktu;
    }

    public void setTenggatWaktu(LocalDateTime tenggatWaktu) {
        this.tenggatWaktu = tenggatWaktu;
    }

    public LocalDateTime getTanggalKembali() {
        return tanggalKembali;
    }

    public void setTanggalKembali(LocalDateTime tanggalKembali) {
        this.tanggalKembali = tanggalKembali;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    // Variabel bantuan untuk UI TableView
    private String namaBarang;
    private String kategori;
    private String tanggalPinjamUI;
    private String tanggalTenggatUI;
    private String tanggalKembaliUI;

    // Tambahkan Getter & Setter untuk variabel bantuan ini
    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getTanggalPinjamUI() { return tanggalPinjamUI; }
    public void setTanggalPinjamUI(String tanggalPinjamUI) { this.tanggalPinjamUI = tanggalPinjamUI; }

    public String getTanggalTenggatUI() { return tanggalTenggatUI; }
    public void setTanggalTenggatUI(String tanggalTenggatUI) { this.tanggalTenggatUI = tanggalTenggatUI; }
    
    public String getTanggalKembaliUI() { return tanggalKembaliUI; }
    public void setTanggalKembaliUI(String tanggalKembaliUI) { this.tanggalKembaliUI = tanggalKembaliUI; }
}
