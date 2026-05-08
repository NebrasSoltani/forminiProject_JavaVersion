package tn.formini.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CardViewTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Loading FXML: /fxml/crud/societe-crud-cards.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-crud-cards.fxml"));
            
            if (loader.getLocation() == null) {
                System.err.println("FXML file not found!");
                return;
            }
            
            System.out.println("FXML found at: " + loader.getLocation());
            Parent root = loader.load();
            
            // Check if controller was loaded
            Object controller = loader.getController();
            System.out.println("Controller loaded: " + (controller != null ? controller.getClass().getName() : "NULL"));
            
            Scene scene = new Scene(root, 1200, 800);
            
            primaryStage.setTitle("Test Vue Cartes Sociétés");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
