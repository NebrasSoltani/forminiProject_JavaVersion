package tn.formini.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.services.QuestionService;
import tn.formini.services.QuizService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuestionController implements Initializable {

    @FXML private TableView<Question> tableQuestion;
    @FXML private TableColumn<Question, Integer> colId;
    @FXML private TableColumn<Question, String> colEnonce;
    @FXML private TableColumn<Question, String> colType;
    @FXML private TableColumn<Question, Integer> colPoints;
    @FXML private TableColumn<Question, Integer> colOrdre;
    @FXML private TableColumn<Question, String> colQuiz;
    @FXML private TableColumn<Question, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<Quiz> filtreQuiz;

    private final QuestionService service = new QuestionService();
    private final QuizService quizService = new QuizService();
    private ObservableList<Question> questionList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEnonce.setCellValueFactory(new PropertyValueFactory<>("enonce"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        ajouterColonneActions();
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
        questionList.setAll(service.getAll());
        tableQuestion.setItems(questionList);
    }

    @FXML
    public void filtrerQuestions() {
        String texte = searchField.getText().toLowerCase();
        Quiz quizFiltre = filtreQuiz.getValue();
        List<Question> filtres = service.getAll().stream()
                .filter(q -> q.getEnonce().toLowerCase().contains(texte))
                .filter(q -> quizFiltre == null || (q.getQuiz() != null && q.getQuiz().getId() == quizFiltre.getId()))
                .collect(Collectors.toList());
        tableQuestion.setItems(FXCollections.observableArrayList(filtres));
    }

    public void filtrerParQuiz(tn.formini.entities.Quiz quiz) {
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

    @FXML
    public void ouvrirFormAjout() {
        ouvrirFormulaire(null);
    }

    private void ouvrirFormulaire(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuestionForm.fxml"));
            Parent root = loader.load();
            QuestionFormController ctrl = loader.getController();
            if (question != null) ctrl.setQuestion(question);

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

    private void ajouterColonneActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btnReps = new Button("👁");
            final Button btnEdit = new Button("✏");
            final Button btnDel = new Button("🗑");
            final HBox box = new HBox(8, btnReps, btnEdit, btnDel);

            {
                btnReps.setStyle("-fx-background-color: #6974e8; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnEdit.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnDel.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                
                btnReps.setOnAction(e -> {
                    Question q = getTableView().getItems().get(getIndex());
                    if (tn.formini.controllers.DashboardController.instance != null) {
                        tn.formini.controllers.DashboardController.instance.ouvrirReponsePourQuestion(q);
                    }
                });
                btnEdit.setOnAction(e -> ouvrirFormulaire(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void supprimer(Question q) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette question ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { service.supprimer(q.getId()); chargerDonnees(); }
        });
    }
}
