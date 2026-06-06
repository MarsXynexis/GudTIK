/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import com.ini.eprojectuas.utils.UserSession;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class MainViewController implements Initializable {

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnDaftarAlat;
    @FXML
    private Button btnPeminjaman;
    @FXML
    private Button btnLaporan;
    @FXML
    private Button btnLogout;
    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (UserSession.getActiveIdUser() == null) {
            System.err.println("Akses ditolak: Tidak ada session aktif.");
            kembaliKeLogin();
            return;
        }

        loadPage("DashboardView.fxml");

        btnDashboard.setOnAction(e -> loadPage("DashboardView.fxml"));
        btnDaftarAlat.setOnAction(e -> loadPage("DaftarAlatView.fxml"));
        btnPeminjaman.setOnAction(e -> loadPage("PeminjamanView.fxml"));
        btnLaporan.setOnAction(e -> loadPage("LaporanView.fxml"));
        btnLogout.setOnAction(e -> handleLogout()); // ← tambahkan ini
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText("Apakah kamu yakin ingin keluar?");
        alert.setContentText("Sesi kamu akan diakhiri.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserSession.clearSession();  // Hapus session
            kembaliKeLogin();            // Redirect ke login
        }
    }

    private void loadPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/ini/eprojectuas/view/" + fxml));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            System.out.println("Gagal memuat " + fxml + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void kembaliKeLogin() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/LoginView.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root));
                stage.setTitle("GudTIK");
                stage.centerOnScreen();
                try {
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/ini/eprojectuas/assets/icon.png")));
                } catch (Exception e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
