/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ini.eprojectuas.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // Sesuaikan URL, username, dan password dengan database Anda
    // Contoh untuk PostgreSQL: jdbc:postgresql://localhost:5432/gudang_digital
    // Contoh untuk MySQL: jdbc:mysql://localhost:3306/gudang_digital
    
    private static final String URL = "jdbc:mysql://localhost:3306/gudtik";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi Database Berhasil!");
            } catch (SQLException e) {
                System.out.println("Koneksi Database Gagal: " + e.getMessage());
            }
        }
        return connection;
    }
}