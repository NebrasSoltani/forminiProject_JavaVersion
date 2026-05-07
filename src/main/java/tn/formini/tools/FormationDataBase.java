package tn.formini.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connexion dédiée à la base de données "formation_db"
 * (utilisée par les services Quiz, Question, Réponse etc.)
 */
public class FormationDataBase {
    private static final String URL  = "jdbc:mysql://localhost:3306/formation_db";
    private static final String USER = "root";
    private static final String PWD  = "";

    private Connection cnx;
    private static FormationDataBase instance;

    private FormationDataBase() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("[FormationDataBase] Connexion à formation_db établie.");
        } catch (ClassNotFoundException e) {
            System.err.println("[FormationDataBase] Driver non trouvé : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[FormationDataBase] Erreur connexion : " + e.getMessage());
        }
    }

    public static FormationDataBase getInstance() {
        if (instance == null) {
            instance = new FormationDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("[FormationDataBase] Reconnexion...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("[FormationDataBase] Vérification connexion échouée : " + e.getMessage());
        }
        return cnx;
    }
}
