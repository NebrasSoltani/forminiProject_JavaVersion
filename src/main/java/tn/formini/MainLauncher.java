package tn.formini;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Loading boutique interface...");
            
            // Load the boutique interface directly as main window
            FXMLLoader loader = new FXMLLoader(
                MainLauncher.class.getResource("/fxml/frontend/Shop.fxml")
            );
            Parent root = loader.load();
            
            // Setup main stage with boutique interface
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("Formini.tn - Boutique");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            
            System.out.println("Boutique interface loaded successfully!");
            
        } catch (Exception e) {
            System.err.println("Error loading boutique interface: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple error display
            showErrorStage();
        }
    }

    private void showErrorStage() {
        try {
            Stage errorStage = new Stage();
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label(
                "Erreur: Impossible de charger l'interface boutique.\n" +
                "Veuillez vérifier que les fichiers FXML existent."
            );
            errorLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20;");
            
            Scene errorScene = new Scene(errorLabel, 400, 200);
            errorStage.setTitle("Erreur de chargement");
            errorStage.setScene(errorScene);
            errorStage.show();
        } catch (Exception e) {
            System.err.println("Error showing error stage: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
