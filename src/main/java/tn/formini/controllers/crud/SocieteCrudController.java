package tn.formini.controllers.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class SocieteCrudController {

    @FXML
    private TableView<Societe> tableView;
    
    @FXML
    private TableColumn<Societe, Integer> idColumn;
    
    @FXML
    private TableColumn<Societe, String> nomColumn;
    
    @FXML
    private TableColumn<Societe, String> secteurColumn;
    
    @FXML
    private TableColumn<Societe, String> descriptionColumn;
    
    @FXML
    private TableColumn<Societe, String> adresseColumn;
    
    @FXML
    private TableColumn<Societe, String> siteWebColumn;
    
    @FXML
    private TableColumn<Societe, String> userEmailColumn;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label lastUpdateLabel;
    
    private SocieteService societeService;
    private UserService userService;
    private ObservableList<Societe> societeList;

    @FXML
    public void initialize() {
        societeService = new SocieteService();
        userService = new UserService();
        
        setupTableColumns();
        loadSocietes();
        updateUI();
        
        tableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                updateButtonStates();
                updateSelectionStatus(newSelection);
            }
        );
        
        updateButtonStates();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom_societe"));
        secteurColumn.setCellValueFactory(new PropertyValueFactory<>("secteur"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        siteWebColumn.setCellValueFactory(new PropertyValueFactory<>("site_web"));
        
        userEmailColumn.setCellValueFactory(cellData -> {
            Societe societe = cellData.getValue();
            return societe.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> societe.getUser().getEmail()) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
    }

    private void loadSocietes() {
        try {
            statusLabel.setText("Chargement...");
            List<Societe> societes = societeService.afficher();
            societeList = FXCollections.observableArrayList(societes);
            tableView.setItems(societeList);
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les societes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        // Update count label
        int count = societeList.size();
        countLabel.setText("Total: " + count + " societe" + (count > 1 ? "s" : ""));
        
        // Update last update time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }
    
    private void updateSelectionStatus(Societe selected) {
        if (selected != null) {
            statusLabel.setText("Sélectionné: " + selected.getNom_societe());
        } else {
            updateUI();
        }
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-form.fxml"));
            Parent root = loader.load();
            
            SocieteFormController controller = loader.getController();
            controller.setMode(SocieteFormController.Mode.ADD);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Société");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadSocietes();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        Societe selectedSociete = tableView.getSelectionModel().getSelectedItem();
        if (selectedSociete == null) {
            showAlert("Avertissement", "Veuillez sélectionner une société à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-form.fxml"));
            Parent root = loader.load();
            
            SocieteFormController controller = loader.getController();
            controller.setMode(SocieteFormController.Mode.EDIT);
            controller.setSociete(selectedSociete);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier une Société");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadSocietes();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Societe selectedSociete = tableView.getSelectionModel().getSelectedItem();
        if (selectedSociete == null) {
            showAlert("Avertissement", "Veuillez sélectionner une société à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette société ?");
        confirmDialog.setContentText("Société: " + selectedSociete.getNom_societe());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                societeService.supprimer(selectedSociete.getId());
                loadSocietes();
                showAlert("Succès", "Société supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadSocietes();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadSocietes();
        } else {
            searchSocietes(searchText);
        }
    }

    private void searchSocietes(String searchText) {
        try {
            int searchId = Integer.parseInt(searchText);
            Societe societe = societeService.findById(searchId);
            if (societe != null) {
                societeList = FXCollections.observableArrayList(societe);
            } else {
                societeList = FXCollections.observableArrayList();
            }
        } catch (NumberFormatException e) {
            societeList = FXCollections.observableArrayList(societeService.findByNom(searchText));
        }
        tableView.setItems(societeList);
    }

    private void updateButtonStates() {
        boolean isSelected = tableView.getSelectionModel().getSelectedItem() != null;
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
