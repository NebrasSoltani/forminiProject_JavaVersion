package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button btnStatistiques;
    @FXML private Button btnQuiz;
    @FXML private Button btnQuestion;
    @FXML private Button btnReponse;
    @FXML private Button btnResultat;
    @FXML private Button btnApprenantQuiz;

    public static DashboardController instance;

    @FXML
    public void initialize() {
        instance = this;
        ouvrirQuiz();
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
        String inactif = "-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: 900; -fx-background-radius: 20; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: null;";
        btnStatistiques.setStyle(inactif);
        btnQuiz.setStyle(inactif);
        btnQuestion.setStyle(inactif);
        btnReponse.setStyle(inactif);
        btnResultat.setStyle(inactif);
        
        btnApprenantQuiz.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 20; -fx-text-fill: #0f172a; -fx-font-weight: 900; -fx-background-radius: 20; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px;");
    }

    private void activerBouton(Button btn) {
        if (btn == btnApprenantQuiz) {
            btn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 20; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.3), 10, 0, 0, 3);");
        } else {
            btn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 20; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.3), 10, 0, 0, 3);");
        }
    }

    @FXML
    public void ouvrirStatistiques() {
        resetBoutons();
        activerBouton(btnStatistiques);
        chargerVue("/fxml/quiz/Statistiques.fxml");
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
