package tn.formini.controllers;

import javafx.collections.FXCollections;
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
import tn.formini.entities.Question;
import tn.formini.entities.Reponse;
import tn.formini.services.QuestionService;
import tn.formini.services.ReponseService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReponseController implements Initializable {

    @FXML private TableView<Reponse> tableReponse;
    @FXML private TableColumn<Reponse, Integer> colId;
    @FXML private TableColumn<Reponse, String> colTexte;
    @FXML private TableColumn<Reponse, Boolean> colCorrecte;
    @FXML private TableColumn<Reponse, String> colExplication;
    @FXML private TableColumn<Reponse, String> colQuestion;
    @FXML private TableColumn<Reponse, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<Question> filtreQuestion;

    private final ReponseService service = new ReponseService();
    private final QuestionService questionService = new QuestionService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTexte.setCellValueFactory(new PropertyValueFactory<>("texte"));
        colCorrecte.setCellValueFactory(new PropertyValueFactory<>("est_correcte"));
        colExplication.setCellValueFactory(new PropertyValueFactory<>("explication_reponse"));
        ajouterColonneActions();
        chargerQuestionCombo();
        chargerDonnees();
    }

    private void chargerQuestionCombo() {
        filtreQuestion.getItems().add(null);
        filtreQuestion.getItems().addAll(questionService.getAll());
        filtreQuestion.setConverter(new javafx.util.StringConverter<Question>() {
            public String toString(Question q) { return q == null ? "Toutes les questions" : q.getEnonce(); }
            public Question fromString(String s) { return null; }
        });
    }

    private void chargerDonnees() {
        tableReponse.setItems(FXCollections.observableArrayList(service.getAll()));
    }

    @FXML
    public void filtrerReponses() {
        String texte = searchField.getText().toLowerCase();
        Question qFiltre = filtreQuestion.getValue();
        List<Reponse> filtres = service.getAll().stream()
                .filter(r -> r.getTexte().toLowerCase().contains(texte))
                .filter(r -> qFiltre == null || (r.getQuestion() != null && r.getQuestion().getId() == qFiltre.getId()))
                .collect(Collectors.toList());
        tableReponse.setItems(FXCollections.observableArrayList(filtres));
    }

    public void filtrerParQuestion(Question question) {
        if (question != null) {
            for (Question q : filtreQuestion.getItems()) {
                if (q != null && q.getId() == question.getId()) {
                    filtreQuestion.setValue(q);
                    filtrerReponses();
                    break;
                }
            }
        }
    }

    @FXML
    public void ouvrirFormAjout() { ouvrirFormulaire(null); }

    private void ouvrirFormulaire(Reponse reponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/formini/views/ReponseForm.fxml"));
            Parent root = loader.load();
            ReponseFormController ctrl = loader.getController();
            if (reponse != null) ctrl.setReponse(reponse);

            Stage stage = new Stage();
            stage.setTitle(reponse == null ? "Nouvelle Réponse" : "Modifier Réponse");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            chargerDonnees();
        } catch (IOException e) {
            System.out.println("Erreur formulaire réponse : " + e.getMessage());
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

    private void supprimer(Reponse r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette réponse ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) { service.supprimer(r.getId()); chargerDonnees(); }
        });
    }
}
