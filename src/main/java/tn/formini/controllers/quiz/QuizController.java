package tn.formini.controllers.quiz;

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
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.controllers.QuizFormController;
import tn.formini.entities.Quiz;
import tn.formini.services.QuizService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuizController implements Initializable {

    @FXML private FlowPane quizContainer;
    @FXML private TextField searchField;
    @FXML private Label quizCountBadge;

    private final QuizService service = new QuizService();
    private ObservableList<Quiz> quizList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerDonnees();
    }

    private void chargerDonnees() {
        quizList.setAll(service.getAll());
        afficherCartes(quizList);
    }

    private void afficherCartes(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();
        for (Quiz quiz : quizzes) {
            quizContainer.getChildren().add(creerCarteQuiz(quiz));
        }
        if (quizCountBadge != null)
            quizCountBadge.setText(quizzes.size() + " quiz disponible" + (quizzes.size() > 1 ? "s" : ""));
    }

    @FXML
    public void filtrerQuiz() {
        String filtre = searchField.getText().toLowerCase();
        List<Quiz> filtres = service.getAll().stream()
                .filter(q -> q.getTitre().toLowerCase().contains(filtre) || q.getDescription().toLowerCase().contains(filtre))
                .collect(Collectors.toList());
        afficherCartes(filtres);
    }

    @FXML
    public void ouvrirFormAjout() {
        ouvrirFormulaire(null);
    }

    private void ouvrirFormulaire(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizForm.fxml"));
            Parent root = loader.load();
            QuizFormController ctrl = loader.getController();
            if (quiz != null) ctrl.setQuiz(quiz);

            Stage stage = new Stage();
            stage.setTitle(quiz == null ? "Nouveau Quiz" : "Modifier Quiz");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            chargerDonnees();
        } catch (IOException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private VBox creerCarteQuiz(Quiz quiz) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1e293b, #162032);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #0891b2 transparent transparent transparent;" +
            "-fx-border-width: 3 0 0 0;" +
            "-fx-border-radius: 16;"
        );
        card.setPrefWidth(330);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(8, 145, 178, 0.22));
        shadow.setRadius(22);
        shadow.setOffsetY(6);
        card.setEffect(shadow);

        // Top accent line + title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 20px;");
        Label lblTitre = new Label(quiz.getTitre());
        lblTitre.setStyle("-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: white;");
        lblTitre.setWrapText(true);
        HBox.setHgrow(lblTitre, javafx.scene.layout.Priority.ALWAYS);
        titleRow.getChildren().addAll(icon, lblTitre);

        // Description
        Label lblDesc = new Label(quiz.getDescription());
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-line-spacing: 2;");

        // Divider
        Region divider = new Region();
        divider.setStyle("-fx-background-color: #1e3a5f; -fx-min-height: 1; -fx-max-height: 1;");

        // Stats chips
        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);

        Label lblDuree = new Label("⏱  " + quiz.getDuree() + " min");
        lblDuree.setStyle(
            "-fx-background-color: rgba(56,189,248,0.1);" +
            "-fx-border-color: rgba(56,189,248,0.25);" +
            "-fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20;" +
            "-fx-padding: 5 12; -fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: bold;"
        );

        Label lblNote = new Label("⭐  " + quiz.getNote_minimale() + " pts min");
        lblNote.setStyle(
            "-fx-background-color: rgba(251,191,36,0.1);" +
            "-fx-border-color: rgba(251,191,36,0.25);" +
            "-fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20;" +
            "-fx-padding: 5 12; -fx-text-fill: #fbbf24; -fx-font-size: 12px; -fx-font-weight: bold;"
        );

        stats.getChildren().addAll(lblDuree, lblNote);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnQs = new Button("👁  Questions");
        btnQs.setStyle(
            "-fx-background-color: rgba(105,116,232,0.15);" +
            "-fx-border-color: rgba(105,116,232,0.4); -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-text-fill: #a5b4fc; -fx-font-weight: bold; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-padding: 8 14;"
        );
        btnQs.setOnAction(e -> {
            if (tn.formini.controllers.DashboardController.instance != null)
                tn.formini.controllers.DashboardController.instance.ouvrirQuestionPourQuiz(quiz);
        });

        Button btnPdf = new Button("🖨️");
        btnPdf.setStyle(
            "-fx-background-color: rgba(251,191,36,0.12);" +
            "-fx-border-color: rgba(251,191,36,0.3); -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-padding: 8 12;"
        );
        btnPdf.setOnAction(e -> exporterPDF(quiz));

        Button btnEdit = new Button("✏");
        btnEdit.setStyle(
            "-fx-background-color: rgba(3,105,161,0.15);" +
            "-fx-border-color: rgba(3,105,161,0.35); -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-text-fill: #7dd3fc; -fx-font-weight: bold; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-padding: 8 12;"
        );
        btnEdit.setOnAction(e -> ouvrirFormulaire(quiz));

        Button btnDel = new Button("🗑");
        btnDel.setStyle(
            "-fx-background-color: rgba(239,68,68,0.12);" +
            "-fx-border-color: rgba(239,68,68,0.3); -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-text-fill: #f87171; -fx-font-weight: bold; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-padding: 8 12;"
        );
        btnDel.setOnAction(e -> supprimer(quiz));

        actions.getChildren().addAll(btnQs, btnPdf, btnEdit, btnDel);

        card.getChildren().addAll(titleRow, lblDesc, divider, stats, spacer, actions);
        return card;
    }

    private void exporterPDF(Quiz quiz) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le Quiz en PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Quiz_" + quiz.getTitre().replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf");
        
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                com.lowagie.text.Document document = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();

                // Titre
                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 22, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("Évaluation : " + quiz.getTitre(), titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Description
                document.add(new com.lowagie.text.Paragraph("Description : " + quiz.getDescription()));
                document.add(new com.lowagie.text.Paragraph("Durée : " + quiz.getDuree() + " minutes"));
                document.add(new com.lowagie.text.Paragraph("Note minimale : " + quiz.getNote_minimale()));
                document.add(new com.lowagie.text.Paragraph(" ")); // Spacer
                document.add(new com.lowagie.text.Paragraph("-------------------------------------------------------------------"));
                document.add(new com.lowagie.text.Paragraph(" ")); // Spacer

                // Questions
                tn.formini.services.QuestionService questionService = new tn.formini.services.QuestionService();
                tn.formini.services.ReponseService reponseService = new tn.formini.services.ReponseService();
                
                List<tn.formini.entities.Question> qs = questionService.getAll().stream()
                        .filter(q -> q.getQuiz() != null && q.getQuiz().getId() == quiz.getId())
                        .collect(Collectors.toList());

                List<tn.formini.entities.Reponse> allReps = reponseService.getAll();

                int index = 1;
                for (tn.formini.entities.Question q : qs) {
                    com.lowagie.text.Font qFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
                    com.lowagie.text.Paragraph qPara = new com.lowagie.text.Paragraph("Q" + index + ". " + q.getEnonce() + " (" + q.getPoints() + " pts)", qFont);
                    qPara.setSpacingBefore(15);
                    document.add(qPara);

                    List<tn.formini.entities.Reponse> reps = allReps.stream()
                            .filter(r -> r.getQuestion() != null && r.getQuestion().getId() == q.getId())
                            .collect(Collectors.toList());

                    for (tn.formini.entities.Reponse r : reps) {
                        // Checkbox empty box logic: [ ]
                        document.add(new com.lowagie.text.Paragraph("    [   ]  " + r.getTexte()));
                    }
                    document.add(new com.lowagie.text.Paragraph(" "));
                    index++;
                }

                document.close();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Le PDF a été généré avec succès à l'emplacement :\n" + file.getAbsolutePath());
                success.showAndWait();

            } catch (Exception ex) {
                showAlert("Erreur PDF", "Impossible de générer le fichier PDF : " + ex.getMessage());
            }
        }
    }

    private void supprimer(Quiz quiz) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le quiz \"" + quiz.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                service.supprimer(quiz.getId());
                chargerDonnees();
            }
        });
    }

    private void showAlert(String titre, String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
