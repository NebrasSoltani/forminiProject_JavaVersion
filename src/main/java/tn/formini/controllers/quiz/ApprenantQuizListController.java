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
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        
        // Effet de base (carte blanche)
        String styleNormale = "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 2, 4); -fx-pref-width: 250; -fx-pref-height: 180;";
        String styleHover = "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(105, 116, 232, 0.4), 25, 0, 4, 8); -fx-pref-width: 250; -fx-pref-height: 180;";
        
        card.setStyle(styleNormale);

        // Animation de soulèvement au survol
        card.setOnMouseEntered(e -> {
            card.setStyle(styleHover);
            card.setTranslateY(-4);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(styleNormale);
            card.setTranslateY(0);
        });

        Label lblTitre = new Label("📋 " + quiz.getTitre());
        lblTitre.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: 900; -fx-font-size: 16px;");
        lblTitre.setWrapText(true);

        Label lblDuree = new Label("⏳ Durée : " + quiz.getDuree() + " min");
        lblDuree.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        Label lblNote = new Label("🎯 Score requis : " + quiz.getNote_minimale() + "%");
        lblNote.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        // Pousser le bouton tout en bas
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button btnPasser = new Button("Commencer 👉");
        btnPasser.setMaxWidth(Double.MAX_VALUE);
        
        if (peutPasser) {
            String btnNorm = "-fx-background-color: linear-gradient(to right, #6974e8, #8b93f0); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10px; -fx-cursor: hand;";
            String btnHov  = "-fx-background-color: linear-gradient(to right, #515ec8, #6974e8); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10px; -fx-cursor: hand;";

            btnPasser.setStyle(btnNorm);
            btnPasser.setOnAction(e -> ouvrirQuizPasser(quiz.getId(), formationId));
            
            btnPasser.setOnMouseEntered(e -> btnPasser.setStyle(btnHov));
            btnPasser.setOnMouseExited(e -> btnPasser.setStyle(btnNorm));
        } else {
            btnPasser.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10px;");
            btnPasser.setDisable(true);
        }

        card.getChildren().addAll(lblTitre, lblDuree, lblNote, spacer, btnPasser);
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
