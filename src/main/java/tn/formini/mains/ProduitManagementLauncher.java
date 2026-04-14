package tn.formini.mains;

import javafx.application.Application;
import tn.formini.product.launchers.ProduitManagementApp;

/**
 * Simple launcher for Product Management Interface.
 * This class serves as an entry point that's easy to find and run.
 */
public class ProduitManagementLauncher {

    private ProduitManagementLauncher() {}

    public static void main(String[] args) {
        System.out.println("=== Launching Product Management Interface ===");
        System.out.println("Main Class: tn.formini.product.launchers.ProduitManagementApp");
        System.out.println("Features: List, Add, Edit, Delete Products");
        System.out.println("================================================");
        
        // Launch the JavaFX application
        Application.launch(ProduitManagementApp.class, args);
    }
}
