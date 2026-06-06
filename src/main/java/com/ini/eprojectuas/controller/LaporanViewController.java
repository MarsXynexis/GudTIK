/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.model.RiwayatTransaksi;
import com.ini.eprojectuas.utils.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class LaporanViewController implements Initializable {

    @FXML
    private DatePicker dpMulai;
    @FXML
    private DatePicker dpSelesai;
    @FXML
    private ComboBox<String> cbJenis;
    @FXML
    private Button btnFilter;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnCetak;

    @FXML
    private TableView<RiwayatTransaksi> tabelLaporan;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colId;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colNama;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colJenis;
    @FXML
    private TableColumn<RiwayatTransaksi, Integer> colJumlah;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colPelaku;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colTanggal;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colKeterangan;

    private ObservableList<RiwayatTransaksi> listLaporan = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbJenis.setItems(FXCollections.observableArrayList(
                "Semua", "Ditambahkan", "Dipakai", "Dipinjam", "Dikembalikan", "Rusak", "Hilang", "Dihapus"
        ));
        cbJenis.setValue("Semua");

        initTableColumns();
        loadFilteredData();

        btnFilter.setOnAction(e -> loadFilteredData());
        btnReset.setOnAction(e -> handleReset());
        btnCetak.setOnAction(this::handleCetak);
    }

    private void initTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colPelaku.setCellValueFactory(new PropertyValueFactory<>("pelaku"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalTransaksiUI"));
        colKeterangan.setCellValueFactory(new PropertyValueFactory<>("keterangan"));

        // ✅ Kolom jenis transaksi dengan badge warna
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenisTransaksi"));
        colJenis.setCellFactory(column -> new TableCell<RiwayatTransaksi, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                javafx.scene.control.Label badge = new javafx.scene.control.Label(item);
                badge.setMaxWidth(Double.MAX_VALUE);
                badge.setAlignment(javafx.geometry.Pos.CENTER);

                String base = "-fx-padding: 4px 8px; -fx-background-radius: 5px; -fx-font-weight: bold; ";

                if ("Ditambahkan".equals(item)) {
                    badge.setStyle(base + "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;");
                } else if ("Dipinjam".equals(item)) {
                    badge.setStyle(base + "-fx-background-color: #FEF08A; -fx-text-fill: #A16207;");
                } else if ("Dikembalikan".equals(item)) {
                    badge.setStyle(base + "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8;");
                } else if ("Dipakai".equals(item)) {
                    badge.setStyle(base + "-fx-background-color: #FFEDD5; -fx-text-fill: #C2410C;");
                } else if ("Rusak".equals(item) || "Hilang".equals(item)) {
                    badge.setStyle(base + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;");
                } else if ("Dihapus".equals(item)) {
                    badge.setStyle(base + "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;");
                } else {
                    badge.setStyle(base + "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;");
                }

                setGraphic(badge);
                setText(null);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
    }

    private void loadFilteredData() {
        listLaporan.clear();

        LocalDate tglMulai = dpMulai.getValue();
        LocalDate tglSelesai = dpSelesai.getValue();
        String jenisPilihan = cbJenis.getValue();

        StringBuilder sql = new StringBuilder(
                "SELECT rt.*, b.nama_barang FROM riwayat_transaksi rt "
                + "LEFT JOIN barang b ON rt.id_barang = b.id_barang WHERE 1=1 "
        );

        if (jenisPilihan != null && !jenisPilihan.equals("Semua")) {
            sql.append("AND rt.jenis_transaksi = ? ");
        }
        if (tglMulai != null) {
            sql.append("AND rt.tanggal_transaksi >= ? ");
        }
        if (tglSelesai != null) {
            sql.append("AND rt.tanggal_transaksi <= ? ");
        }
        sql.append("ORDER BY rt.tanggal_transaksi DESC");

        Connection conn = Database.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (jenisPilihan != null && !jenisPilihan.equals("Semua")) {
                pstmt.setString(paramIndex++, jenisPilihan);
            }
            if (tglMulai != null) {
                pstmt.setTimestamp(paramIndex++, Timestamp.valueOf(tglMulai.atStartOfDay()));
            }
            if (tglSelesai != null) {
                pstmt.setTimestamp(paramIndex++, Timestamp.valueOf(tglSelesai.atTime(23, 59, 59)));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    RiwayatTransaksi rt = new RiwayatTransaksi();
                    rt.setIdTransaksi(rs.getString("id_transaksi"));
                    rt.setJenisTransaksi(rs.getString("jenis_transaksi"));
                    rt.setJumlah(rs.getInt("jumlah"));
                    rt.setPelaku(rs.getString("pelaku"));
                    rt.setKeterangan(rs.getString("keterangan"));

                    String namaBarang = rs.getString("nama_barang");
                    if (namaBarang == null) {
                        String ket = rs.getString("keterangan");
                        if (ket != null && ket.contains("] ")) {
                            int start = ket.indexOf("] ") + 2;
                            int end = ket.indexOf(" dihapus permanen.");
                            namaBarang = (start > 1 && end > start) ? ket.substring(start, end) + " (Dihapus)" : "(Barang Dihapus)";
                        } else {
                            namaBarang = "(Barang Dihapus)";
                        }
                    }
                    rt.setNamaBarang(namaBarang);

                    if (rs.getTimestamp("tanggal_transaksi") != null) {
                        rt.setTanggalTransaksiUI(rs.getTimestamp("tanggal_transaksi").toLocalDateTime().format(formatter));
                    }

                    listLaporan.add(rt);
                }
            }
            tabelLaporan.setItems(listLaporan);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat laporan: " + e.getMessage());
        }
    }

    private void handleReset() {
        dpMulai.setValue(null);
        dpSelesai.setValue(null);
        cbJenis.setValue("Semua");
        loadFilteredData();
    }

    private void handleCetak(ActionEvent event) {
        if (listLaporan.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada data yang dapat dicetak!");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            Stage owner = (Stage) btnCetak.getScene().getWindow();
            boolean proceed = job.showPrintDialog(owner);

            if (proceed) {
                // Mencetak komponen TableView secara langsung ke printer / device print-to-PDF
                boolean success = job.printPage(tabelLaporan);
                if (success) {
                    job.endJob();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Dokumen laporan berhasil dikirim ke printer / PDF.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Proses cetak gagal.");
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Sistem cetak tidak ditemukan pada perangkat ini.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
