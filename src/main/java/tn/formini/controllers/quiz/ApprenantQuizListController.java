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
        
        // IMPORTANT: Un apprenant a besoin d'un objet User associé pour la soumission
        tn.formini.entities.Users.User mockUser = new tn.formini.entities.Users.User();
        mockUser.setId(1);
        mockUser.setNom("ApprenantTest");
        currentApprenant.setUser(mockUser);
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

            progressLabel.setText(String.format("%d / %d", leconsTerminees, totalLecons));
            if (totalLecons > 0 && !toutesTerminees) {
                statusLabel.setText("⚠️ Vous devez terminer toutes les leçons");
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
        VBox card = new VBox();
        card.setMaxWidth(320);
        card.setPrefWidth(320);
        card.setMinHeight(280);
        
        String styleNormale = "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 15, 0, 0, 5);";
        String styleHover = "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 25, 0, 0, 10);";
        
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

        // Banner Header
        javafx.scene.layout.VBox banner = new javafx.scene.layout.VBox();
        banner.setPrefHeight(110);
        // Randomize banner colors slightly based on ID for variety
        String[] colors = {"#6366f1, #8b5cf6", "#3b82f6, #2dd4bf", "#ec4899, #f43f5e", "#f59e0b, #ef4444"};
        String colorGradient = colors[quiz.getId() % colors.length];
        banner.setStyle("-fx-background-color: linear-gradient(to bottom right, " + colorGradient + "); -fx-background-radius: 16 16 0 0; -fx-alignment: center;");
        Label iconLabel = new Label("📝");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        banner.getChildren().add(iconLabel);

        // Body
        VBox body = new VBox(15);
        body.setPadding(new Insets(20));
        VBox.setVgrow(body, javafx.scene.layout.Priority.ALWAYS);

        Label lblTitre = new Label(quiz.getTitre());
        lblTitre.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: 900; -fx-font-size: 18px;");
        lblTitre.setWrapText(true);
        lblTitre.setMaxWidth(280);

        javafx.scene.layout.HBox badges = new javafx.scene.layout.HBox(10);
        Label lblDuree = new Label("⏳ " + quiz.getDuree() + " min");
        lblDuree.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 8; -fx-font-weight: bold;");

        Label lblNote = new Label("🎯 " + quiz.getNote_minimale() + "% requis");
        lblNote.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 8; -fx-font-weight: bold;");
        badges.getChildren().addAll(lblDuree, lblNote);

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button btnPasser = new Button("Commencer 👉");
        btnPasser.setMaxWidth(Double.MAX_VALUE);
        
        if (peutPasser) {
            String btnNorm = "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 12px; -fx-cursor: hand;";
            String btnHov  = "-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 12px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);";

            btnPasser.setStyle(btnNorm);
            btnPasser.setOnAction(e -> ouvrirQuizPasser(quiz.getId(), formationId));
            
            btnPasser.setOnMouseEntered(e -> btnPasser.setStyle(btnHov));
            btnPasser.setOnMouseExited(e -> btnPasser.setStyle(btnNorm));
        } else {
            btnPasser.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12px;");
            btnPasser.setDisable(true);
        }

        body.getChildren().addAll(lblTitre, badges, spacer, btnPasser);
        card.getChildren().addAll(banner, body);
        return card;
    }

    private void ouvrirQuizPasser(int quizId, int formationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ApprenantQuizPasser.fxml"));
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
