package tn.formini.mains;

import tn.formini.tools.MyDataBase;

/**
 * Point d'entrée : initialise la base puis ouvre l'écran d'inscription.
 */
public class Main {
    public static void main(String[] args) {
        MyDataBase.getInstance();
        SignupLauncher.main(args);
    }
}