package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.model.Barang;
import com.ini.eprojectuas.model.Kategori;
import com.ini.eprojectuas.model.TipeBarang;
import com.ini.eprojectuas.utils.Database;
import com.ini.eprojectuas.utils.UserSession;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

public class DaftarAlatController implements Initializable {

    @FXML
    private Button btnTambah;
    @FXML
    private Button btnRefresh;
    @FXML
    private TextField fieldSearch;
    @FXML
    private ComboBox<String> cbKategori;
    @FXML
    private ComboBox<String> cbKetersediaan;
    @FXML
    private TableView<Barang> tabelAlat;
    @FXML
    private TableColumn<Barang, String> colId;
    @FXML
    private TableColumn<Barang, String> colNama;
    @FXML
    private TableColumn<Barang, Kategori> colKategori;
    @FXML
    private TableColumn<Barang, Integer> colStok;
    @FXML
    private TableColumn<Barang, TipeBarang> colPeminjaman;
    @FXML
    private TableColumn<Barang, Integer> colJumlahTersedia;
    @FXML
    private TableColumn<Barang, String> colTersedia;
    @FXML
    private TableColumn<Barang, Void> colAksi;

    private ObservableList<Barang> listBarang = FXCollections.observableArrayList();
    private FilteredList<Barang> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnTambah.setOnAction(event -> tampilkanModalTambah());
        btnRefresh.setOnAction(event -> handleRefresh());
        initTable();
        loadTableData();
        setupFilter();
    }

    private void terapkanFilter() {
        filteredData.setPredicate(barang -> {
            // Ambil nilai dari inputan UI
            String keyword = fieldSearch.getText() == null ? "" : fieldSearch.getText().toLowerCase();
            String filterKategori = cbKategori.getValue();
            String filterKetersediaan = cbKetersediaan.getValue();

            // 1. Cek Pencarian (Search berdasarkan Nama atau ID)
            boolean cocokSearch = true;
            if (!keyword.isEmpty()) {
                cocokSearch = barang.getNamaBarang().toLowerCase().contains(keyword)
                        || barang.getIdBarang().toLowerCase().contains(keyword);
            }

            // 2. Cek Kategori
            boolean cocokKategori = true;
            if (filterKategori != null && !filterKategori.equals("Semua")) {
                cocokKategori = barang.getKategori().name().equalsIgnoreCase(filterKategori);
            }

            // 3. Cek Ketersediaan (Kondisi Saat Ini)
            boolean cocokKetersediaan = true;
            if (filterKetersediaan != null && !filterKetersediaan.equals("Semua")) {
                cocokKetersediaan = barang.getKondisiSaatIni().equalsIgnoreCase(filterKetersediaan);
            }

            // Baris data akan ditampilkan JIKA SEMUA kondisi bernilai true
            return cocokSearch && cocokKategori && cocokKetersediaan;
        });
    }

    private void setupFilter() {
        // 1. Isi pilihan ComboBox
        cbKategori.setItems(FXCollections.observableArrayList("Semua", "NETWORKING", "HARDWARE", "TOOLS"));
        cbKategori.setValue("Semua");

        cbKetersediaan.setItems(FXCollections.observableArrayList("Semua", "Tersedia", "Dipinjam", "Habis"));
        cbKetersediaan.setValue("Semua");

        // 2. Bungkus listBarang ke dalam FilteredList
        filteredData = new FilteredList<>(listBarang, b -> true);

        // 3. Tambahkan Listener agar filter berjalan otomatis saat input berubah
        fieldSearch.textProperty().addListener((observable, oldValue, newValue) -> terapkanFilter());
        cbKategori.valueProperty().addListener((observable, oldValue, newValue) -> terapkanFilter());
        cbKetersediaan.valueProperty().addListener((observable, oldValue, newValue) -> terapkanFilter());

        // 4. Bungkus FilteredList ke SortedList agar tabel tetap bisa di-sorting dengan klik header kolom
        SortedList<Barang> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabelAlat.comparatorProperty());

        // 5. Masukkan data yang sudah di-filter ke tabel (MENGGANTIKAN tabelAlat.setItems(listBarang))
        tabelAlat.setItems(sortedData);
    }

    private void handleRefresh() {
        // 1. Reset input pencarian dan filter ke kondisi awal
        if (fieldSearch != null) {
            fieldSearch.clear();
        }
        if (cbKategori != null) {
            cbKategori.setValue("Semua");
        }
        if (cbKetersediaan != null) {
            cbKetersediaan.setValue("Semua");
        }

        // 2. Kosongkan list dan ambil data terbaru dari database
        if (listBarang != null) {
            listBarang.clear();
        }
        loadTableData();
    }

    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stokTersedia"));
        colPeminjaman.setCellValueFactory(new PropertyValueFactory<>("tipeBarang"));
        colJumlahTersedia.setCellValueFactory(new PropertyValueFactory<>("sedangDipinjam"));
        colTersedia.setCellValueFactory(new PropertyValueFactory<>("kondisiSaatIni"));

        // Format Badge untuk kolom Status
        colTersedia.setCellFactory(column -> new TableCell<Barang, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String baseStyle = "-fx-padding: 4px 12px; -fx-background-radius: 15px; -fx-font-weight: bold; ";

                    if (item.equalsIgnoreCase("Tersedia")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;"); // Hijau
                    } else if (item.equalsIgnoreCase("Dipinjam")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #FEF08A; -fx-text-fill: #A16207;"); // Kuning
                    } else if (item.equalsIgnoreCase("Habis")) {
                        badge.setStyle(baseStyle + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;"); // Merah
                    } else {
                        badge.setStyle(baseStyle + "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;"); // Abu-abu
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Setup Tombol Aksi (Edit & Hapus)
        colAksi.setCellFactory(param -> new TableCell<Barang, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnHapus = new Button("Hapus");
            private final HBox pane = new HBox(10, btnEdit, btnHapus);

            {
                pane.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5px;");
                btnHapus.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5px;");

                btnEdit.setOnAction(event -> {
                    Barang b = getTableView().getItems().get(getIndex());
                    tampilkanModalEdit(b);
                });

                btnHapus.setOnAction(event -> {
                    Barang b = getTableView().getItems().get(getIndex());
                    hapusDataAlat(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadTableData() {
        listBarang.clear();
        String sql = "SELECT id_barang, nama_barang, kategori, tipe_barang, stok_tersedia, sedang_dipinjam FROM barang";

        Connection conn = Database.getConnection();
        if (conn != null) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    // Penentuan String Kondisi secara manual jika konstruktor tidak menanganinya
                    int stokTersedia = rs.getInt("stok_tersedia");
                    int sedangDipinjam = rs.getInt("sedang_dipinjam");

                    String kondisiUI;
                    if (stokTersedia > 0) {
                        kondisiUI = "Tersedia";
                    } else if (stokTersedia == 0 && sedangDipinjam > 0) {
                        kondisiUI = "Dipinjam";
                    } else {
                        kondisiUI = "Habis";
                    }

                    Barang barang = new Barang(
                            rs.getString("id_barang"),
                            rs.getString("nama_barang"),
                            Kategori.valueOf(rs.getString("kategori")),
                            TipeBarang.valueOf(rs.getString("tipe_barang")),
                            stokTersedia,
                            sedangDipinjam,
                            kondisiUI
                    );

                    listBarang.add(barang);
                }

            } catch (SQLException e) {
                System.out.println("Error SQL Daftar Alat: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fatal Error");
                alert.setHeaderText("Terjadi Kesalahan Database");
                alert.setContentText("Aplikasi gagal memuat atau menyimpan data. Program akan ditutup.\n\nDetail: " + e.getMessage());

                alert.showAndWait();

                javafx.application.Platform.exit();
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.out.println("Error Enum Daftar Alat: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Terjadi Kesalahan Input");
                alert.setContentText("Input tidak sesuai.\n\nDetail: " + e.getMessage());

                alert.showAndWait();

            }
        }
    }

    private void hapusDataAlat(Barang barang) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Menghapus: " + barang.getNamaBarang());
        alert.setContentText("Apakah Anda yakin ingin menghapus alat ini secara permanen dari database?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = Database.getConnection();
            try {
                conn.setAutoCommit(false);

                // 1. Catat ke riwayat DULU (id_barang masih valid)
                String idTrans = generateIdRiwayat(conn, "DEL");
                String namaAdmin = UserSession.getActiveNamaLengkap();
                String keterangan = "Data alat [" + barang.getIdBarang() + "] "
                        + barang.getNamaBarang() + " dihapus permanen.";

                String sqlLog = "INSERT INTO riwayat_transaksi "
                        + "(id_transaksi, id_barang, pelaku, jenis_transaksi, jumlah, keterangan) "
                        + "VALUES (?, ?, ?, 'Dihapus', ?, ?)";
                try (PreparedStatement psLog = conn.prepareStatement(sqlLog)) {
                    psLog.setString(1, idTrans);
                    psLog.setString(2, barang.getIdBarang());
                    psLog.setString(3, (namaAdmin != null) ? namaAdmin : "Admin");
                    psLog.setInt(4, barang.getStokTersedia());
                    psLog.setString(5, keterangan);
                    psLog.executeUpdate();
                }

                // 2. Baru DELETE barang
                String sqlDel = "DELETE FROM barang WHERE id_barang = ?";
                int rowsDeleted;
                try (PreparedStatement psDel = conn.prepareStatement(sqlDel)) {
                    psDel.setString(1, barang.getIdBarang());
                    rowsDeleted = psDel.executeUpdate();
                }

                conn.commit();

                if (rowsDeleted > 0) {
                    loadTableData();
                }

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                System.err.println("Gagal menghapus data: " + e.getMessage());
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Gagal Menghapus Data");
                errorAlert.setContentText("Terjadi kesalahan SQL: " + e.getMessage());
                errorAlert.showAndWait();
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private String generateIdRiwayat(Connection conn, String prefix) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(id_transaksi, ?) AS UNSIGNED)) "
                + "FROM riwayat_transaksi "
                + "WHERE id_transaksi LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, prefix.length() + 2);
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

    private void tampilkanModalTambah() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/TambahView.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle("GudTIK");
            try {
                Image applicationIcon = new Image(getClass().getResourceAsStream("/com/ini/eprojectuas/assets/logo128.png"));
                modalStage.getIcons().add(applicationIcon);
            } catch (Exception e) {
                System.out.println("Ikon gagal dimuat: " + e.getMessage());
            }
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(btnTambah.getScene().getWindow());
            modalStage.showAndWait();
            tabelAlat.requestFocus();

            loadTableData();
        } catch (IOException e) {
            System.out.println("Gagal memuat modal tambah: " + e.getMessage());
        }
    }

    private void tampilkanModalEdit(Barang barangYangDipilih) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/TambahView.fxml"));
            Parent root = loader.load();

            // Ambil controller dari modal
            TambahViewController controller = loader.getController();

            // Kirim data ke modal (ini yang membuat field otomatis terisi)
            controller.setEditData(barangYangDipilih);

            Stage modalStage = new Stage();
            modalStage.setTitle("Edit Data Alat");
            try {
                Image applicationIcon = new Image(getClass().getResourceAsStream("/com/ini/eprojectuas/assets/logo128.png"));
                modalStage.getIcons().add(applicationIcon);
            } catch (Exception e) {
                System.out.println("Ikon gagal dimuat: " + e.getMessage());
            }
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();

            // Refresh tabel setelah modal ditutup
            loadTableData();

        } catch (IOException e) {
            System.err.println("Gagal memuat form edit: " + e.getMessage());
        }
    }
}
