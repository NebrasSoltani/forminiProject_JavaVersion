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
        AdminInitializer.initializeAdmin();
        initializeSession();

        URL resource = getClass().getResource("/fxml/MainMenu.fxml");
        
        // On demande à FXMLLoader d'utiliser Spring pour créer les instances des controllers
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(context::getBean);
        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Formini - Menu Principal");
        primaryStage.setScene(scene);
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
