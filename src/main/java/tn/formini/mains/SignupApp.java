package tn.formini.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.auth.SignupController;
import tn.formini.utils.StageWindowMode;

import java.net.URL;

/**
 * Lance uniquement l'écran d'inscription (FXML {@code /fxml/auth/Signup.fxml}).
 */
public class SignupApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageWindowMode.installGlobalMaximizedPolicy();

        URL resource = getClass().getResource("/fxml/auth/Signup.fxml");
        if (resource == null) {
            System.err.println("FXML introuvable : /fxml/auth/Signup.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        SignupController controller = loader.getController();
        if (controller != null) {
            controller.setOnBack(primaryStage::close);
        }

        Scene scene = new Scene(root, 980, 760);
        URL css = getClass().getResource("/css/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        primaryStage.setTitle("Formini - Inscription");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(520);
        StageWindowMode.maximize(primaryStage);
        primaryStage.show();
    }
}
