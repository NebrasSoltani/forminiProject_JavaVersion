package tn.formini.controllers;

import javafx.beans.property.SimpleStringProperty;
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
    @FXML private TableColumn<Reponse, String> colTexte;
    @FXML private TableColumn<Reponse, Boolean> colCorrecte;
    @FXML private TableColumn<Reponse, String> colExplication;
    @FXML private TableColumn<Reponse, String> colQuestion;
    @FXML private TableColumn<Reponse, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<Question> filtreQuestion;
    @FXML private Label reponseCountLabel;

    private final ReponseService service = new ReponseService();
    private final QuestionService questionService = new QuestionService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTexte.setCellValueFactory(new PropertyValueFactory<>("texte"));
        colCorrecte.setCellValueFactory(new PropertyValueFactory<>("est_correcte"));
        colExplication.setCellValueFactory(new PropertyValueFactory<>("explication_reponse"));
        colQuestion.setCellValueFactory(data -> {
            tn.formini.entities.Quizs.Question q = data.getValue().getQuestion();
            return new SimpleStringProperty(q != null ? q.getEnonce() : "-");
        });
        ajouterColonneActions();
        styleTableDark();
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
        updateCount(tableReponse.getItems().size());
    }

    private void updateCount(int count) {
        if (reponseCountLabel != null)
            reponseCountLabel.setText(count + " réponse" + (count > 1 ? "s" : ""));
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
        updateCount(filtres.size());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ReponseForm.fxml"));
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
                btnEdit.setStyle("-fx-background-color: #0369a1; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 10;");
                btnDel.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 10;");
                btnEdit.setOnAction(e -> ouvrirFormulaire(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: " + (getIndex() % 2 == 0 ? "#1e293b" : "#0f172a") + ";");
                setGraphic(empty ? null : box);
            }
        });
    }

    private void styleTableDark() {
        tableReponse.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-table-cell-border-color: #334155;");

        // Callback typed as String to match colTexte, colExplication, colQuestion (all TableColumn<Reponse, String>)
        javafx.util.Callback<TableColumn<Reponse, String>, TableCell<Reponse, String>> darkCell = col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: " + (getIndex() % 2 == 0 ? "#1e293b" : "#0f172a") + "; -fx-text-fill: " + (empty || item == null ? "#64748b" : "#e2e8f0") + "; -fx-padding: 10 16;");
                setText(empty || item == null ? null : item);
            }
        };
        colTexte.setCellFactory(darkCell);
        colExplication.setCellFactory(darkCell);
        colQuestion.setCellFactory(darkCell);

        // colCorrecte is Boolean — separate factory
        colCorrecte.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: " + (getIndex() % 2 == 0 ? "#1e293b" : "#0f172a") + ";");
                } else {
                    setText(item ? "✅ Oui" : "❌ Non");
                    setStyle("-fx-background-color: " + (getIndex() % 2 == 0 ? "#1e293b" : "#0f172a") + "; -fx-text-fill: " + (item ? "#34d399" : "#f87171") + "; -fx-font-weight: bold; -fx-padding: 10 16;");
                }
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
