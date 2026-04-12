package tn.formini.controllers;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Quiz;
import tn.formini.services.QuizService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuizController implements Initializable {

    @FXML private TableView<Quiz> tableQuiz;
    @FXML private TableColumn<Quiz, Integer> colId;
    @FXML private TableColumn<Quiz, String> colTitre;
    @FXML private TableColumn<Quiz, String> colDescription;
    @FXML private TableColumn<Quiz, Integer> colDuree;
    @FXML private TableColumn<Quiz, Integer> colNote;
    @FXML private TableColumn<Quiz, Boolean> colMelanger;
    @FXML private TableColumn<Quiz, Void> colActions;
    @FXML private TextField searchField;

    private final QuizService service = new QuizService();
    private ObservableList<Quiz> quizList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note_minimale"));
        colMelanger.setCellValueFactory(new PropertyValueFactory<>("melanger"));
        ajouterColonneActions();
        chargerDonnees();
        styleTable();
    }

    private void chargerDonnees() {
        quizList.setAll(service.getAll());
        tableQuiz.setItems(quizList);
    }

    @FXML
    public void filtrerQuiz() {
        String filtre = searchField.getText().toLowerCase();
        List<Quiz> filtres = service.getAll().stream()
                .filter(q -> q.getTitre().toLowerCase().contains(filtre))
                .collect(Collectors.toList());
        tableQuiz.setItems(FXCollections.observableArrayList(filtres));
    }

    @FXML
    public void ouvrirFormAjout() {
        ouvrirFormulaire(null);
    }

    private void ouvrirFormulaire(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/formini/views/QuizForm.fxml"));
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

    private void ajouterColonneActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btnEdit = new Button("✏");
            final Button btnDel = new Button("🗑");
            final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnDel.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnEdit.setOnAction(e -> ouvrirFormulaire(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
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

    private void styleTable() {
        tableQuiz.setStyle("-fx-background-color: #16213e; -fx-text-fill: white;");
    }

    private void showAlert(String titre, String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
