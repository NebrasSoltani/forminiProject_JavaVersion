package tn.formini.product.launchers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.produit.ProduitEditFormController;

/**
 * Simple test launcher for ultra-compact edit form
 */
public class TestUltraCompactForm extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            System.out.println("=== TESTING ULTRA-COMPACT FORM ===");
            System.out.println("Loading: /fxml/product/ProduitEditForm.fxml");
            System.out.println("Controller: tn.formini.controllers.produit.ProduitEditFormController");
            System.out.println("CSS: /css/ultra-compact-forms.css");
            System.out.println("=====================================");
            
            // Load the ultra-compact edit form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProduitEditForm.fxml"));
            Parent root = loader.load();
            
            // Get controller
            ProduitEditFormController controller = loader.getController();
            System.out.println("✅ Form loaded successfully!");
            System.out.println("✅ Controller initialized: " + (controller != null ? "YES" : "NO"));
            
            // Set up the stage with ultra-compact size
            primaryStage.setTitle("Formini - Ultra-Compact Edit Form Test");
            primaryStage.setScene(new Scene(root, 650, 480)); // Ultra-compact size
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(450);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            System.out.println("✅ Stage configured: 650x480 (ultra-compact)");
            System.out.println("✅ Form displayed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR loading ultra-compact form:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
