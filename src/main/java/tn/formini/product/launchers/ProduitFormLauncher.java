package tn.formini.product.launchers;

import tn.formini.controllers.produit.ProduitFormController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProduitFormLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/ProduitForm.fxml"));
            Parent root = loader.load();
            
            // Set up the stage
            primaryStage.setTitle("Gestion des Produits - Formini");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
            System.out.println("Product form interface started successfully!");
            System.out.println("You can now test all the validation features.");
            
        } catch (Exception e) {
            System.err.println("Error starting product form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
