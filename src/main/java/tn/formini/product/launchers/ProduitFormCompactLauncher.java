package tn.formini.product.launchers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.produit.ProduitFormController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Launcher for compact product add form.
 * Uses the improved, smaller interface for better user experience.
 */
public class ProduitFormCompactLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the compact add form FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProduitForm_Compact.fxml"));
            Parent root = loader.load();
            
            // Get controller
            ProduitFormController controller = loader.getController();
            
            // Set up the stage with smaller size
            primaryStage.setTitle("Formini - Ajouter Produit (Compact)");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setMinWidth(700);
            primaryStage.setMinHeight(500);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            System.out.println("=== COMPACT PRODUCT ADD FORM ===");
            System.out.println("Features:");
            System.out.println("  - Organized sections for better usability");
            System.out.println("  - Reduced form size (800x600)");
            System.out.println("  - Improved visual design");
            System.out.println("  - Responsive layout");
            System.out.println("  - Enhanced user experience");
            System.out.println("=====================================");
            
        } catch (Exception e) {
            System.err.println("Error starting compact product add form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
