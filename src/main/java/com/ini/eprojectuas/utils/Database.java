/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ini.eprojectuas.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class Database {    
    private static final String URL = "jdbc:mysql://localhost:3306/gud_tik";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null ) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi Database Berhasil!");
            } catch (SQLException e) {
                Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fatal Error");
                alert.setHeaderText("Koneksi Database Gagal");
                alert.setContentText("Aplikasi tidak dapat terhubung ke database. Pastikan server MySQL sudah berjalan.\n\nDetail: " + e.getMessage());
                
                alert.showAndWait(); 
                

                Platform.exit(); 
                System.exit(1);  
            });
            }
        }
        return connection;
    }
}