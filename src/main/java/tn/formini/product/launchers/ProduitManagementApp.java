package tn.formini.product.launchers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.produit.ProduitListController;

import java.net.URL;

/**
 * Lance l'interface complète de gestion des produits (liste, modification, suppression, ajout).
 */
public class ProduitManagementApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL resource = getClass().getResource("/fxml/product/ProduitList.fxml");
        if (resource == null) {
            System.err.println("FXML introuvable : /fxml/produits/ProduitList.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        ProduitListController controller = loader.getController();
        if (controller != null) {
            controller.setOnClose(v -> primaryStage.close());
        }

        Scene scene = new Scene(root, 1200, 800);
        URL css = getClass().getResource("/css/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        primaryStage.setTitle("Formini - Gestion Complète des Produits");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
        
        System.out.println("=== GESTION DES PRODUITS DÉMARRÉE ===");
        System.out.println("Fonctionnalités disponibles:");
        System.out.println("  - Ajouter de nouveaux produits");
        System.out.println("  - Modifier les produits existants");
        System.out.println("  - Supprimer des produits");
        System.out.println("  - Rechercher des produits");
        System.out.println("  - Filtrer par catégorie");
        System.out.println("=====================================");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
