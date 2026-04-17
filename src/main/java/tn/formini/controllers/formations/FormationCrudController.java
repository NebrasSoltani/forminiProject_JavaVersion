package tn.formini.controllers.formations;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.formations.Formation;
import tn.formini.services.formations.FormationService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FormationCrudController {

    @FXML private Label headerLabel;
    @FXML private TableView<Formation> formationTable;
    @FXML private TableColumn<Formation, Integer> idColumn;
    @FXML private TableColumn<Formation, String> titreColumn;
    @FXML private TableColumn<Formation, String> categorieColumn;
    @FXML private TableColumn<Formation, String> niveauColumn;
    @FXML private TableColumn<Formation, String> langueColumn;
    @FXML private TableColumn<Formation, Integer> dureeColumn;
    @FXML private TableColumn<Formation, Integer> leconsColumn;
    @FXML private TableColumn<Formation, String> statutColumn;
    @FXML private TableColumn<Formation, java.math.BigDecimal> prixColumn;

    private final FormationService formationService = new FormationService();
    private int formateurId;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        niveauColumn.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        langueColumn.setCellValueFactory(new PropertyValueFactory<>("langue"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        leconsColumn.setCellValueFactory(new PropertyValueFactory<>("nombre_lecons"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
    }

    public void setFormateurId(int formateurId) {
        this.formateurId = formateurId;
        refreshTable();
    }

    @FXML
    private void handleCreate() {
        openFormationForm(null);
    }

    @FXML
    private void handleEdit() {
        Formation selected = formationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selectionnez une formation a modifier.");
            return;
        }
        openFormationForm(selected);
    }

    @FXML
    private void handleDelete() {
        Formation selected = formationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selectionnez une formation a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la formation");
        confirm.setContentText("La formation '" + selected.getTitre() + "' et ses lecons seront supprimees.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            formationService.supprimer(selected.getId());
            refreshTable();
        }
    }

    @FXML
    private void handleManageLessons() {
        Formation selected = formationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selectionnez une formation pour gerer ses lecons.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/lecon-crud.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            controller.getClass().getMethod("setFormation", Formation.class).invoke(controller, selected);

            Stage stage = new Stage();
            stage.setTitle("Lecons - " + selected.getTitre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshTable();
        } catch (Exception ex) {
            showError("Impossible d'ouvrir la gestion des lecons: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    private void refreshTable() {
        List<Formation> formations = formationService.findByFormateurId(formateurId);
        formationTable.setItems(FXCollections.observableArrayList(formations));
        headerLabel.setText("Mes formations (" + formations.size() + ")");
    }

    private void openFormationForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/formation-form.fxml"));
            Parent root = loader.load();

            FormationFormController controller = loader.getController();
            controller.setFormateurId(formateurId);
            controller.setOnSaved(this::refreshTable);
            if (formation != null) {
                controller.setFormation(formation);
            }

            Stage stage = new Stage();
            stage.setTitle(formation == null ? "Nouvelle formation" : "Modifier formation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            showError("Impossible d'ouvrir le formulaire formation: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
