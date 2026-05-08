package tn.formini.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.crud.SocieteCrudController;

public class SocieteCardsTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-crud-cards.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1200, 800);
            
            // Load CSS if available
            java.net.URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            primaryStage.setTitle("Test - Interface Cartes Sociétés");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
