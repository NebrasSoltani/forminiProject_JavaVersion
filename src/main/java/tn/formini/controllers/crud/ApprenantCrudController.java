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
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Domaine;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.DomaineService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ApprenantCrudController {

    @FXML
    private TableView<Apprenant> tableView;
    
    @FXML
    private TableColumn<Apprenant, Integer> idColumn;
    
    @FXML
    private TableColumn<Apprenant, String> genreColumn;
    
    @FXML
    private TableColumn<Apprenant, String> etatCivilColumn;
    
    @FXML
    private TableColumn<Apprenant, String> objectifColumn;
    
    @FXML
    private TableColumn<Apprenant, String> userEmailColumn;
    
    @FXML
    private TableColumn<Apprenant, String> domainesColumn;
    
    @FXML
    private TableColumn<Apprenant, String> userNomColumn;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label lastUpdateLabel;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private TextField searchField;
    
    private ApprenantService apprenantService;
    private UserService userService;
    private DomaineService domaineService;
    private ObservableList<Apprenant> apprenantList;

    @FXML
    public void initialize() {
        apprenantService = new ApprenantService();
        userService = new UserService();
        domaineService = new DomaineService();
        
        apprenantList = FXCollections.observableArrayList();
        
        setupTableColumns();
        loadApprenants();
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
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        etatCivilColumn.setCellValueFactory(new PropertyValueFactory<>("etat_civil"));
        objectifColumn.setCellValueFactory(new PropertyValueFactory<>("objectif"));
        
        userEmailColumn.setCellValueFactory(cellData -> {
            Apprenant apprenant = cellData.getValue();
            return apprenant.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> apprenant.getUser().getEmail()) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        
        domainesColumn.setCellValueFactory(cellData -> {
            Apprenant apprenant = cellData.getValue();
            return apprenant.getDomaine() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> {
                    String domaines = apprenant.getDomaines_interet();
                    return domaines != null && !domaines.isEmpty() ? domaines : "N/A";
                }) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        
        userNomColumn.setCellValueFactory(cellData -> {
            Apprenant apprenant = cellData.getValue();
            return apprenant.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> 
                    apprenant.getUser().getPrenom() + " " + apprenant.getUser().getNom()
                ) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
    }

    private void loadApprenants() {
        try {
            statusLabel.setText("Chargement...");
            List<Apprenant> apprenants = apprenantService.afficher();
            apprenantList = FXCollections.observableArrayList(apprenants);
            tableView.setItems(apprenantList);
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les apprenants: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        // Update count label
        int count = apprenantList.size();
        countLabel.setText("Total: " + count + " apprenant" + (count > 1 ? "s" : ""));
        
        // Update last update time
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }
    
    private void updateSelectionStatus(Apprenant selected) {
        if (selected != null) {
            statusLabel.setText("Sélectionné: " + selected.getUser().getPrenom() + " " + selected.getUser().getNom());
        } else {
            updateUI();
        }
    }

    @FXML
    private void handleAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-form.fxml"));
            Parent root = loader.load();
            
            ApprenantFormController controller = loader.getController();
            controller.setMode(ApprenantFormController.Mode.ADD);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Apprenant");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadApprenants();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        Apprenant selectedApprenant = tableView.getSelectionModel().getSelectedItem();
        if (selectedApprenant == null) {
            showAlert("Avertissement", "Veuillez sélectionner un apprenant à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-form.fxml"));
            Parent root = loader.load();
            
            ApprenantFormController controller = loader.getController();
            controller.setMode(ApprenantFormController.Mode.EDIT);
            controller.setApprenant(selectedApprenant);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier un Apprenant");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadApprenants();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Apprenant selectedApprenant = tableView.getSelectionModel().getSelectedItem();
        if (selectedApprenant == null) {
            showAlert("Avertissement", "Veuillez sélectionner un apprenant à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cet apprenant ?");
        confirmDialog.setContentText("Apprenant ID: " + selectedApprenant.getId());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                apprenantService.supprimer(selectedApprenant.getId());
                loadApprenants();
                showAlert("Succès", "Apprenant supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadApprenants();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadApprenants();
        } else {
            searchApprenants(searchText);
        }
    }

    private void searchApprenants(String searchText) {
        try {
            int searchId = Integer.parseInt(searchText);
            Apprenant apprenant = apprenantService.findById(searchId);
            if (apprenant != null) {
                apprenantList = FXCollections.observableArrayList(apprenant);
            } else {
                apprenantList = FXCollections.observableArrayList();
            }
        } catch (NumberFormatException e) {
            apprenantList = FXCollections.observableArrayList();
        }
        tableView.setItems(apprenantList);
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
