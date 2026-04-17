package tn.formini.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.utils.AdminInitializer;
import tn.formini.utils.StageWindowMode;
import java.io.File;
import java.net.URL;

public class MainMenuApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageWindowMode.installGlobalMaximizedPolicy();

        AdminInitializer.initializeAdmin();
        initializeSession();

        // Essaie plusieurs chemins

        // Essaie plusieurs chemins
        String[] chemins = {
                "/tn/formini/fxml/MainMenu.fxml",
                "/fxml/MainMenu.fxml",
                "/fxml/MainMenu.fxml",
                "tn/formini/fxml/MainMenu.fxml"
        };

        Parent root = null;
        for (String chemin : chemins) {
            try {
                System.out.println("Tentative: " + chemin);
                URL resource = getClass().getResource(chemin);
                if (resource != null) {
                    root = FXMLLoader.load(resource);
                    System.out.println("FXML trouvé au chemin: " + chemin);
                    break;
                } else {
                    System.out.println("Resource null pour: " + chemin);
                }
            } catch (Exception e) {
                System.out.println("Échec pour: " + chemin + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        if (root == null) {
            System.err.println("Fichier FXML introuvable !");
            System.err.println("Dossiers disponibles dans resources:");
            URL resourceRoot = getClass().getResource("/");
            if (resourceRoot != null) {
                String path = resourceRoot.getPath();
                System.out.println("Resources path: " + path);
                File dir = new File(path);
                if (dir.exists()) {
                    listFiles(dir, "");
                }
            } else {
                System.err.println("Impossible de localiser le dossier des ressources (getClass().getResource(\"/\") est null)");
            }
            return;
        }

        Scene scene = new Scene(root);
        primaryStage.setTitle("Formini - Menu Principal");
        primaryStage.setScene(scene);
        StageWindowMode.maximize(primaryStage);
        primaryStage.show();
    }

    private void initializeSession() {
        // Session initialization - admin is already created by AdminInitializer
        try {
            tn.formini.services.UsersService.UserService us = new tn.formini.services.UsersService.UserService();
            tn.formini.entities.Users.User adminUser = AdminInitializer.getAdminUser();
            
            if (adminUser != null) {
                tn.formini.tools.SessionManager.setCurrentUser(adminUser);
                System.out.println("Session active pour : " + adminUser.getNom() + " " + adminUser.getPrenom());
            } else {
                System.err.println("Admin user not found for session initialization");
            }
        } catch (Exception e) {
            System.err.println("Erreur session : " + e.getMessage());
        }
    }

    private void listFiles(File dir, String indent) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                System.out.println(indent + file.getName());
                if (file.isDirectory()) {
                    listFiles(file, indent + "  ");
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}