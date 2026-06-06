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
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class TambahPeminjamanViewController implements Initializable {

    @FXML
    private TextField fieldNim;
    @FXML
    private ComboBox<String> comboBarang;
    @FXML
    private TextField fieldJumlah;
    @FXML
    private DatePicker dateTenggat;

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

    private String generateIdPeminjaman(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(id_peminjaman, 5) AS UNSIGNED)) "
                + "FROM peminjaman "
                + "WHERE id_peminjaman LIKE 'PJM-%'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            int nextNum = 1;
            if (rs.next() && rs.getObject(1) != null) {
                nextNum = rs.getInt(1) + 1;
            }
            return String.format("PJM-%03d", nextNum);
        }
    }

    private void loadDataBarang() {
        ObservableList<String> listBarang = FXCollections.observableArrayList();
        // Hanya memuat barang yang stoknya lebih dari 0 dan bertipe RETURNABLE / TOOLS
        String sql = "SELECT id_barang, nama_barang, stok_tersedia FROM barang WHERE stok_tersedia > 0 AND tipe_barang = 'RETURNABLE'";
        Connection conn = Database.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Format di UI: "ID_BARANG - NAMA_BARANG (Sisa Stok: X)"
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
        String nim = fieldNim.getText();
        String pilihanBarang = comboBarang.getValue();
        String jumlahStr = fieldJumlah.getText();

        if (nim.isEmpty() || pilihanBarang == null || jumlahStr.isEmpty() || dateTenggat.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom wajib diisi!");
            return;
        }

        int jumlahPinjam;
        try {
            jumlahPinjam = Integer.parseInt(jumlahStr);
            if (jumlahPinjam <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jumlah peminjaman harus berupa angka lebih dari 0!");
            return;
        }

        // Ekstrak id_barang dari format string ComboBox (Mengambil teks sebelum spasi pertama)
        String idBarang = pilihanBarang.split(" - ")[0];

        // Ekstrak stok maksimal untuk validasi
        int stokMaksimal = Integer.parseInt(pilihanBarang.substring(pilihanBarang.lastIndexOf(": ") + 2, pilihanBarang.length() - 1));

        if (jumlahPinjam > stokMaksimal) {
            showAlert(Alert.AlertType.WARNING, "Stok Tidak Cukup", "Jumlah pinjam melebihi stok yang tersedia!");
            return;
        }

        Connection conn = Database.getConnection();
        String namaAdmin = UserSession.getActiveNamaLengkap();
        Timestamp tenggatWaktu = Timestamp.valueOf(dateTenggat.getValue().atTime(23, 59, 59));

        String sql = "INSERT INTO peminjaman (id_peminjaman, id_barang, nim_peminjam, pelaku, jumlah, tenggat_waktu) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String idPeminjaman = generateIdPeminjaman(conn);

            pstmt.setString(1, idPeminjaman);
            pstmt.setString(2, idBarang);
            pstmt.setString(3, nim);
            pstmt.setString(4, (namaAdmin != null) ? namaAdmin : "Admin");
            pstmt.setInt(5, jumlahPinjam);
            pstmt.setTimestamp(6, tenggatWaktu);

            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data peminjaman berhasil dicatat!");
            tutupModal(event);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error SQL", "Gagal menyimpan peminjaman:\n" + e.getMessage());
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
