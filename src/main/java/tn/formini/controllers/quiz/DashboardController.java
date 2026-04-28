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

    public static DashboardController instance;

    @FXML
    public void initialize() {
        instance = this;
    }

    private void chargerVue(String fxmlPath) {
        try {
            Node vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur chargement vue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetBoutons() {
        String inactif = "-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 13px; -fx-padding: 10 24 10 48; -fx-alignment: center-left; -fx-cursor: hand;";
        btnQuiz.setStyle(inactif);
        btnQuestion.setStyle(inactif);
        btnReponse.setStyle(inactif);
        btnResultat.setStyle(inactif);
        btnApprenantQuiz.setStyle(inactif);
    }

    private void activerBouton(Button btn) {
        btn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10 24 10 44; -fx-alignment: center-left; -fx-border-color: #6974e8; -fx-border-width: 0 0 0 4; -fx-cursor: hand;");
    }

    @FXML
    public void ouvrirQuiz() {
        resetBoutons();
        activerBouton(btnQuiz);
        chargerVue("/fxml/quiz/Quiz.fxml");
    }

    @FXML
    public void ouvrirQuestion() {
        resetBoutons();
        activerBouton(btnQuestion);
        chargerVue("/fxml/quiz/Question.fxml");
    }

    public void ouvrirQuestionPourQuiz(tn.formini.entities.Quiz quizToFilter) {
        resetBoutons();
        activerBouton(btnQuestion);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/Question.fxml"));
            Node vue = loader.load();
            QuestionController qc = loader.getController();
            qc.filtrerParQuiz(quizToFilter);
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur ouverture questions pour quiz : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirReponse() {
        resetBoutons();
        activerBouton(btnReponse);
        chargerVue("/fxml/quiz/Reponse.fxml");
    }

    public void ouvrirReponsePourQuestion(tn.formini.entities.Question questionToFilter) {
        resetBoutons();
        activerBouton(btnReponse);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/Reponse.fxml"));
            Node vue = loader.load();
            ReponseController rc = loader.getController();
            rc.filtrerParQuestion(questionToFilter);
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur ouverture reponses pour question : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirResultat() {
        resetBoutons();
        activerBouton(btnResultat);
        chargerVue("/fxml/quiz/Resultat.fxml");
    }

    @FXML
    public void ouvrirApprenantQuiz() {
        resetBoutons();
        activerBouton(btnApprenantQuiz);
        chargerVue("/fxml/quiz/ApprenantQuizList.fxml");
    }
}
