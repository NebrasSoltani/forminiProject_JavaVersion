package tn.formini.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lanceur pour l'interface Stripe fonctionnelle
 */
public class StripeLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Lancement de l'interface Stripe...");
            
            // Charger l'interface FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/frontend/WorkingStripe.fxml"));
            Parent root = loader.load();
            
            // Configurer la scène
            Scene scene = new Scene(root, 800, 900);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            // Configurer la fenêtre
            primaryStage.setTitle("💳 Paiement Stripe - Interface Fonctionnelle");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
            // Afficher la fenêtre
            primaryStage.show();
            
            System.out.println("✅ Interface Stripe lancée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher une alerte d'erreur
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur de lancement");
            alert.setHeaderText("Impossible de lancer l'interface Stripe");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        System.out.println("🔥 Démarrage de l'application Stripe...");
        launch(args);
    }
}
