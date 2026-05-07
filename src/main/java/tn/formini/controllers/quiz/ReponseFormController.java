package tn.formini.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.formini.controllers.quiz.DashboardController;
import tn.formini.entities.Quizs.Question;
import tn.formini.entities.Quizs.Reponse;
import tn.formini.services.quizService.QuestionService;
import tn.formini.services.quizService.ReponseService;

import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class ReponseFormController implements Initializable {

    @FXML private VBox questionBox;
    @FXML private Label titreLabel;
    @FXML private ComboBox<Question> questionCombo;
    @FXML private TextArea texteField;
    @FXML private TextField explicationField;
    @FXML private CheckBox correcteCheck;
    @FXML private Label errorLabel;

    private final ReponseService service = new ReponseService();
    private final QuestionService questionService = new QuestionService();
    private Reponse reponseExistante = null;
    private Runnable onSuccess = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        questionCombo.getItems().addAll(questionService.getAll());
        questionCombo.setConverter(new javafx.util.StringConverter<Question>() {
            public String toString(Question q) { return q == null ? "" : q.getEnonce(); }
            public Question fromString(String s) { return null; }
        });
    }

    public void setReponse(Reponse r) {
        this.reponseExistante = r;
        titreLabel.setText("Modifier Réponse");
        texteField.setText(r.getTexte());
        explicationField.setText(r.getExplication_reponse());
        correcteCheck.setSelected(r.isEst_correcte());
        if (r.getQuestion() != null) {
            questionCombo.getItems().stream()
                    .filter(q -> q.getId() == r.getQuestion().getId())
                    .findFirst().ifPresent(questionCombo::setValue);
        }
        
        // Hide the question box completely when modifying
        if (questionBox != null) {
            questionBox.setVisible(false);
            questionBox.setManaged(false);
        }
    }

    public void initData(Reponse r, Runnable onSuccess) {
        this.onSuccess = onSuccess;
        if (r != null) setReponse(r);
    }

    @FXML
    public void sauvegarder() {
        errorLabel.setText("");
        try {
            if (questionCombo.getValue() == null)
                throw new IllegalArgumentException("Veuillez sélectionner une question.");

            Reponse r = reponseExistante != null ? reponseExistante : new Reponse();
            r.setTexte(texteField.getText());
            r.setExplication_reponse(explicationField.getText());
            r.setEst_correcte(correcteCheck.isSelected());
            r.setQuestion(questionCombo.getValue());
            r.valider();

            if (reponseExistante == null) service.ajouter(r);
            else service.modifier(r);
            
            if (onSuccess != null) {
                onSuccess.run();
            } else {
                fermer();
            }
        } catch (IllegalArgumentException e) {
            errorLabel.setText("⚠ " + e.getMessage());
        }
    }

    @FXML public void annuler() { fermer(); }

    private void fermer() {
        if (DashboardController.instance != null) {
            DashboardController.instance.ouvrirReponse();
        } else {
            ((Stage) texteField.getScene().getWindow()).close();
        }
    }
}
