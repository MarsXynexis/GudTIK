/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.model.RiwayatTransaksi;
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.scene.control.Alert;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class DashboardViewController implements Initializable {

    @FXML
    private TableView<RiwayatTransaksi> tabelTransaksi;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colNama;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colJenis;
    @FXML
    private TableColumn<RiwayatTransaksi, String> colTanggal;
    @FXML
    private TableColumn<RiwayatTransaksi, Integer> colJumlah;

    @FXML
    private Label labelWelcome;
    @FXML
    private Label labelTanggal;
    @FXML
    private Label labelUser;

    @FXML
    private PieChart pieChart;
    @FXML
    private Label pieTersedia;
    @FXML
    private Label pieDipinjam;
    @FXML
    private Label pieRusak;
    @FXML
    private Label totalAlat;
    @FXML
    private Label tersedia;
    @FXML
    private Label dipinjam;
    @FXML
    private Label rusak;

    private ObservableList<RiwayatTransaksi> listTransaksi = FXCollections.observableArrayList();
    private String namaPengguna;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initHeader();
        initTable();
        loadTotalData();
        loadTableData();
    }

    private void initHeader() {
        LocalDate hariIni = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        labelTanggal.setText(hariIni.format(formatter));

        namaPengguna = UserSession.getActiveNamaLengkap();

        labelUser.setText(namaPengguna);
        labelWelcome.setText("Selamat datang, " + namaPengguna + "!");
    }

    private void initTable() {
        // Pemetaan properti model
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenisTransaksi"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalTransaksiUI"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));

        // Pewarnaan Badge untuk Kolom Transaksi
        // Pewarnaan Badge untuk Kolom Transaksi
        colJenis.setCellFactory(column -> new TableCell<RiwayatTransaksi, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);

                    // Kunci agar background melebar mengikuti kolom
                    badge.setMaxWidth(Double.MAX_VALUE);
                    badge.setAlignment(Pos.CENTER);

                    // Radius diubah jadi 5px agar tidak aneh saat ditarik panjang
                    String baseStyle = "-fx-padding: 4px; -fx-background-radius: 5px; -fx-font-weight: bold; ";

                    if (item.equalsIgnoreCase("Ditambahkan")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;");
                    } else if (item.equalsIgnoreCase("Dipinjam")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #FEF08A; -fx-text-fill: #A16207;");
                    } else if (item.equalsIgnoreCase("Rusak") || item.equalsIgnoreCase("Hilang")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;");
                    } else if (item.equalsIgnoreCase("Dikembalikan")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8;");
                    } else if (item.equalsIgnoreCase("Dipakai")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #FFEDD5; -fx-text-fill: #C2410C;");
                    } else {
                        badge.setStyle(baseStyle + "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;");
                    }

                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Paksa kolom jenis transaksi agar rata tengah secara keseluruhan
        colJenis.setStyle("-fx-alignment: CENTER;");
        colJenis.setStyle("-fx-padding: 0 5px;");
    }

    private void loadTotalData() {
        int jumlahTersedia = 0;
        int jumlahDipinjam = 0;
        int jumlahRusak = 0;

        Connection conn = Database.getConnection();
        if (conn != null) {
            try {
                ResultSet rsTersedia = conn.createStatement().executeQuery("SELECT SUM(stok_tersedia) FROM barang");
                if (rsTersedia.next()) {
                    jumlahTersedia = rsTersedia.getInt(1);
                }

                ResultSet rsPinjam = conn.createStatement().executeQuery("SELECT SUM(sedang_dipinjam) FROM barang");
                if (rsPinjam.next()) {
                    jumlahDipinjam = rsPinjam.getInt(1);
                }

                ResultSet rsRusak = conn.createStatement().executeQuery("SELECT SUM(jumlah) FROM riwayat_transaksi WHERE jenis_transaksi IN ('Rusak', 'Hilang')");
                if (rsRusak.next()) {
                    jumlahRusak = rsRusak.getInt(1);
                }

            } catch (SQLException e) {
                System.out.println("Gagal memuat data SQL: " + e.getMessage());

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fatal Error");
                alert.setHeaderText("Terjadi Kesalahan Database");
                alert.setContentText("Aplikasi gagal memuat atau menyimpan data. Program akan ditutup.\n\nDetail: " + e.getMessage());

                alert.showAndWait();

                javafx.application.Platform.exit();
                System.exit(1);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Tersedia", jumlahTersedia),
                new PieChart.Data("Dipinjam", jumlahDipinjam),
                new PieChart.Data("Rusak/Hilang", jumlahRusak)
        );
        pieChart.setData(pieChartData);

        double kalkulasi = jumlahTersedia + jumlahDipinjam + jumlahRusak;

        totalAlat.setText(String.valueOf((int) kalkulasi));
        tersedia.setText(String.valueOf(jumlahTersedia));
        dipinjam.setText(String.valueOf(jumlahDipinjam));
        rusak.setText(String.valueOf(jumlahRusak));

        if (kalkulasi > 0) {
            pieTersedia.setText(String.format("Tersedia : %.1f%%", (jumlahTersedia / kalkulasi) * 100));
            pieDipinjam.setText(String.format("Dipinjam : %.1f%%", (jumlahDipinjam / kalkulasi) * 100));
            pieRusak.setText(String.format("Rusak/Hilang : %.1f%%", (jumlahRusak / kalkulasi) * 100));
        } else {
            pieTersedia.setText("Tersedia : 0%");
            pieDipinjam.setText("Dipinjam : 0%");
            pieRusak.setText("Rusak : 0%");
        }
    }

    private void loadTableData() {
        listTransaksi.clear();

        // Menggunakan LEFT JOIN agar riwayat barang yang dihapus tetap muncul
        String sql = "SELECT rt.id_transaksi, rt.id_barang, b.nama_barang, rt.id_peminjaman, "
                   + "rt.pelaku, rt.jenis_transaksi, rt.jumlah, rt.tanggal_transaksi, rt.keterangan "
                   + "FROM riwayat_transaksi rt "
                   + "LEFT JOIN barang b ON rt.id_barang = b.id_barang "
                   + "ORDER BY rt.tanggal_transaksi DESC LIMIT 8";

        Connection conn = Database.getConnection();
        if (conn != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql); 
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    // 1. Deklarasi dan ambil data dari ResultSet (Perhatikan huruf besar/kecil di sini)
                    String idTransaksi = rs.getString("id_transaksi"); 
                    String namaBarang = rs.getString("nama_barang");
                    String idPeminjaman = rs.getString("id_peminjaman");
                    String pelaku = rs.getString("pelaku");
                    String jenisTransaksi = rs.getString("jenis_transaksi");
                    int jumlah = rs.getInt("jumlah");
                    String keterangan = rs.getString("keterangan");

                    // 2. Logika ekstraksi nama barang jika statusnya "Dihapus"
                    if (namaBarang == null && "Dihapus".equals(jenisTransaksi)) {
                        try {
                            int startIndex = keterangan.indexOf("] ") + 2;
                            int endIndex = keterangan.indexOf(" dihapus permanen.");
                            
                            if (startIndex > 1 && endIndex > startIndex) {
                                namaBarang = keterangan.substring(startIndex, endIndex) + " (Dihapus)";
                            } else {
                                namaBarang = "(Barang Dihapus)";
                            }
                        } catch (Exception e) {
                            namaBarang = "(Barang Dihapus)";
                        }
                    }
                    if (namaBarang == null) {
                            namaBarang = "(Barang Dihapus)";
                    }

                    // 3. Masukkan ke model menggunakan setter
                    RiwayatTransaksi transaksi = new RiwayatTransaksi();
                    transaksi.setIdTransaksi(idTransaksi); // Pemanggilan harus sama persis: idTransaksi
                    transaksi.setNamaBarang(namaBarang);
                    transaksi.setIdPeminjaman(idPeminjaman);
                    transaksi.setPelaku(pelaku);
                    transaksi.setJenisTransaksi(jenisTransaksi);
                    transaksi.setJumlah(jumlah);
                    transaksi.setKeterangan(keterangan);

                    if (rs.getTimestamp("tanggal_transaksi") != null) {
                        transaksi.setTanggalTransaksiUI(rs.getTimestamp("tanggal_transaksi").toLocalDateTime().format(formatter));
                    }

                    listTransaksi.add(transaksi);
                }

                tabelTransaksi.setItems(listTransaksi);

            } catch (SQLException e) {
                System.out.println("Error memuat data tabel transaksi: " + e.getMessage());

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fatal Error");
                alert.setHeaderText("Terjadi Kesalahan Database");
                alert.setContentText("Aplikasi gagal memuat atau menyimpan data.\n\nDetail: " + e.getMessage());

                alert.showAndWait();
            }
        }
    }
}
