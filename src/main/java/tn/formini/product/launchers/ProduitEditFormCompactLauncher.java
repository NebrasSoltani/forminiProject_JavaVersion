package tn.formini.product.launchers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.produit.ProduitEditFormController;
import tn.formini.entities.produits.Produit;
import tn.formini.services.produitsService.ProduitService;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Launcher for compact product edit form.
 * Uses the improved, smaller interface for better user experience.
 */
public class ProduitEditFormCompactLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the compact edit form FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProduitEditForm_Compact.fxml"));
            Parent root = loader.load();
            
            // Get controller and set up test data
            ProduitEditFormController controller = loader.getController();
            
            // Create a sample product for testing
            Produit sampleProduct = new Produit();
            sampleProduct.setId(1);
            sampleProduct.setNom("Laptop Dell XPS 15");
            sampleProduct.setCategorie("Électronique");
            sampleProduct.setDescription("Ordinateur portable haute performance avec écran 4K et processeur Intel i7.");
            sampleProduct.setPrix(new BigDecimal("2499.990"));
            sampleProduct.setStock(15);
            sampleProduct.setStatut("disponible");
            sampleProduct.setImage("https://example.com/laptop.jpg");
            sampleProduct.setDate_creation(new Date());
            
            controller.setProduit(sampleProduct);
            controller.setOnProductUpdated(() -> {
                System.out.println("Product updated callback triggered!");
            });
            
            // Set up the stage with smaller size
            primaryStage.setTitle("Formini - Modifier Produit (Compact)");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setMinWidth(700);
            primaryStage.setMinHeight(500);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            System.out.println("=== COMPACT PRODUCT EDIT FORM ===");
            System.out.println("Features:");
            System.out.println("  - Organized sections for better usability");
            System.out.println("  - Reduced form size (800x600)");
            System.out.println("  - Improved visual design");
            System.out.println("  - Responsive layout");
            System.out.println("  - Enhanced user experience");
            System.out.println("=====================================");
            
        } catch (Exception e) {
            System.err.println("Error starting compact product edit form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
