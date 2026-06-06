/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import com.ini.eprojectuas.utils.Database;
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
import javafx.scene.control.Alert;
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
public class RegisterViewController implements Initializable {

    @FXML
    private TextField fieldNamaLengkap;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Button btnRegister;
    @FXML
    private Button btnKembali;
    @FXML
    private Label labelError;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnRegister.setOnAction(this::handleRegister);
        btnKembali.setOnAction(this::kembaliKeLogin);
    }

    private void handleRegister(ActionEvent event) {
        String namaLengkap = fieldNamaLengkap.getText().trim();
        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText().trim();

        // 1. Validasi Input Kosong
        if (namaLengkap.isEmpty() || username.isEmpty() || password.isEmpty()) {
            tampilkanError("Semua kolom wajib diisi!");
            return;
        }

        Connection conn = Database.getConnection();

        // 2. Cek apakah Username sudah digunakan
        String sqlCek = "SELECT username FROM users WHERE username = ?";
        try (PreparedStatement pstmtCek = conn.prepareStatement(sqlCek)) {
            pstmtCek.setString(1, username);
            try (ResultSet rs = pstmtCek.executeQuery()) {
                if (rs.next()) {
                    tampilkanError("Username sudah terdaftar! Pilih username lain.");
                    return;
                }
            }
        } catch (SQLException e) {
            tampilkanError("Terjadi kesalahan koneksi database.");
            e.printStackTrace();
            return;
        }

        // 3. Insert User Baru
        String idUser = "USR-" + System.currentTimeMillis();
        String sqlInsert = "INSERT INTO users (id_user, username, password, nama_lengkap) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
            pstmtInsert.setString(1, idUser);
            pstmtInsert.setString(2, username);
            pstmtInsert.setString(3, password);
            pstmtInsert.setString(4, namaLengkap);

            pstmtInsert.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registrasi Berhasil");
            alert.setHeaderText(null);
            alert.setContentText("Akun berhasil didaftarkan. Silakan login.");
            alert.showAndWait();

            kembaliKeLogin(event);

        } catch (SQLException e) {
            tampilkanError("Gagal mendaftarkan akun.");
            e.printStackTrace();
        }
    }

    private void tampilkanError(String pesan) {
        labelError.setText(pesan);
        labelError.setVisible(true);
    }

    private void kembaliKeLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ini/eprojectuas/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("GudTIK");
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/ini/eprojectuas/assets/icon.png")));
            } catch (Exception e) {
            }
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
