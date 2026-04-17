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
import tn.formini.entities.formations.Lecon;
import tn.formini.services.formations.LeconService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LeconCrudController {

    @FXML private Label headerLabel;
    @FXML private TableView<Lecon> leconTable;
    @FXML private TableColumn<Lecon, Integer> idColumn;
    @FXML private TableColumn<Lecon, String> titreColumn;
    @FXML private TableColumn<Lecon, Integer> ordreColumn;
    @FXML private TableColumn<Lecon, Integer> dureeColumn;
    @FXML private TableColumn<Lecon, Boolean> gratuitColumn;

    private final LeconService leconService = new LeconService();
    private Formation formation;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        ordreColumn.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        gratuitColumn.setCellValueFactory(new PropertyValueFactory<>("gratuit"));
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        openLeconForm(null);
    }

    @FXML
    private void handleEdit() {
        Lecon selected = leconTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selectionnez une lecon a modifier.");
            return;
        }
        openLeconForm(selected);
    }

    @FXML
    private void handleDelete() {
        Lecon selected = leconTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selectionnez une lecon a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la lecon");
        confirm.setContentText("Confirmer la suppression de '" + selected.getTitre() + "' ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            leconService.supprimer(selected.getId());
            refreshTable();
        }
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    private void refreshTable() {
        if (formation == null) {
            headerLabel.setText("Lecons");
            leconTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Lecon> lecons = leconService.findByFormationId(formation.getId());
        leconTable.setItems(FXCollections.observableArrayList(lecons));
        headerLabel.setText("Lecons de '" + formation.getTitre() + "' (" + lecons.size() + ")");
    }

    private void openLeconForm(Lecon lecon) {
        if (formation == null) {
            showError("Formation introuvable.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/lecon-form.fxml"));
            Parent root = loader.load();

            LeconFormController controller = loader.getController();
            controller.setFormation(formation);
            controller.setOnSaved(this::refreshTable);
            if (lecon != null) {
                controller.setLecon(lecon);
            }

            Stage stage = new Stage();
            stage.setTitle(lecon == null ? "Nouvelle lecon" : "Modifier lecon");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            showError("Impossible d'ouvrir le formulaire lecon: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

