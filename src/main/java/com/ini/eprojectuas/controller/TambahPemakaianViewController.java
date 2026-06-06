/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.utils.Database;
import com.ini.eprojectuas.utils.UserSession;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class TambahPemakaianViewController implements Initializable {

    @FXML
    private TextField fieldNim;
    @FXML
    private ComboBox<String> comboBarang;
    @FXML
    private TextField fieldJumlah;
    @FXML
    private TextArea areaTujuan;

    @FXML
    private Button btnBatal;
    @FXML
    private Button btnSimpan;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDataBarang();

        btnBatal.setOnAction(this::tutupModal);
        btnSimpan.setOnAction(this::simpanData);
    }

    private String generateIdPemakaian(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(id_pemakaian, 5) AS UNSIGNED)) "
                + "FROM pemakaian "
                + "WHERE id_pemakaian LIKE 'PKI-%'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            int nextNum = 1;
            if (rs.next() && rs.getObject(1) != null) {
                nextNum = rs.getInt(1) + 1;
            }
            return String.format("PKI-%03d", nextNum);
        }
    }

    private void loadDataBarang() {
        ObservableList<String> listBarang = FXCollections.observableArrayList();
        // Hanya memuat barang bertipe CONSUMABLE yang stoknya lebih dari 0
        String sql = "SELECT id_barang, nama_barang, stok_tersedia FROM barang WHERE stok_tersedia > 0 AND tipe_barang = 'CONSUMABLE'";
        Connection conn = Database.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String item = rs.getString("id_barang") + " - "
                        + rs.getString("nama_barang")
                        + " (Sisa Stok: " + rs.getInt("stok_tersedia") + ")";
                listBarang.add(item);
            }
            comboBarang.setItems(listBarang);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void simpanData(ActionEvent event) {
        String nim = fieldNim.getText().trim();
        String pilihanBarang = comboBarang.getValue();
        String jumlahStr = fieldJumlah.getText().trim();
        String tujuan = areaTujuan.getText().trim();

        if (nim.isEmpty() || pilihanBarang == null || jumlahStr.isEmpty() || tujuan.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom wajib diisi!");
            return;
        }

        int jumlahPakai;
        try {
            jumlahPakai = Integer.parseInt(jumlahStr);
            if (jumlahPakai <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jumlah pemakaian harus berupa angka lebih dari 0!");
            return;
        }

        // Ekstrak id_barang dari string ComboBox
        String idBarang = pilihanBarang.split(" - ")[0];

        // Ekstrak stok maksimal
        int stokMaksimal = Integer.parseInt(pilihanBarang.substring(pilihanBarang.lastIndexOf(": ") + 2, pilihanBarang.length() - 1));

        if (jumlahPakai > stokMaksimal) {
            showAlert(Alert.AlertType.WARNING, "Stok Tidak Cukup", "Jumlah pemakaian melebihi stok yang tersedia di gudang!");
            return;
        }

        // Generate ID Pemakaian dengan Base36 (contoh: PAK-LWQ1K2S)
       
        String namaAdmin = UserSession.getActiveNamaLengkap();

        Connection conn = Database.getConnection();
        try {
            String idPemakaian = generateIdPemakaian(conn); // di dalam try agar SQLException ter-handle

            String sql = "INSERT INTO pemakaian (id_pemakaian, id_barang, nim_pemakai, pelaku, jumlah, tujuan_pemakaian) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, idPemakaian);
                pstmt.setString(2, idBarang);
                pstmt.setString(3, nim);
                pstmt.setString(4, (namaAdmin != null) ? namaAdmin : "Admin");
                pstmt.setInt(5, jumlahPakai);
                pstmt.setString(6, tujuan);
                pstmt.executeUpdate();
            }

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data pemakaian barang berhasil dicatat!");
            tutupModal(event);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error SQL", "Gagal mencatat pemakaian:\n" + e.getMessage());
        }
    }

    private void tutupModal(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
