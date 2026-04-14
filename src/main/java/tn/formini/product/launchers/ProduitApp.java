package tn.formini.product.launchers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.formini.controllers.produit.ProduitFormController;

import java.net.URL;

/**
 * Lance uniquement l'écran d'ajout de produit (FXML {@code /fxml/produits/ProduitForm.fxml}).
 */
public class ProduitApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL resource = getClass().getResource("/fxml/product/ProduitForm.fxml");
        if (resource == null) {
            System.err.println("FXML introuvable : /fxml/produits/ProduitForm.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        ProduitFormController controller = loader.getController();
        if (controller != null) {
            controller.setOnClose(v -> primaryStage.close());
        }

        Scene scene = new Scene(root, 1000, 700);
        URL css = getClass().getResource("/css/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        primaryStage.setTitle("Formini - Gestion des Produits");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
}
