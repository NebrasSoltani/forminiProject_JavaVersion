package tn.formini.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tn.formini.ForminiApplication;

public class TestCloudinaryApp extends Application {

    private static ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        System.out.println("[DEBUG] Initialisation de Spring Boot...");
        // Headless false est crucial pour les applications graphiques
        context = new SpringApplicationBuilder(ForminiApplication.class)
                .headless(false)
                .run();
        System.out.println("[DEBUG] Spring Boot est PRÊT.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("[DEBUG] Chargement de l'interface FXML...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cloudinary_test.fxml"));
            
            // On injecte le contexte Spring (nécessaire pour @Autowired dans le controller)
            loader.setControllerFactory(context::getBean);
            
            Parent root = loader.load();
            stage.setTitle("Test Cloudinary - Formini");
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("[DEBUG] Fenêtre JavaFX affichée.");
        } catch (Exception e) {
            System.err.println("[ERREUR] Échec du chargement FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        // Utilisation de launch(Class, args) pour plus de stabilité
        Application.launch(TestCloudinaryApp.class, args);
    }
}
