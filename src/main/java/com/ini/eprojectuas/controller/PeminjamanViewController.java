package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.model.Peminjaman;
import com.ini.eprojectuas.utils.Database;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class PeminjamanViewController implements Initializable {

    @FXML
    private StackPane searchBtn;
    @FXML
    private ComboBox<String> cbSearch;
    @FXML
    private TextField fieldSearch;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private ComboBox<String> cbDate;

    @FXML
    private Button btnTambah;
    @FXML
    private Button btnPemakaian;
    @FXML
    private Button btnRefresh;

    @FXML
    private TableView<Peminjaman> tabelAlat;
    @FXML
    private TableColumn<Peminjaman, String> colId;
    @FXML
    private TableColumn<Peminjaman, String> colNimPeminjam;
    @FXML
    private TableColumn<Peminjaman, String> colNamaBarang;
    @FXML
    private TableColumn<Peminjaman, String> colKategori;
    @FXML
    private TableColumn<Peminjaman, Integer> colJumlah;
    @FXML
    private TableColumn<Peminjaman, String> colStatus;
    @FXML
    private TableColumn<Peminjaman, String> colTanggalPinjam;
    @FXML
    private TableColumn<Peminjaman, String> colTanggalTenggat;
    @FXML
    private TableColumn<Peminjaman, String> colTanggalKembali;
    @FXML
    private TableColumn<Peminjaman, Void> colAksi;

    private ObservableList<Peminjaman> listPeminjaman = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnRefresh.setOnAction(e -> loadTableData());
        btnTambah.setOnAction(e -> bukaModalTambah());
        btnPemakaian.setOnAction(e -> bukaModalPakai());

        setupTableColumns();
        setupTombolAksi();
        setupFilterDanSearch();
        loadTableData();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPeminjaman"));
        colNimPeminjam.setCellValueFactory(new PropertyValueFactory<>("nimPeminjam"));
        colNamaBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colTanggalPinjam.setCellValueFactory(new PropertyValueFactory<>("tanggalPinjamUI"));
        colTanggalTenggat.setCellValueFactory(new PropertyValueFactory<>("tanggalTenggatUI"));
        colTanggalKembali.setCellValueFactory(new PropertyValueFactory<>("tanggalKembaliUI"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Modifikasi cellFactory untuk kolom Status agar berwarna
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Peminjaman, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setMaxWidth(Double.MAX_VALUE);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);

                    String baseStyle = "-fx-padding: 4px; -fx-background-radius: 5px; -fx-font-weight: bold; ";

                    if (item.equalsIgnoreCase("Selesai")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;");
                    } else if (item.equalsIgnoreCase("Dipinjam")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #FEF08A; -fx-text-fill: #A16207;");
                    } else if (item.equalsIgnoreCase("Rusak") || item.equalsIgnoreCase("Hilang")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;");
                    } else {
                        badge.setStyle(baseStyle + "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;");
                    }

                    setGraphic(badge);
                    setText(null);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

    private void terapkanFilter() {
        String keyword = fieldSearch.getText().trim().toLowerCase();
        String kolom = cbSearch.getValue();    // "NIM" atau "ID Transaksi"
        String status = cbStatus.getValue();    // "Semua", "Dipinjam", dst
        String rentangTgl = cbDate.getValue();      // "Semua", "7 Hari Terakhir", dst

        // Hitung batas tanggal
        java.time.LocalDateTime batasTgl = null;
        java.time.LocalDateTime sekarang = java.time.LocalDateTime.now();
        if ("7 Hari Terakhir".equals(rentangTgl)) {
            batasTgl = sekarang.minusDays(7);
        } else if ("30 Hari Terakhir".equals(rentangTgl)) {
            batasTgl = sekarang.minusDays(30);
        } else if ("Bulan Ini".equals(rentangTgl)) {
            batasTgl = sekarang.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        final java.time.LocalDateTime batasTglFinal = batasTgl;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        ObservableList<Peminjaman> hasil = listPeminjaman.filtered(p -> {

            // 1. Filter keyword (NIM atau ID)
            boolean lolosKeyword = true;
            if (!keyword.isEmpty()) {
                if ("NIM".equals(kolom)) {
                    lolosKeyword = p.getNimPeminjam() != null
                            && p.getNimPeminjam().toLowerCase().contains(keyword);
                } else { // ID Transaksi
                    lolosKeyword = p.getIdPeminjaman() != null
                            && p.getIdPeminjaman().toLowerCase().contains(keyword);
                }
            }

            // 2. Filter status
            boolean lolosStatus = "Semua".equals(status) || status.equals(p.getStatus());

            // 3. Filter tanggal (parse dari tanggalPinjamUI yang sudah diformat)
            boolean lolosTgl = true;
            if (batasTglFinal != null && p.getTanggalPinjamUI() != null && !p.getTanggalPinjamUI().isEmpty()) {
                try {
                    java.time.LocalDateTime tglPinjam = java.time.LocalDateTime.parse(
                            p.getTanggalPinjamUI(), formatter
                    );
                    lolosTgl = !tglPinjam.isBefore(batasTglFinal);
                } catch (Exception ex) {
                    lolosTgl = false;
                }
            }

            return lolosKeyword && lolosStatus && lolosTgl;
        });

        tabelAlat.setItems(hasil);
    }

    private void setupFilterDanSearch() {
        // Isi pilihan kolom pencarian
        cbSearch.setItems(FXCollections.observableArrayList("NIM", "ID Transaksi"));
        cbSearch.setValue("NIM"); // default

        // Isi pilihan filter status
        cbStatus.setItems(FXCollections.observableArrayList("Semua", "Dipinjam", "Selesai", "Rusak", "Hilang"));
        cbStatus.setValue("Semua");

        // Isi pilihan filter tanggal
        cbDate.setItems(FXCollections.observableArrayList("Semua", "7 Hari Terakhir", "30 Hari Terakhir", "Bulan Ini"));
        cbDate.setValue("Semua");

        // Tombol search
        fieldSearch.textProperty().addListener((observable, oldValue, newValue) -> terapkanFilter());
        // Filter otomatis saat combobox berubah
        cbStatus.setOnAction(e -> terapkanFilter());
        cbDate.setOnAction(e -> terapkanFilter());
    }

    private void setupTombolAksi() {
        Callback<TableColumn<Peminjaman, Void>, TableCell<Peminjaman, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Peminjaman, Void> call(final TableColumn<Peminjaman, Void> param) {
                return new TableCell<>() {
                    private final Button btnUpdate = new Button("Update");

                    {
                        btnUpdate.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                        btnUpdate.setOnAction((ActionEvent event) -> {
                            Peminjaman data = getTableView().getItems().get(getIndex());
                            bukaModalUpdate(data);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Peminjaman data = getTableView().getItems().get(getIndex());
                            // Sembunyikan tombol jika statusnya Selesai ATAU ID-nya berawalan PAK- (Pemakaian)
                            if ("Selesai".equals(data.getStatus()) || "Rusak".equals(data.getStatus()) || "Hilang".equals(data.getStatus()) || data.getIdPeminjaman().startsWith("PAK-")) {
                                setGraphic(null);
                            } else {
                                setGraphic(btnUpdate);
                            }
                        }
                    }
                };
            }
        };
        colAksi.setCellFactory(cellFactory);
    }

    private void loadTableData() {
        listPeminjaman.clear();

        // ✅ JOIN langsung ke barang, tidak perlu fillBarangDetails lagi
        String sql = "SELECT p.id_peminjaman AS id_trans, p.id_barang, p.nim_peminjam AS nim, "
                + "       p.jumlah, p.status, p.tanggal_pinjam AS tgl, "
                + "       p.tenggat_waktu AS tgl_tenggat, p.tanggal_kembali AS tgl_kembali, "
                + "       IFNULL(b.nama_barang, '(Barang Dihapus)') AS nama_barang, "
                + "       IFNULL(b.kategori, '-') AS kategori "
                + "FROM peminjaman p "
                + "LEFT JOIN barang b ON p.id_barang = b.id_barang "
                + "UNION ALL "
                + "SELECT pk.id_pemakaian AS id_trans, pk.id_barang, pk.nim_pemakai AS nim, "
                + "       pk.jumlah, 'Selesai' AS status, pk.tanggal_pakai AS tgl, "
                + "       NULL AS tgl_tenggat, NULL AS tgl_kembali, "
                + "       IFNULL(b.nama_barang, '(Barang Dihapus)') AS nama_barang, "
                + "       IFNULL(b.kategori, '-') AS kategori "
                + "FROM pemakaian pk "
                + "LEFT JOIN barang b ON pk.id_barang = b.id_barang "
                + "ORDER BY tgl DESC";

        Connection conn = Database.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                Peminjaman p = new Peminjaman();
                p.setIdPeminjaman(rs.getString("id_trans"));
                p.setNimPeminjam(rs.getString("nim"));
                p.setJumlah(rs.getInt("jumlah"));
                p.setStatus(rs.getString("status"));
                p.setNamaBarang(rs.getString("nama_barang")); // ✅ langsung dari JOIN
                p.setKategori(rs.getString("kategori"));      // ✅ langsung dari JOIN

                if (rs.getTimestamp("tgl") != null) {
                    p.setTanggalPinjamUI(rs.getTimestamp("tgl").toLocalDateTime().format(formatter));
                }
                if (rs.getTimestamp("tgl_tenggat") != null) {
                    p.setTanggalTenggatUI(rs.getTimestamp("tgl_tenggat").toLocalDateTime().format(formatter));
                } else {
                    p.setTanggalTenggatUI("-");
                }
                if (rs.getTimestamp("tgl_kembali") != null) {
                    p.setTanggalKembaliUI(rs.getTimestamp("tgl_kembali").toLocalDateTime().format(formatter));
                } else {
                    p.setTanggalKembaliUI("-");
                }

                listPeminjaman.add(p);
            }

            tabelAlat.setItems(listPeminjaman); // ✅ cukup satu kali di sini

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data transaksi: " + e.getMessage());
        }
    }

    // Method Helper untuk mengambil detail barang (nama dan kategori)
    private void fillBarangDetails(Peminjaman p, String idBarang) {
        String sql = "SELECT nama_barang, kategori FROM barang WHERE id_barang = ?";
        Connection conn = Database.getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idBarang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setNamaBarang(rs.getString("nama_barang"));
                    p.setKategori(rs.getString("kategori"));
                } else {
                    p.setNamaBarang("(Barang Dihapus)");
                    p.setKategori("-");
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal load detail barang: " + e.getMessage());
        }
    }

    private void bukaModalTambah() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/TambahPeminjamanView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("GudTIK");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTableData();
            tabelAlat.requestFocus();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bukaModalPakai() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/TambahPemakaianView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("GudTIK");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTableData();
            tabelAlat.requestFocus();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bukaModalUpdate(Peminjaman data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/UpdatePeminjamanView.fxml"));
            Parent root = loader.load();

            UpdatePeminjamanViewController controller = loader.getController();
            controller.setDataPeminjaman(data);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("GudTIK");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTableData();
            tabelAlat.requestFocus();

        } catch (IOException e) {
            e.printStackTrace();
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
