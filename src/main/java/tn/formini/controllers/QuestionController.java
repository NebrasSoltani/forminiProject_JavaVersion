package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.entities.Reponse;
import tn.formini.services.QuestionService;
import tn.formini.services.QuizService;
import tn.formini.services.ReponseService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuestionController implements Initializable {

    @FXML private VBox questionsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<Quiz> filtreQuiz;

    private final QuestionService service = new QuestionService();
    private final QuizService quizService = new QuizService();
    private final ReponseService reponseService = new ReponseService();
    
    private List<Question> allQuestions;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerQuizCombo();
        chargerDonnees();
    }

    private void chargerQuizCombo() {
        filtreQuiz.getItems().add(null); // option "Tous"
        filtreQuiz.getItems().addAll(quizService.getAll());
        filtreQuiz.setConverter(new javafx.util.StringConverter<Quiz>() {
            public String toString(Quiz q) { return q == null ? "Tous les quiz" : q.getTitre(); }
            public Quiz fromString(String s) { return null; }
        });
    }

    private void chargerDonnees() {
        allQuestions = service.getAll();
        afficherQuestions(allQuestions);
    }

    @FXML
    public void filtrerQuestions() {
        String texte = searchField.getText().toLowerCase();
        Quiz quizFiltre = filtreQuiz.getValue();
        List<Question> filtres = allQuestions.stream()
                .filter(q -> q.getEnonce().toLowerCase().contains(texte))
                .filter(q -> quizFiltre == null || (q.getQuiz() != null && q.getQuiz().getId() == quizFiltre.getId()))
                .collect(Collectors.toList());
        afficherQuestions(filtres);
    }

    public void filtrerParQuiz(Quiz quiz) {
        if (quiz != null) {
            for (Quiz q : filtreQuiz.getItems()) {
                if (q != null && q.getId() == quiz.getId()) {
                    filtreQuiz.setValue(q);
                    filtrerQuestions();
                    break;
                }
            }
        }
    }

    private void afficherQuestions(List<Question> questionsAafficher) {
        questionsContainer.getChildren().clear();
        
        if (questionsAafficher == null || questionsAafficher.isEmpty()) {
            Label noData = new Label("Aucune question trouvée.");
            noData.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 20;");
            questionsContainer.getChildren().add(noData);
            return;
        }

        List<Reponse> toutesReponses = reponseService.getAll();

        int index = 1;
        for (Question q : questionsAafficher) {
            VBox card = createQuestionCard(q, index, toutesReponses);
            questionsContainer.getChildren().add(card);
            index++;
            
            // Add a separator unless it's the last item
            if (index <= questionsAafficher.size()) {
                Region separator = new Region();
                separator.setStyle("-fx-background-color: #334155; -fx-min-height: 1; -fx-max-height: 1;");
                questionsContainer.getChildren().add(separator);
            }
        }
    }

    private VBox createQuestionCard(Question q, int index, List<Reponse> toutesReponses) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #1e293b; -fx-padding: 18 20; -fx-border-color: #0891b2; -fx-border-width: 0 0 0 4; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Top Row: Number + Enonce + Points + Buttons
        HBox topRow = new HBox(15);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblTitle = new Label(index + ". " + q.getEnonce());
        lblTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 900;");
        lblTitle.setWrapText(true);
        HBox.setHgrow(lblTitle, Priority.ALWAYS);
        lblTitle.setMaxWidth(Double.MAX_VALUE);

        Label lblType = new Label(q.getType());
        lblType.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");

        Label lblPoints = new Label(q.getPoints() + " point(s)");
        lblPoints.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");

        Button btnReps = new Button("👁");
        Button btnEdit = new Button("✏");
        Button btnDel = new Button("🗑");
        
        btnReps.setStyle("-fx-background-color: #6974e8; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10;");
        btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10;");
        btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10;");

        btnReps.setOnAction(e -> {
            if (tn.formini.controllers.DashboardController.instance != null) {
                tn.formini.controllers.DashboardController.instance.ouvrirReponsePourQuestion(q);
            }
        });
        btnEdit.setOnAction(e -> ouvrirFormulaire(q));
        btnDel.setOnAction(e -> supprimer(q));

        HBox actions = new HBox(8, lblType, lblPoints, btnReps, btnEdit, btnDel);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        topRow.getChildren().addAll(lblTitle, actions);

        // Fetch responses for this question
        List<Reponse> responsesPourQuestion = toutesReponses.stream()
                .filter(r -> r.getQuestion() != null && r.getQuestion().getId() == q.getId())
                .collect(Collectors.toList());

        Label repsLabel = new Label("Réponses (" + responsesPourQuestion.size() + ") :");
        repsLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 13px;");

        FlowPane repsFlow = new FlowPane();
        repsFlow.setHgap(10);
        repsFlow.setVgap(10);

        if (responsesPourQuestion.isEmpty()) {
            Label noRep = new Label("Aucune réponse n'est définie.");
            noRep.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-style: italic;");
            repsFlow.getChildren().add(noRep);
        } else {
            for (Reponse r : responsesPourQuestion) {
                Label repBadge = new Label();
                if (r.isEst_correcte()) {
                    repBadge.setText("✓ " + r.getTexte());
                    repBadge.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4; -fx-font-size: 12px;");
                } else {
                    repBadge.setText("✗ " + r.getTexte());
                    repBadge.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4; -fx-font-size: 12px;");
                }
                repsFlow.getChildren().add(repBadge);
            }
        }

        card.getChildren().addAll(topRow, repsLabel, repsFlow);
        return card;
    }

    @FXML
    public void ouvrirFormAjout() {
        ouvrirFormulaire(null);
    }

    @FXML
    public void ouvrirFormIA() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/GenererQuestionIA.fxml"));
            Parent root = loader.load();
            GenererQuestionIAController ctrl = loader.getController();
            ctrl.initData(filtreQuiz.getValue(), this::chargerDonnees);

            Stage stage = new Stage();
            stage.setTitle("Génération IA - Questions");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            System.out.println("Erreur formulaire génération IA : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirFormulaire(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuestionForm.fxml"));
            Parent root = loader.load();
            QuestionFormController ctrl = loader.getController();
            if (question != null) {
                ctrl.setQuestion(question);
            }

            Stage stage = new Stage();
            stage.setTitle(question == null ? "Nouvelle Question" : "Modifier Question");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            chargerDonnees();
        } catch (IOException e) {
            System.out.println("Erreur formulaire question : " + e.getMessage());
        }
    }

    private void supprimer(Question q) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette question ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { service.supprimer(q.getId()); chargerDonnees(); }
        });
    }
}
