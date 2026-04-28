package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Apprenant;
import tn.formini.entities.Quiz;
import tn.formini.services.ApprenantQuizService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ApprenantQuizListController implements Initializable {

    @FXML private TextField formationIdField;
    @FXML private Label statusLabel;
    @FXML private Label progressLabel;
    @FXML private FlowPane quizContainer;

    private ApprenantQuizService apprenantQuizService;
    private Apprenant currentApprenant;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apprenantQuizService = new ApprenantQuizService();
        // Simuler un apprenant connecté (ID 1)
        currentApprenant = new Apprenant();
        currentApprenant.setId(1);
    }

    @FXML
    public void chargerQuiz() {
        quizContainer.getChildren().clear();
        statusLabel.setText("");
        
        try {
            int formationId = Integer.parseInt(formationIdField.getText());
            Map<String, Object> data = apprenantQuizService.index(currentApprenant, formationId);

            if (data.containsKey("erreur")) {
                statusLabel.setText((String) data.get("erreur"));
                progressLabel.setText("Non inscrit");
                return;
            }

            int leconsTerminees = (int) data.getOrDefault("leconsTerminees", 0);
            int totalLecons = (int) data.getOrDefault("totalLecons", 0);
            boolean toutesTerminees = (boolean) data.getOrDefault("toutesLeconsTerminees", false);

            progressLabel.setText(String.format("Leçons terminées : %d / %d", leconsTerminees, totalLecons));
            if (totalLecons > 0 && !toutesTerminees) {
                statusLabel.setText("⚠️ Vous devez terminer toutes les leçons avant de passer les quiz.");
            }

            List<Quiz> quizzes = (List<Quiz>) data.get("quizzes");
            if (quizzes == null || quizzes.isEmpty()) {
                statusLabel.setText("Aucun quiz disponible pour cette formation.");
                return;
            }

            // Générer les cartes pour chaque quiz
            for (Quiz quiz : quizzes) {
                VBox card = createQuizCard(quiz, formationId, toutesTerminees);
                quizContainer.getChildren().add(card);
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("L'ID formation doit être un nombre.");
        }
    }

    private VBox createQuizCard(Quiz quiz, int formationId, boolean peutPasser) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #16213e; -fx-border-color: #e94560; -fx-border-radius: 8; -fx-background-radius: 8; -fx-pref-width: 200;");

        Label lblTitre = new Label(quiz.getTitre());
        lblTitre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblDuree = new Label("⏳ " + quiz.getDuree() + " min");
        lblDuree.setStyle("-fx-text-fill: #a8a8b3;");

        Label lblNote = new Label("🎯 Note Min: " + quiz.getNote_minimale() + "%");
        lblNote.setStyle("-fx-text-fill: #a8a8b3;");

        Button btnPasser = new Button("Passer le Quiz");
        btnPasser.setMaxWidth(Double.MAX_VALUE);
        
        if (peutPasser) {
            btnPasser.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-cursor: hand;");
            btnPasser.setOnAction(e -> ouvrirQuizPasser(quiz.getId(), formationId));
        } else {
            btnPasser.setStyle("-fx-background-color: #2d2d44; -fx-text-fill: #a8a8b3;");
            btnPasser.setDisable(true);
        }

        card.getChildren().addAll(lblTitre, lblDuree, lblNote, btnPasser);
        return card;
    }

    private void ouvrirQuizPasser(int quizId, int formationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/formini/views/ApprenantQuizPasser.fxml"));
            Parent root = loader.load();
            
            ApprenantQuizPasserController ctrl = loader.getController();
            ctrl.initData(currentApprenant, formationId, quizId);

            Stage stage = new Stage();
            stage.setTitle("Passer le Quiz");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 700, 500));
            stage.showAndWait();
            
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du quiz : " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }
}
