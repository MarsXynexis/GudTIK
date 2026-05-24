/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ini.eprojectuas.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import com.ini.eprojectuas.utils.Database;
import com.ini.eprojectuas.model.Barang;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * FXML Controller class
 *
 * @author raziq
 */
public class DashboardViewController implements Initializable {

    private ObservableList<Barang> listBarang = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
