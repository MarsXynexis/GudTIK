package com.ini.eprojectuas.main;

import com.ini.eprojectuas.utils.UserSession;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage; // Simpan stage utama agar bisa diakses global (opsional)

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        String viewTarget;

        // Cek session: Jika belum login, ke LoginView. Jika sudah, ke MainView.
        if (UserSession.getActiveIdUser() != null) {
            viewTarget = "/com/ini/eprojectuas/view/MainView.fxml";
        } else {
            viewTarget = "/com/ini/eprojectuas/view/LoginView.fxml";

        }
        stage.setTitle("GudTIK");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(viewTarget));
        Parent root = loader.load();

        try {
            Image applicationIcon = new Image(getClass().getResourceAsStream("/com/ini/eprojectuas/assets/logo128.png"));
            primaryStage.getIcons().add(applicationIcon);
        } catch (Exception e) {
            System.out.println("Ikon gagal dimuat: " + e.getMessage());
        }
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
