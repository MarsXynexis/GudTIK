package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.model.Barang;
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

public class TambahViewController implements Initializable {

    @FXML
    private TextField fieldId;
    @FXML
    private TextField fieldNama;
    @FXML
    private ComboBox<String> comboKategori;
    @FXML
    private ComboBox<String> comboTipe;
    @FXML
    private TextField fieldStok;

    @FXML
    private Button btnBatal;
    @FXML
    private Button btnSimpan;
    private boolean isEditMode = false;
    private int stokLama = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. PERBAIKAN: Gunakan huruf kapital semua agar cocok dengan ENUM Database dan Enum Java
        comboKategori.setItems(FXCollections.observableArrayList("NETWORKING", "COMPONENTS", "TOOLS", "PERIPHERALS", "LAINNYA"));
        comboTipe.setItems(FXCollections.observableArrayList("CONSUMABLE", "RETURNABLE"));

        // 2. Hubungkan Tombol dengan Method
        btnBatal.setOnAction(this::tutupModal);
        btnSimpan.setOnAction(this::simpanData);
    }

    private String generateIdTransaksi(Connection conn, String prefix) throws SQLException {
        // Cari nomor urut terbesar yang sudah ada untuk prefix ini
        String sql = "SELECT MAX(CAST(SUBSTRING(id_transaksi, ?) AS UNSIGNED)) "
                + "FROM riwayat_transaksi "
                + "WHERE id_transaksi LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, prefix.length() + 2); // posisi mulai angka (skip "ADD-")
            pstmt.setString(2, prefix + "-%");    // filter: "ADD-%"

            try (ResultSet rs = pstmt.executeQuery()) {
                int nextNum = 1; // default mulai dari 1
                if (rs.next() && rs.getObject(1) != null) {
                    nextNum = rs.getInt(1) + 1; // ambil max + 1
                }
                return String.format("%s-%03d", prefix, nextNum); // "ADD-001"
            }
        }
    }

    public void setEditData(Barang barang) {
        if (barang != null) {
            isEditMode = true;
            this.stokLama = barang.getStokTersedia();

            // Isi form dengan data yang dilempar
            fieldId.setText(barang.getIdBarang());
            fieldId.setEditable(false); // Kunci ID agar tidak bisa diubah (karena Primary Key)
            fieldId.setStyle("-fx-background-color: #e5e7eb;"); // Opsional: Beri warna abu-abu

            fieldNama.setText(barang.getNamaBarang());
            comboKategori.setValue(barang.getKategori().name()); // Memanggil string murni
            comboTipe.setValue(barang.getTipeBarang().name());
            fieldStok.setText(String.valueOf(barang.getStokTersedia()));

            btnSimpan.setText("Update"); // Ubah teks tombol
        }
    }

    private void simpanData(ActionEvent event) {
        String id = fieldId.getText();
        String nama = fieldNama.getText();
        String kategori = comboKategori.getValue();
        String tipe = comboTipe.getValue();
        String stokStr = fieldStok.getText();

        // 3. Validasi Input Kosong
        if (id.isEmpty() || nama.isEmpty() || kategori == null || tipe == null || stokStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom harus diisi!");
            return;
        }

        // 4. Validasi Tipe Angka untuk Stok
        int stok;
        try {
            stok = Integer.parseInt(stokStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok harus berupa angka yang valid!");
            return;
        }

        // 5. Eksekusi Query Insert/Update
        String sql;
        if (isEditMode) {
            sql = "UPDATE barang SET nama_barang=?, kategori=?, tipe_barang=?, stok_tersedia=? WHERE id_barang=?";
        } else {
            sql = "INSERT INTO barang (id_barang, nama_barang, kategori, tipe_barang, stok_tersedia, sedang_dipinjam) VALUES (?, ?, ?, ?, ?, 0)";
        }
        // Gunakan transaksi agar jika salah satu gagal, kedua tabel dibatalkan (rollback)
        Connection conn = Database.getConnection();
        try {
            conn.setAutoCommit(false);

            try {
                if (isEditMode) {
                    // 1. Update HANYA identitas barang (stok jangan di-update langsung)
                    String sqlUpdateBarang = "UPDATE barang SET nama_barang=?, kategori=?, tipe_barang=? WHERE id_barang=?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateBarang)) {
                        pstmt.setString(1, nama);
                        pstmt.setString(2, kategori);
                        pstmt.setString(3, tipe);
                        pstmt.setString(4, id);
                        pstmt.executeUpdate();
                    }

                    // 2. Hitung selisih stok untuk dicatat ke riwayat
                    int selisih = stok - stokLama;

                    if (selisih != 0) {
                        // Jika selisih positif (nambah) = Ditambahkan. Jika negatif (berkurang) = Dihapus/Dipakai
                        String jenis = (selisih > 0) ? "Ditambahkan" : "Dihapus";
                        int jumlahMutasi = Math.abs(selisih); // Hilangkan nilai minus
                        String prefixEdit = (selisih > 0) ? "ADD" : "DEL";
                        String idTrans = generateIdTransaksi(conn, prefixEdit);
                        String namaAdmin = UserSession.getActiveNamaLengkap();

                        String sqlRiwayat = "INSERT INTO riwayat_transaksi (id_transaksi, id_barang, pelaku, jenis_transaksi, jumlah, keterangan) VALUES (?, ?, ?, ?, ?, 'Penyesuaian stok manual (Edit Alat)')";
                        try (PreparedStatement pstmtRiwayat = conn.prepareStatement(sqlRiwayat)) {
                            pstmtRiwayat.setString(1, idTrans);
                            pstmtRiwayat.setString(2, id);
                            pstmtRiwayat.setString(3, (namaAdmin != null) ? namaAdmin : "Admin");
                            pstmtRiwayat.setString(4, jenis);
                            pstmtRiwayat.setInt(5, jumlahMutasi);
                            pstmtRiwayat.executeUpdate();
                        }
                    }
                } else {
                    // LOGIKA TAMBAH BARU (INSERT)
                    // 1. Daftarkan identitas barang dengan stok fix = 0
                    String sqlBarang = "INSERT INTO barang (id_barang, nama_barang, kategori, tipe_barang, stok_tersedia, sedang_dipinjam) VALUES (?, ?, ?, ?, 0, 0)";
                    try (PreparedStatement pstmtBarang = conn.prepareStatement(sqlBarang)) {
                        pstmtBarang.setString(1, id);
                        pstmtBarang.setString(2, nama);
                        pstmtBarang.setString(3, kategori);
                        pstmtBarang.setString(4, tipe);
                        pstmtBarang.executeUpdate();
                    }

                    // 2. Insert ke riwayat_transaksi untuk memicu trigger dan mengisi stok
                    String idTrans = generateIdTransaksi(conn, "ADD");
                    String namaAdmin = UserSession.getActiveNamaLengkap();

                    String sqlRiwayat = "INSERT INTO riwayat_transaksi (id_transaksi, id_barang, pelaku, jenis_transaksi, jumlah, keterangan) VALUES (?, ?, ?, 'Ditambahkan', ?, 'Registrasi alat baru')";
                    try (PreparedStatement pstmtRiwayat = conn.prepareStatement(sqlRiwayat)) {
                        pstmtRiwayat.setString(1, idTrans);
                        pstmtRiwayat.setString(2, id);
                        pstmtRiwayat.setString(3, (namaAdmin != null) ? namaAdmin : "Admin");
                        pstmtRiwayat.setInt(4, stok);
                        pstmtRiwayat.executeUpdate();
                    }
                }

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", isEditMode ? "Data berhasil diupdate!" : "Data alat berhasil didaftarkan!");
                tutupModal(event);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Gagal menyimpan data: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error SQL", "Gagal menyimpan data ke database:\n" + e.getMessage());
        }
    } // PERBAIKAN: Penambahan kurung kurawal tutup untuk metode simpanData

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
