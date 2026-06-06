/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ini.eprojectuas.utils;

/**
 *
 * @author raziq
 */
public class UserSession {
    private static String activeIdUser;
    private static String activeUsername;
    private static String activeNamaLengkap;

    // Dipanggil saat login berhasil
    public static void setLogin(String idUser, String username, String namaLengkap) {
        activeIdUser = idUser;
        activeUsername = username;
        activeNamaLengkap = namaLengkap;
    }

    public static String getActiveIdUser() {
        return activeIdUser;
    }

    public static String getActiveUsername() {
        return activeUsername;
    }

    public static String getActiveNamaLengkap() {
        return activeNamaLengkap;
    }

    // Dipanggil saat tombol logout ditekan
    public static void clearSession() {
        activeIdUser = null;
        activeUsername = null;
        activeNamaLengkap = null;
    }
}
