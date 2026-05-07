package tn.formini.mains;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tn.formini.ForminiApplication;
import tn.formini.utils.AdminInitializer;
import tn.formini.utils.StageWindowMode;
import java.io.File;

import java.net.URL;

public class MainMenuApp extends Application {

    private static ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Démarre Spring Boot en même temps que JavaFX
        context = new SpringApplicationBuilder(ForminiApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageWindowMode.installGlobalMaximizedPolicy();

        AdminInitializer.initializeAdmin();
        initializeSession();

        // Essaie plusieurs chemins
        String[] chemins = {
                "/fxml/MainMenu.fxml",
                "/tn/formini/fxml/MainMenu.fxml",
                "tn/formini/fxml/MainMenu.fxml"
        };

        Parent root = null;
        for (String chemin : chemins) {
            try {
                System.out.println("Tentative: " + chemin);
                URL resource = getClass().getResource(chemin);
                if (resource != null) {
                    FXMLLoader loader = new FXMLLoader(resource);
                    // On demande à FXMLLoader d'utiliser Spring pour créer les instances des controllers
                    if (context != null) {
                        loader.setControllerFactory(context::getBean);
                    }
                    root = loader.load();
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
            return;
        }

        Scene scene = new Scene(root);
        primaryStage.setTitle("Formini - Menu Principal");
        primaryStage.setScene(scene);
        StageWindowMode.maximize(primaryStage);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
    }

    private void initializeSession() {
        try {
            tn.formini.entities.Users.User adminUser = AdminInitializer.getAdminUser();
            if (adminUser != null) {
                tn.formini.tools.SessionManager.setCurrentUser(adminUser);
            }
        } catch (Exception e) {
            System.err.println("Erreur session : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
