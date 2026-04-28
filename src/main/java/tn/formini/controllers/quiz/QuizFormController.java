package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.formini.entities.Quiz;
import tn.formini.services.QuizService;

import java.net.URL;
import java.util.ResourceBundle;

public class QuizFormController implements Initializable {

    @FXML private Label titreLabel;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField dureeField;
    @FXML private TextField noteMinField;
    @FXML private CheckBox melangerCheck;
    @FXML private CheckBox afficherCorrectionCheck;
    @FXML private Label errorLabel;

    private final QuizService service = new QuizService();
    private Quiz quizExistant = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setQuiz(Quiz quiz) {
        this.quizExistant = quiz;
        titreLabel.setText("Modifier Quiz");
        titreField.setText(quiz.getTitre());
        descriptionField.setText(quiz.getDescription());
        dureeField.setText(String.valueOf(quiz.getDuree()));
        noteMinField.setText(String.valueOf(quiz.getNote_minimale()));
        melangerCheck.setSelected(quiz.isMelanger());
        afficherCorrectionCheck.setSelected(quiz.isAfficher_correction());
    }

    @FXML
    public void sauvegarder() {
        errorLabel.setText("");
        try {
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            int duree = Integer.parseInt(dureeField.getText().trim());
            int noteMin = Integer.parseInt(noteMinField.getText().trim());

            Quiz quiz = quizExistant != null ? quizExistant : new Quiz();
            quiz.setTitre(titre);
            quiz.setDescription(description);
            quiz.setDuree(duree);
            quiz.setNote_minimale(noteMin);
            quiz.setMelanger(melangerCheck.isSelected());
            quiz.setAfficher_correction(afficherCorrectionCheck.isSelected());
            quiz.valider();

            if (quizExistant == null) {
                service.ajouter(quiz);
            } else {
                service.modifier(quiz);
            }
            fermer();
        } catch (NumberFormatException e) {
            errorLabel.setText("⚠ La durée et la note minimale doivent être des nombres.");
        } catch (IllegalArgumentException e) {
            errorLabel.setText("⚠ " + e.getMessage());
        }
    }

    @FXML
    public void annuler() {
        fermer();
    }

    private void fermer() {
        ((Stage) titreField.getScene().getWindow()).close();
    }
}
