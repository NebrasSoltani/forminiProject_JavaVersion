package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button btnQuiz;
    @FXML private Button btnQuestion;
    @FXML private Button btnReponse;
    @FXML private Button btnResultat;
    @FXML private Button btnApprenantQuiz;

    private void chargerVue(String fxmlPath) {
        try {
            Node vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur chargement vue : " + e.getMessage());
        }
    }

    private void resetBoutons() {
        String inactif = "-fx-background-color: transparent; -fx-text-fill: #a8a8b3; -fx-font-size: 14px; -fx-padding: 14 20; -fx-background-radius: 0; -fx-cursor: hand;";
        String actif   = "-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-background-radius: 0; -fx-cursor: hand;";
        btnQuiz.setStyle(inactif);
        btnQuestion.setStyle(inactif);
        btnReponse.setStyle(inactif);
        btnResultat.setStyle(inactif);
        btnApprenantQuiz.setStyle(inactif);
    }

    @FXML
    public void ouvrirQuiz() {
        resetBoutons();
        btnQuiz.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-background-radius: 0; -fx-cursor: hand;");
        chargerVue("/tn/formini/views/Quiz.fxml");
    }

    @FXML
    public void ouvrirQuestion() {
        resetBoutons();
        btnQuestion.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-background-radius: 0; -fx-cursor: hand;");
        chargerVue("/tn/formini/views/Question.fxml");
    }

    @FXML
    public void ouvrirReponse() {
        resetBoutons();
        btnReponse.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-background-radius: 0; -fx-cursor: hand;");
        chargerVue("/tn/formini/views/Reponse.fxml");
    }

    @FXML
    public void ouvrirResultat() {
        resetBoutons();
        btnResultat.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-background-radius: 0; -fx-cursor: hand;");
        chargerVue("/tn/formini/views/Resultat.fxml");
    }

    @FXML
    public void ouvrirApprenantQuiz() {
        resetBoutons();
        btnApprenantQuiz.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 14 20; -fx-background-radius: 0; -fx-cursor: hand;");
        chargerVue("/tn/formini/views/ApprenantQuizList.fxml");
    }
}
