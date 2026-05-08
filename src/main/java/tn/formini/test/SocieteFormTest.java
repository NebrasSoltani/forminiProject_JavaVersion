package tn.formini.test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class SocieteFormTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing FXML loading...");
            
            // Test loading the FXML file
            FXMLLoader loader = new FXMLLoader(
                SocieteFormTest.class.getResource("/fxml/crud/societe-form.fxml")
            );
            
            Parent root = loader.load();
            System.out.println("✅ FXML loaded successfully!");
            System.out.println("Controller: " + loader.getController());
            
        } catch (IOException e) {
            System.err.println("❌ Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
