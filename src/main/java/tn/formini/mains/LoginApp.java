package tn.formini.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.auth.LoginController;
import tn.formini.utils.AdminInitializer;
import tn.formini.utils.StageWindowMode;

import java.net.URL;

/**
 * Lance la page d'accueil (FXML {@code /fxml/frontend/Home.fxml}).
 */
public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageWindowMode.installGlobalMaximizedPolicy();

        // Initialize admin user if it doesn't exist
        AdminInitializer.initializeAdmin();
        
        URL resource = getClass().getResource("/fxml/frontend/Home.fxml");
        if (resource == null) {
            System.err.println("FXML introuvable : /fxml/frontend/Home.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();

        Scene scene = new Scene(root, 980, 760);
        URL css = getClass().getResource("/css/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        primaryStage.setTitle("Formini - Accueil");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(520);
        StageWindowMode.maximize(primaryStage);
        primaryStage.show();
    }
}
