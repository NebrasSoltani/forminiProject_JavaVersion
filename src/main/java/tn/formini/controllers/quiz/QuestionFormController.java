package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.services.QuestionService;
import tn.formini.services.QuizService;

import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class QuestionFormController implements Initializable {

    @FXML private VBox quizBox;
    @FXML private Label titreLabel;
    @FXML private ComboBox<Quiz> quizCombo;
    @FXML private TextArea enonceField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField pointsField;
    @FXML private TextField ordreField;
    @FXML private TextField explicationField;
    @FXML private TextArea explicationDetailField;
    @FXML private Label errorLabel;

    private final QuestionService service = new QuestionService();
    private final QuizService quizService = new QuizService();
    private Question questionExistante = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeCombo.getItems().addAll("qcm", "vrai_faux", "texte_libre", "correspondance");
        quizCombo.getItems().addAll(quizService.getAll());
        quizCombo.setConverter(new javafx.util.StringConverter<Quiz>() {
            public String toString(Quiz q) { return q == null ? "" : q.getTitre(); }
            public Quiz fromString(String s) { return null; }
        });
    }

    public void setQuestion(Question q) {
        this.questionExistante = q;
        titreLabel.setText("Modifier Question");
        enonceField.setText(q.getEnonce());
        typeCombo.setValue(q.getType());
        pointsField.setText(String.valueOf(q.getPoints()));
        ordreField.setText(String.valueOf(q.getOrdre()));
        explicationField.setText(q.getExplication());
        explicationDetailField.setText(q.getExplications_detaillees());
        if (q.getQuiz() != null) {
            quizCombo.getItems().stream()
                    .filter(quiz -> quiz.getId() == q.getQuiz().getId())
                    .findFirst().ifPresent(quizCombo::setValue);
        }
        
        // Hide the quiz box completely when modifying
        if (quizBox != null) {
            quizBox.setVisible(false);
            quizBox.setManaged(false);
        }
    }

    @FXML
    public void sauvegarder() {
        errorLabel.setText("");
        try {
            if (quizCombo.getValue() == null) throw new IllegalArgumentException("Veuillez sélectionner un quiz.");
            if (typeCombo.getValue() == null) throw new IllegalArgumentException("Veuillez sélectionner un type.");

            Question q = questionExistante != null ? questionExistante : new Question();
            q.setEnonce(enonceField.getText().trim());
            q.setType(typeCombo.getValue());
            q.setPoints(Integer.parseInt(pointsField.getText().trim()));
            q.setOrdre(ordreField.getText().isBlank() ? 0 : Integer.parseInt(ordreField.getText().trim()));
            q.setExplication(explicationField.getText().trim());
            q.setExplications_detaillees(explicationDetailField.getText().trim());
            q.setQuiz(quizCombo.getValue());
            q.valider();

            if (questionExistante == null) service.ajouter(q);
            else service.modifier(q);
            fermer();
        } catch (NumberFormatException e) {
            errorLabel.setText("⚠ Points et Ordre doivent être des nombres.");
        } catch (IllegalArgumentException e) {
            errorLabel.setText("⚠ " + e.getMessage());
        }
    }

    @FXML public void annuler() { fermer(); }

    private void fermer() { ((Stage) enonceField.getScene().getWindow()).close(); }
}
