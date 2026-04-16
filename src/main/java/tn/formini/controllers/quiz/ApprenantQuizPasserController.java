package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.formini.entities.Apprenant;
import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.entities.Reponse;
import tn.formini.entities.ResultatQuiz;
import tn.formini.services.ApprenantQuizService;
import tn.formini.services.QuestionService;
import tn.formini.services.ReponseService;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ApprenantQuizPasserController implements Initializable {

    @FXML private Label quizTitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label progressLabel;
    @FXML private Label questionEnonceLabel;
    @FXML private VBox reponsesContainer;
    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;
    @FXML private Button btnSoumettre;
    @FXML private Label errorLabel;

    @FXML private VBox resultBox;
    @FXML private Label resultNoteLabel;
    @FXML private Label resultBonnesLabel;

    private Apprenant apprenant;
    private int formationId;
    private Quiz quiz;
    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    
    // Maps Question ID -> Selected Reponse ID
    private Map<Integer, Integer> reponsesUtilisateur = new HashMap<>();

    private final ApprenantQuizService apprenantQuizService = new ApprenantQuizService();
    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    private ToggleGroup toggleGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toggleGroup = new ToggleGroup();
    }

    public void initData(Apprenant apprenant, int formationId, int quizId) {
        this.apprenant = apprenant;
        this.formationId = formationId;
        
        Map<String, Object> data = apprenantQuizService.passer(apprenant, formationId, quizId);
        if (data.containsKey("erreur") || data.containsKey("avertissement")) {
            errorLabel.setText((String) data.getOrDefault("erreur", data.get("avertissement")));
            btnSuivant.setDisable(true);
            return;
        }

        this.quiz = (Quiz) data.get("quiz");
        quizTitleLabel.setText(quiz.getTitre());
        timerLabel.setText("Durée: " + quiz.getDuree() + " min");

        // Load actual questions since passer() only returns strings
        List<Question> allQuestions = questionService.getAll();
        this.questions = allQuestions.stream()
                .filter(q -> q.getQuiz() != null && q.getQuiz().getId() == quizId)
                .collect(Collectors.toList());

        if (quiz.isMelanger()) {
            Collections.shuffle(this.questions);
        }

        if (questions.isEmpty()) {
            errorLabel.setText("Ce quiz ne contient aucune question.");
            btnSuivant.setDisable(true);
        } else {
            afficherQuestion(0);
        }
    }

    private void afficherQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        
        // Save current selection before moving
        sauvegarderSelection(currentIndex);

        this.currentIndex = index;
        Question currentQ = questions.get(index);

        progressLabel.setText(String.format("Question %d / %d", index + 1, questions.size()));
        questionEnonceLabel.setText(currentQ.getEnonce());
        
        reponsesContainer.getChildren().clear();
        toggleGroup.getToggles().clear();

        List<Reponse> reponses = reponseService.getAll().stream()
                .filter(r -> r.getQuestion() != null && r.getQuestion().getId() == currentQ.getId())
                .collect(Collectors.toList());

        Integer reponsePrecedenteId = reponsesUtilisateur.get(currentQ.getId());

        for (Reponse r : reponses) {
            RadioButton rb = new RadioButton(r.getTexte());
            rb.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
            rb.setUserData(r.getId());
            rb.setToggleGroup(toggleGroup);

            if (reponsePrecedenteId != null && reponsePrecedenteId == r.getId()) {
                rb.setSelected(true);
            }
            reponsesContainer.getChildren().add(rb);
        }

        updateButtons();
    }

    private void sauvegarderSelection(int index) {
        if (index < 0 || index >= questions.size()) return;
        Question currentQ = questions.get(index);
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            Integer repId = (Integer) selectedToggle.getUserData();
            reponsesUtilisateur.put(currentQ.getId(), repId);
        } else {
            reponsesUtilisateur.remove(currentQ.getId());
        }
    }

    private void updateButtons() {
        btnPrecedent.setVisible(currentIndex > 0);
        btnPrecedent.setManaged(currentIndex > 0);

        boolean isLast = currentIndex == questions.size() - 1;
        btnSuivant.setVisible(!isLast);
        btnSuivant.setManaged(!isLast);
        
        btnSoumettre.setVisible(isLast);
        btnSoumettre.setManaged(isLast);
    }

    @FXML
    public void questionSuivante() {
        afficherQuestion(currentIndex + 1);
    }

    @FXML
    public void questionPrecedente() {
        afficherQuestion(currentIndex - 1);
    }

    @FXML
    public void soumettreQuiz() {
        sauvegarderSelection(currentIndex);

        // Verification if all questions answered
        if (reponsesUtilisateur.size() < questions.size()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "Vous n'avez pas répondu à toutes les questions. Voulez-vous quand même soumettre ?", 
                ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.YES) return;
        }

        try {
            ResultatQuiz resultat = apprenantQuizService.soumettre(apprenant, formationId, quiz.getId(), reponsesUtilisateur);
            
            // Hide quiz UI and show results
            questionEnonceLabel.setVisible(false);
            reponsesContainer.setVisible(false);
            progressLabel.setText("Terminé");
            timerLabel.setText("");

            btnPrecedent.setVisible(false);
            btnSuivant.setVisible(false);
            btnSoumettre.setVisible(false);

            resultBox.setVisible(true);
            resultBox.setManaged(true);

            resultNoteLabel.setText("Note: " + resultat.getNote() + "% - " + (resultat.isReussi() ? "✅ Réussi" : "❌ Échoué"));
            resultBonnesLabel.setText(String.format("Bonnes réponses : %d / %d", 
                resultat.getNombre_bonnes_reponses(), resultat.getNombre_total_questions()));

        } catch (Exception e) {
            errorLabel.setText("Erreur lors de la soumission : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void fermer() {
        ((Stage) btnSoumettre.getScene().getWindow()).close();
    }
}
