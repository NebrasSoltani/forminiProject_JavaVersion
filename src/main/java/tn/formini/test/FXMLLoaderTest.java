package tn.formini.test;

import javafx.fxml.FXMLLoader;
import java.net.URL;

public class FXMLLoaderTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing FXML loading...");
            
            // Test resource loading
            URL resource = FXMLLoaderTest.class.getResource("/fxml/product/ProduitEditForm_Compact.fxml");
            if (resource == null) {
                System.err.println("FXML resource NOT found!");
                return;
            }
            
            System.out.println("FXML resource found: " + resource.toExternalForm());
            
            // Test FXML loading
            FXMLLoader loader = new FXMLLoader(resource);
            Object root = loader.load();
            
            System.out.println("FXML loaded successfully!");
            System.out.println("Root object: " + (root != null ? root.getClass().getSimpleName() : "null"));
            
            Object controller = loader.getController();
            System.out.println("Controller: " + (controller != null ? controller.getClass().getSimpleName() : "null"));
            
        } catch (Exception e) {
            System.err.println("ERROR loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
