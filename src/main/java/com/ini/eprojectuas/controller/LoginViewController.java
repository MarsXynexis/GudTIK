/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.utils.Database;
import com.ini.eprojectuas.utils.UserSession;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class LoginViewController implements Initializable {

    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnGoToRegister;
    @FXML
    private Label labelError;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnLogin.setOnAction(this::handleLogin);
        btnGoToRegister.setOnAction(this::bukaHalamanRegister);
    }

    private void bukaHalamanRegister(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/RegisterView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("GudTIK");
            stage.centerOnScreen();

            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/ini/eprojectuas/assets/icon.png")));
            } catch (Exception e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(ActionEvent event) {
        String username = fieldUsername.getText();
        String password = fieldPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            tampilkanError("Username dan Password tidak boleh kosong!");
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = Database.getConnection();
        try (
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Login Berhasil - Set Session
                    String idDb = rs.getString("id_user");
                    String userDb = rs.getString("username");
                    String namaDb = rs.getString("nama_lengkap");

                    UserSession.setLogin(idDb, userDb, namaDb);

                    // Pindah ke halaman utama
                    pindahKeMainView(event);
                } else {
                    // Login Gagal
                    tampilkanError("Username atau Password salah!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            tampilkanError("Koneksi database gagal!");
        }
    }

    private void tampilkanError(String pesan) {
        labelError.setText(pesan);
        labelError.setVisible(true);
    }

    private void pindahKeMainView(ActionEvent event) {
        try {
            // Sesuaikan path ini dengan lokasi MainView.fxml Anda
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/MainView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("GudTIK");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            tampilkanError("Gagal memuat halaman utama!");
        }
    }
}
