package tn.formini.mains;

import javafx.application.Application;

/**
 * Point d'entrée pour lancer la gestion des produits. Ne doit pas étendre {@link Application}
 * (évite des erreurs JVM sur JDK 21+ avec {@code main} dans la sous-classe JavaFX).
 */
public final class ProduitLauncher {

    private ProduitLauncher() {}

    public static void main(String[] args) {
        Application.launch(ProduitApp.class, args);
    }
}
