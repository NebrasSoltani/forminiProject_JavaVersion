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

public class ProduitEditFormLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProduitEditForm.fxml"));
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
            
            // Set up the stage
            primaryStage.setTitle("Test - Formulaire de Modification de Produit");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
            System.out.println("Product edit form test interface started successfully!");
            System.out.println("You can now test all the modification features:");
            System.out.println("  - Edit product information");
            System.out.println("  - Update stock and price");
            System.out.println("  - Change status");
            System.out.println("  - Modify description");
            System.out.println("  - Duplicate product");
            System.out.println("  - Delete product");
            
        } catch (Exception e) {
            System.err.println("Error starting product edit form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
