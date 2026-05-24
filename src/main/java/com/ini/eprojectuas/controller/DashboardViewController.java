/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class DashboardViewController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private PieChart pieChart;
    @FXML
    private Label pieTersedia;
    @FXML
    private Label pieDipinjam;
    @FXML
    private Label pieRusak;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

// ... (di dalam method initialize)
        // 1. Siapkan Data
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Baik", 82),
            new PieChart.Data("Dipinjam", 28),
            new PieChart.Data("Rusak", 15)
    );
    
    // 2. Masukkan data ke grafik
    pieChart.setData(pieChartData);

    // 3. Hitung Total Semua Alat untuk mencari persentase
    double totalAlat = 0;
    for (PieChart.Data data : pieChartData) {
        totalAlat += data.getPieValue();
    }

    // 4. Hitung Persentase dan Set Teks ke Legend Kustom
    // Data index 0 (Baik)
    double nilaiBaik = pieChartData.get(0).getPieValue();
    double persenBaik = (nilaiBaik / totalAlat) * 100;
    pieTersedia.setText(String.format("Tersedia : %.0f (%.1f%%)", nilaiBaik, persenBaik));

    // Data index 1 (Dipinjam)
    double nilaiDipinjam = pieChartData.get(1).getPieValue();
    double persenDipinjam = (nilaiDipinjam / totalAlat) * 100;
    pieDipinjam.setText(String.format("Dipinjam : %.0f (%.1f%%)", nilaiDipinjam, persenDipinjam));

    // Data index 2 (Rusak)
    double nilaiRusak = pieChartData.get(2).getPieValue();
    double persenRusak = (nilaiRusak / totalAlat) * 100;
    pieRusak.setText(String.format("Rusak : %.0f (%.1f%%)", nilaiRusak, persenRusak));
    }

}
