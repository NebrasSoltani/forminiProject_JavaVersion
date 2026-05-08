package tn.formini.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lanceur DIRECT pour l'interface Stripe qui fonctionne à coup sûr
 */
public class StripeDirectLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Lancement de l'interface Stripe DIRECT...");
            System.out.println("🔑 Utilisation de la clé directe - PAS de configuration externe");
            
            // Charger l'interface FXML DIRECTE
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/frontend/StripeDirect.fxml"));
            Parent root = loader.load();
            
            // Configurer la scène
            Scene scene = new Scene(root, 800, 900);
            
            // Configurer la fenêtre
            primaryStage.setTitle("💳 Stripe Direct - Interface Fonctionnelle");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
            // Afficher la fenêtre
            primaryStage.show();
            
            System.out.println("✅ Interface Stripe DIRECT lancée avec succès !");
            System.out.println("🎯 L'interface utilise votre clé directement !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement: " + e.getMessage());
            e.printStackTrace();
            
            // Afficher une alerte d'erreur
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur de lancement");
            alert.setHeaderText("Impossible de lancer l'interface Stripe Direct");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        System.out.println("🔥 Démarrage de l'application Stripe DIRECT...");
        System.out.println("🔑 Configuration directe - PAS de fichiers properties");
        launch(args);
    }
}
