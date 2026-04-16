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
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        card.setPrefWidth(320);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.05));
        shadow.setRadius(15);
        card.setEffect(shadow);

        Label lblTitre = new Label(quiz.getTitre());
        lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        
        Label lblDesc = new Label(quiz.getDescription());
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        HBox stats = new HBox(15);
        Label lblDuree = new Label("⏱ " + quiz.getDuree() + " min");
        lblDuree.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 4 8; -fx-background-radius: 6; -fx-text-fill: #475569; -fx-font-size: 12px;");
        Label lblNote = new Label("⭐ Note: " + quiz.getNote_minimale());
        lblNote.setStyle("-fx-background-color: #fef3c7; -fx-padding: 4 8; -fx-background-radius: 6; -fx-text-fill: #d97706; -fx-font-size: 12px;");
        stats.getChildren().addAll(lblDuree, lblNote);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnQs = new Button("👁 Questions");
        btnQs.setStyle("-fx-background-color: #6974e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 12;");
        btnQs.setOnAction(e -> {
            if (tn.formini.controllers.DashboardController.instance != null) {
                tn.formini.controllers.DashboardController.instance.ouvrirQuestionPourQuiz(quiz);
            }
        });

        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8;");
        btnEdit.setOnAction(e -> ouvrirFormulaire(quiz));

        Button btnDel = new Button("🗑");
        btnDel.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8;");
        btnDel.setOnAction(e -> supprimer(quiz));

        actions.getChildren().addAll(btnQs, btnEdit, btnDel);

        card.getChildren().addAll(lblTitre, lblDesc, stats, spacer, actions);
        return card;
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
