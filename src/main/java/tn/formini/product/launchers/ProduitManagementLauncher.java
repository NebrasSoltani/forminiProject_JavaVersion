package tn.formini.product.launchers;

import javafx.application.Application;
import tn.formini.product.launchers.ProduitManagementApp;

/**
 * Point d'entrée pour lancer la gestion complète des produits. Ne doit pas étendre {@link Application}
 * (évite des erreurs JVM sur JDK 21+ avec {@code main} dans la sous-classe JavaFX).
 */
public final class ProduitManagementLauncher {

    private ProduitManagementLauncher() {}

    public static void main(String[] args) {
        Application.launch(ProduitManagementApp.class, args);
    }
}
