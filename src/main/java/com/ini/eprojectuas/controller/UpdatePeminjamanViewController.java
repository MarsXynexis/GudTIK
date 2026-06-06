/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.model.Peminjaman;
import com.ini.eprojectuas.utils.Database;
import com.ini.eprojectuas.utils.UserSession;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class UpdatePeminjamanViewController implements Initializable {

    @FXML
    private TextField fieldId;
    @FXML
    private TextField fieldNim;
    @FXML
    private TextField fieldBarang;
    @FXML
    private ComboBox<String> comboStatus;

    @FXML
    private Button btnBatal;
    @FXML
    private Button btnSimpan;

    private Peminjaman peminjamanAktif;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Pilihan status dikembalikan atau bermasalah
        comboStatus.setItems(FXCollections.observableArrayList(
                "Selesai (Dikembalikan)",
                "Rusak",
                "Hilang"
        ));

        btnBatal.setOnAction(this::tutupModal);
        btnSimpan.setOnAction(this::simpanData);
    }

    private String generateIdRiwayat(Connection conn, String prefix) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(id_transaksi, ?) AS UNSIGNED)) "
                + "FROM riwayat_transaksi "
                + "WHERE id_transaksi LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setInt(1, prefix.length() + 2); // skip "RET-" / "DMG-" / "LST-"
            pstmt.setString(2, prefix + "-%");

            try (ResultSet rs = pstmt.executeQuery()) {
                int nextNum = 1;
                if (rs.next() && rs.getObject(1) != null) {
                    nextNum = rs.getInt(1) + 1;
                }
                return String.format("%s-%03d", prefix, nextNum);
            }
        }
    }

    // Dipanggil dari PeminjamanViewController sebelum modal dimunculkan
    public void setDataPeminjaman(Peminjaman data) {
        this.peminjamanAktif = data;
        fieldId.setText(data.getIdPeminjaman());
        fieldNim.setText(data.getNimPeminjam());
        fieldBarang.setText(data.getNamaBarang() + " (Jumlah: " + data.getJumlah() + ")");
    }

    private void simpanData(ActionEvent event) {
        String statusPilihan = comboStatus.getValue();

        if (statusPilihan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan pilih status akhir peminjaman!");
            return;
        }

        String statusUpdateTabel;
        String jenisTransaksiLog;
        String prefixLog;

        if (statusPilihan.equals("Selesai (Dikembalikan)")) {
            statusUpdateTabel = "Selesai";
            jenisTransaksiLog = "Dikembalikan";
            prefixLog = "RET";
        } else if (statusPilihan.equals("Rusak")) {
            statusUpdateTabel = "Rusak";
            jenisTransaksiLog = "Rusak";
            prefixLog = "DMG";
        } else {
            statusUpdateTabel = "Hilang";
            jenisTransaksiLog = "Hilang";
            prefixLog = "LST";
        }

        Connection conn = Database.getConnection();

        try {
            // BLOK TRY UTAMA: Membungkus semua aktivitas database
            conn.setAutoCommit(false);

            // 1. Update tabel peminjaman
            String sqlUpdate = "UPDATE peminjaman SET status = ?, tanggal_kembali = CURRENT_TIMESTAMP WHERE id_peminjaman = ?";
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                pstmtUpdate.setString(1, statusUpdateTabel);
                pstmtUpdate.setString(2, peminjamanAktif.getIdPeminjaman());
                pstmtUpdate.executeUpdate();
            }

            // 2. Insert log ke riwayat_transaksi
            conn.setAutoCommit(false);
            String idTrans = generateIdRiwayat(conn, prefixLog); // RET-001, DMG-001, LST-001
            String namaAdmin = UserSession.getActiveNamaLengkap();
            String keterangan = "Penyelesaian transaksi peminjaman oleh NIM: " + peminjamanAktif.getNimPeminjam();

            // AMBIL ID BARANG DARI TABEL PEMINJAMAN SECARA LANGSUNG
            String idBarangAsli = peminjamanAktif.getIdBarang();
            if (idBarangAsli == null || idBarangAsli.isEmpty()) {
                String sqlGetIdBarang = "SELECT id_barang FROM peminjaman WHERE id_peminjaman = ?";
                try (PreparedStatement psGet = conn.prepareStatement(sqlGetIdBarang)) {
                    psGet.setString(1, peminjamanAktif.getIdPeminjaman());
                    try (ResultSet rsGet = psGet.executeQuery()) {
                        if (rsGet.next()) {
                            idBarangAsli = rsGet.getString("id_barang");
                        }
                    }
                }
            }

            // 3. Masukkan ke Riwayat Transaksi
            String sqlInsertLog = "INSERT INTO riwayat_transaksi (id_transaksi, id_barang, id_peminjaman, pelaku, jenis_transaksi, jumlah, keterangan) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertLog)) {
                pstmtInsert.setString(1, idTrans);
                pstmtInsert.setString(2, idBarangAsli); // Menggunakan ID yang sudah divalidasi
                pstmtInsert.setString(3, peminjamanAktif.getIdPeminjaman());
                pstmtInsert.setString(4, (namaAdmin != null) ? namaAdmin : "Admin");
                pstmtInsert.setString(5, jenisTransaksiLog);
                pstmtInsert.setInt(6, peminjamanAktif.getJumlah());
                pstmtInsert.setString(7, keterangan);
                pstmtInsert.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Status peminjaman berhasil diperbarui!");
            tutupModal(event);

        } catch (SQLException e) {
            // BLOK CATCH: Menangkap error jika terjadi kegagalan SQL
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memperbarui data:\n" + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
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
