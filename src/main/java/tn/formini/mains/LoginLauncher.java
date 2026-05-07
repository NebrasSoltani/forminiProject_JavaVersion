package tn.formini.mains;

import javafx.application.Application;

/**
 * Point d'entrée pour lancer la connexion. Ne doit pas étendre {@link Application}
 * (évite des erreurs JVM sur JDK 21+wind avec {@code main} dans la sous-classe JavaFX).
 */
public final class LoginLauncher {

    private LoginLauncher() {}

    public static void main(String[] args) {
        Application.launch(LoginApp.class, args);
    }
}
