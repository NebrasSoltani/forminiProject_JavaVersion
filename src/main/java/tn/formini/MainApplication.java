package tn.formini;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Loading boutique interface...");
            
            FXMLLoader loader = new FXMLLoader(
                MainApplication.class.getResource("/fxml/frontend/Shop.fxml")
            );
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("Formini.tn - Boutique");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            
            System.out.println("Boutique interface loaded successfully!");
            
        } catch (Exception e) {
            System.err.println("Error loading boutique interface: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
