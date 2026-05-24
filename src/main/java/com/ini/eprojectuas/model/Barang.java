/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ini.eprojectuas.model;

public class Barang {
    private int no;
    private String kodeBarang;
    private String namaBarang;
    private String kategori;
    private int stock;
    private int borrowed;
    

    public Barang(int no, String kodeBarang, String namaBarang, String kategori, int stock) {
        this.no = no;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.stock = stock;
        this.borrowed = borrowed;
    }

    public int getNo() { return no; }
    public String getKodeBarang() { return kodeBarang; }
    public String getNamaBarang() { return namaBarang; }
    public String getKategori() { return kategori; }
    public int getStock() { return stock; }
    public int getBorrowed() { return borrowed; }
}