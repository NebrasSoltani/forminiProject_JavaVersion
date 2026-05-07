package tn.formini.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import tn.formini.controllers.QuestionController;
import tn.formini.entities.Quizs.Question;
import tn.formini.entities.Quizs.Quiz;
import tn.formini.entities.Quizs.Reponse;
import tn.formini.entities.Users.Apprenant;

import java.io.IOException;
import org.springframework.stereotype.Component;

@Component("quizDashboardController")
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
        configurerInterfaceSelonRole();
        
        // Par défaut, on ouvre les statistiques ou les quiz selon le rôle
        tn.formini.services.UsersService.SessionManager session = tn.formini.services.UsersService.SessionManager.getInstance();
        if (session.isApprenant()) {
            ouvrirApprenantQuiz();
        } else {
            ouvrirQuiz();
        }
    }

    private void configurerInterfaceSelonRole() {
        tn.formini.services.UsersService.SessionManager session = tn.formini.services.UsersService.SessionManager.getInstance();
        boolean isApprenant = session.isApprenant();
        boolean isFormateur = session.isFormateur();
        boolean isAdmin = session.isAdmin();

        // Si c'est un apprenant, on cache tout sauf le bouton "Apprenant Quiz"
        if (isApprenant) {
            cacherBouton(btnStatistiques);
            cacherBouton(btnQuiz);
            cacherBouton(btnQuestion);
            cacherBouton(btnReponse);
            cacherBouton(btnResultat);
            
            if (btnApprenantQuiz != null) {
                btnApprenantQuiz.setVisible(true);
                btnApprenantQuiz.setManaged(true);
            }
        } 
        // Si c'est un formateur ou admin, on cache le bouton "Mode Apprenant"
        else if (isFormateur || isAdmin) {
            cacherBouton(btnApprenantQuiz);
            
            if (btnStatistiques != null) btnStatistiques.setVisible(true);
            if (btnQuiz != null) btnQuiz.setVisible(true);
            if (btnQuestion != null) btnQuestion.setVisible(true);
            if (btnReponse != null) btnReponse.setVisible(true);
            if (btnResultat != null) btnResultat.setVisible(true);
        }
    }

    private void cacherBouton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
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
        if (btnStatistiques != null) btnStatistiques.setStyle(inactif);
        if (btnQuiz != null) btnQuiz.setStyle(inactif);
        if (btnQuestion != null) btnQuestion.setStyle(inactif);
        if (btnReponse != null) btnReponse.setStyle(inactif);
        if (btnResultat != null) btnResultat.setStyle(inactif);

        if (btnApprenantQuiz != null) {
            btnApprenantQuiz.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 20; -fx-text-fill: #0f172a; -fx-font-weight: 900; -fx-background-radius: 20; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px;");
        }
    }

    private void activerBouton(Button btn) {
        if (btn == null) return;
        if (btn == btnApprenantQuiz) {
            btn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 20; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.3), 10, 0, 0, 3);");
        } else {
            btn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 20; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.3), 10, 0, 0, 3);");
        }
    }

    @FXML
    public void ouvrirStatistiques() {
        resetBoutons();
        if (btnStatistiques != null) activerBouton(btnStatistiques);
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

    public void ouvrirQuestionPourQuiz(Quiz quizToFilter) {
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

    public void ouvrirReponsePourQuestion(Question questionToFilter) {
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

    public void ouvrirApprenantQuizPasser(Apprenant apprenant, int formationId, int quizId) {
        resetBoutons();
        activerBouton(btnApprenantQuiz);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ApprenantQuizPasser.fxml"));
            Node vue = loader.load();
            ApprenantQuizPasserController ctrl = loader.getController();
            ctrl.initData(apprenant, formationId, quizId);
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur ouverture Passer Quiz : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ouvrirGenerateurIA(Quiz quizToFilter, Runnable onSuccess) {
        resetBoutons();
        activerBouton(btnQuestion); // On reste logiquement sous "Questions"
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/GenererQuestionIA.fxml"));
            Node vue = loader.load();
            tn.formini.controllers.GenererQuestionIAController ctrl = loader.getController();
            ctrl.initData(quizToFilter, () -> {
                if (onSuccess != null) onSuccess.run();
                // Return to Question view
                if (quizToFilter != null) {
                    ouvrirQuestionPourQuiz(quizToFilter);
                } else {
                    ouvrirQuestion();
                }
            });
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur ouverture Générateur IA : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ouvrirFormulaireQuiz(Quiz quizToEdit, Runnable onSuccess) {
        resetBoutons();
        activerBouton(btnQuiz);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizForm.fxml"));
            Node vue = loader.load();
            QuizFormController ctrl = loader.getController();
            
            // On lui passe un callback pour revenir à la liste des quiz
            ctrl.initData(quizToEdit, () -> {
                if (onSuccess != null) onSuccess.run();
                ouvrirQuiz();
            });
            
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur ouverture formulaire Quiz : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ouvrirFormulaireQuestion(Question questionToEdit, Runnable onSuccess) {
        resetBoutons();
        activerBouton(btnQuestion);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuestionForm.fxml"));
            Node vue = loader.load();
            QuestionFormController ctrl = loader.getController();
            
            // On lui passe un callback pour revenir à la liste des questions
            ctrl.initData(questionToEdit, () -> {
                if (onSuccess != null) onSuccess.run();
                ouvrirQuestion();
            });
            
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur ouverture formulaire Question : " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void ouvrirFormulaireReponse(Reponse reponseToEdit, Runnable onSuccess) {
        resetBoutons();
        activerBouton(btnReponse);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ReponseForm.fxml"));
            Node vue = loader.load();
            ReponseFormController ctrl = loader.getController();
            
            // On lui passe un callback pour revenir à la liste des réponses
            ctrl.initData(reponseToEdit, () -> {
                if (onSuccess != null) onSuccess.run();
                ouvrirReponse();
            });
            
            contentArea.getChildren().setAll(vue);
        } catch (IOException e) {
            System.out.println("Erreur ouverture formulaire Réponse : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
