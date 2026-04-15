package tn.formini.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;

public class MainMenuApp extends Application {

    @Override
<<<<<<< Updated upstream
    public void start(Stage primaryStage) throws Exception {
        // Initialiser une session avec un utilisateur réel de la DB
        try {
            tn.formini.services.UserService us = new tn.formini.services.UserService();
            java.util.List<tn.formini.entities.User> users = us.afficher();
            tn.formini.entities.User sessionUser;
            if (users.isEmpty()) {
                sessionUser = new tn.formini.entities.User();
                sessionUser.setNom("Admin");
                sessionUser.setPrenom("Système");
                sessionUser.setEmail("admin@formini.tn");
                sessionUser.setPassword("admin");
                sessionUser.setRole_utilisateur("admin");
                us.ajouter(sessionUser);
            } else {
                sessionUser = users.get(0);
            }
            tn.formini.tools.SessionManager.setCurrentUser(sessionUser);
            System.out.println("Session active pour : " + sessionUser.getNom());
        } catch (Exception e) {
            System.err.println("Erreur session : " + e.getMessage());
        }
=======
    public void start(Stage primaryStage) {
        initializeSession();

        // Essaie plusieurs chemins
>>>>>>> Stashed changes

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
                root = FXMLLoader.load(getClass().getResource(chemin));
                if (root != null) {
                    System.out.println("FXML trouvé au chemin: " + chemin);
                    break;
                }
            } catch (Exception e) {
                System.out.println("Échec pour: " + chemin);
            }
        }

        if (root == null) {
            System.err.println("Fichier FXML introuvable !");
            System.err.println("Dossiers disponibles dans resources:");
            String path = getClass().getResource("/").getPath();
            System.out.println("Resources path: " + path);
            File dir = new File(path);
            if (dir.exists()) {
                listFiles(dir, "");
            }
            return;
        }

        Scene scene = new Scene(root);
        primaryStage.setTitle("Formini - Menu Principal");
        primaryStage.setScene(scene);
        primaryStage.show();
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