package tn.formini.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.formini.entities.ResultatQuiz;
import tn.formini.entities.Quiz;
import tn.formini.entities.Users.User;
import tn.formini.services.quizService.ResultatQuizService;
import tn.formini.services.quizService.QuizService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ResultatController implements Initializable {

    @FXML private ComboBox<Quiz> filtreQuiz;
    @FXML private ToggleButton btnTous;
    @FXML private ToggleButton btnReussis;
    @FXML private ToggleButton btnNonReussis;
    @FXML private TextField searchField;
    @FXML private Label resultCountLabel;
    @FXML private VBox participantsContainer;

    private final ResultatQuizService resultatService = new ResultatQuizService();
    private final QuizService quizService = new QuizService();
    
    private List<ResultatQuiz> allResultats;
    private ToggleGroup filterGroup;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterGroup = new ToggleGroup();
        btnTous.setToggleGroup(filterGroup);
        btnReussis.setToggleGroup(filterGroup);
        btnNonReussis.setToggleGroup(filterGroup);

        filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                btnTous.setSelected(true); // Always keep one selected
            }
            updateToggleStyles();
            filtrer();
        });

        chargerQuizCombo();
        chargerDonnees();
    }

    private void chargerQuizCombo() {
        filtreQuiz.getItems().add(null);
        filtreQuiz.getItems().addAll(quizService.getAll());
        filtreQuiz.setConverter(new javafx.util.StringConverter<Quiz>() {
            @Override
            public String toString(Quiz q) { return q == null ? "Tous les quiz" : q.getTitre(); }
            @Override
            public Quiz fromString(String s) { return null; }
        });
        
        filtreQuiz.setOnAction(e -> filtrer());
    }

    private void chargerDonnees() {
        allResultats = resultatService.getAll();
        filtrer();
    }

    @FXML
    public void filtrer() {
        if (allResultats == null) return;

        String search = searchField.getText().toLowerCase();
        Quiz selectedQuiz = filtreQuiz.getValue();
        
        boolean showReussis = btnReussis.isSelected();
        boolean showNonReussis = btnNonReussis.isSelected();

        List<ResultatQuiz> filtres = allResultats.stream()
                .filter(r -> selectedQuiz == null || (r.getQuiz() != null && r.getQuiz().getId() == selectedQuiz.getId()))
                .filter(r -> {
                    if (showReussis) return r.isReussi();
                    if (showNonReussis) return !r.isReussi();
                    return true; // btnTous
                })
                .filter(r -> {
                    User u = r.getApprenant();
                    if (u == null) return search.isEmpty();
                    String nomPrenom = (u.getNom() + " " + u.getPrenom()).toLowerCase();
                    String email = (u.getEmail() != null) ? u.getEmail().toLowerCase() : "";
                    return nomPrenom.contains(search) || email.contains(search);
                })
                .collect(Collectors.toList());

        afficherResultats(filtres);
    }

    private void updateToggleStyles() {
        // Reset styles
        btnTous.setStyle("-fx-background-color: white; -fx-text-fill: #6366f1; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 4 0 0 4; -fx-border-color: #6366f1; -fx-cursor: hand;");
        btnReussis.setStyle("-fx-background-color: white; -fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-color: #22c55e; -fx-border-width: 1 1 1 0; -fx-cursor: hand;");
        btnNonReussis.setStyle("-fx-background-color: white; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 0 4 4 0; -fx-border-color: #ef4444; -fx-border-width: 1 1 1 0; -fx-cursor: hand;");

        // Apply active styles
        if (btnTous.isSelected()) {
            btnTous.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 4 0 0 4; -fx-border-color: #6366f1; -fx-cursor: hand;");
        } else if (btnReussis.isSelected()) {
            btnReussis.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-color: #22c55e; -fx-border-width: 1 1 1 0; -fx-cursor: hand;");
        } else if (btnNonReussis.isSelected()) {
            btnNonReussis.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 0 4 4 0; -fx-border-color: #ef4444; -fx-border-width: 1 1 1 0; -fx-cursor: hand;");
        }
    }

    private void afficherResultats(List<ResultatQuiz> liste) {
        participantsContainer.getChildren().clear();
        if (resultCountLabel != null) resultCountLabel.setText(liste.size() + " résultats");

        for (ResultatQuiz r : liste) {
            HBox row = new HBox(20);
            row.setStyle("-fx-padding: 14 24; -fx-border-color: #334155; -fx-border-width: 0 0 1 0; -fx-background-color: #1e293b;");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Column 1: Apprenant
            HBox apprenantBox = new HBox(15);
            apprenantBox.setPrefWidth(300);
            apprenantBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            User u = r.getApprenant();
            String fullName = u != null ? u.getNom() + " " + u.getPrenom() : "Inconnu";
            String initiales = u != null && u.getNom() != null && !u.getNom().isEmpty() ? u.getNom().substring(0, 1).toUpperCase() : "?";
            String email = u != null && u.getEmail() != null ? u.getEmail() : "Pas d'email";

            Label avatar = new Label(initiales);
            avatar.setStyle("-fx-background-color: #0369a1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");

            VBox nameBox = new VBox(2);
            Label nameLabel = new Label(fullName);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            Label subEmailLabel = new Label(email);
            subEmailLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
            nameBox.getChildren().addAll(nameLabel, subEmailLabel);

            apprenantBox.getChildren().addAll(avatar, nameBox);

            // Column 2: Email
            Label emailLabel = new Label(email);
            emailLabel.setPrefWidth(300);
            emailLabel.setStyle("-fx-text-fill: #64748b;");

            // Column 3: Statut
            Label statutBadge = new Label();
            statutBadge.setPrefWidth(120);
            if (r.isReussi()) {
                statutBadge.setText("✓ Réussi");
                statutBadge.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-border-color: #22c55e; -fx-border-radius: 12; -fx-padding: 4 12; -fx-background-color: #f0fdf4; -fx-background-radius: 12;");
            } else {
                statutBadge.setText("✗ Non réussi");
                statutBadge.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-border-color: #ef4444; -fx-border-radius: 12; -fx-padding: 4 12; -fx-background-color: #fef2f2; -fx-background-radius: 12;");
            }

            // Column 4: Actions (PDF)
            Button btnPdf = new Button("🖨️ Rapport");
            btnPdf.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 5 10;");
            btnPdf.setOnAction(e -> exporterRapportPDF(r, fullName));

            row.getChildren().addAll(apprenantBox, emailLabel, statutBadge, btnPdf);
            participantsContainer.getChildren().add(row);
        }
    }

    private void exporterRapportPDF(ResultatQuiz r, String fullName) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le rapport en PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Rapport_" + fullName.replaceAll(" ", "_") + ".pdf");
        
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                com.lowagie.text.Document document = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();

                // Fonts
                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 24, com.lowagie.text.Font.BOLD, java.awt.Color.BLUE);
                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.NORMAL);
                com.lowagie.text.Font successFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD, java.awt.Color.GREEN);
                com.lowagie.text.Font errorFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD, java.awt.Color.RED);

                // Title
                com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("Bilan de Compétences", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(30);
                document.add(title);

                // Info
                String quizName = r.getQuiz() != null && r.getQuiz().getTitre() != null ? r.getQuiz().getTitre() : "Quiz inconnu";
                document.add(new com.lowagie.text.Paragraph("Apprenant : " + fullName, headerFont));
                document.add(new com.lowagie.text.Paragraph("Évaluation : " + quizName, headerFont));
                document.add(new com.lowagie.text.Paragraph("Date : " + r.getDate_tentative(), normalFont));
                document.add(new com.lowagie.text.Paragraph(" "));

                // Score
                document.add(new com.lowagie.text.Paragraph("-------------------------------------------------------------------"));
                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("Score Obtenu : " + r.getNote() + " %", headerFont));
                document.add(new com.lowagie.text.Paragraph("Bonnes réponses : " + r.getNombre_bonnes_reponses() + " / " + r.getNombre_total_questions(), normalFont));
                
                com.lowagie.text.Paragraph status = new com.lowagie.text.Paragraph(
                    r.isReussi() ? "Statut : RÉUSSI" : "Statut : ÉCHEC",
                    r.isReussi() ? successFont : errorFont
                );
                document.add(status);
                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("-------------------------------------------------------------------"));
                document.add(new com.lowagie.text.Paragraph(" "));

                // AI Details
                if (r.getDetails_reponses() != null && !r.getDetails_reponses().isEmpty()) {
                    document.add(new com.lowagie.text.Paragraph("Feedback et Recommandations :", headerFont));
                    document.add(new com.lowagie.text.Paragraph(" "));
                    document.add(new com.lowagie.text.Paragraph(r.getDetails_reponses(), normalFont));
                }

                document.close();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Le rapport PDF a été généré avec succès :\n" + file.getAbsolutePath());
                success.showAndWait();

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du PDF : " + ex.getMessage()).showAndWait();
            }
        }
    }
}
