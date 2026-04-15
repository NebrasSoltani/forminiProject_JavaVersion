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
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FormateurCrudController {

    @FXML
    private TableView<Formateur> tableView;
    
    @FXML
    private TableColumn<Formateur, Integer> idColumn;
    
    @FXML
    private TableColumn<Formateur, String> specialiteColumn;
    
    @FXML
    private TableColumn<Formateur, String> bioColumn;
    
    @FXML
    private TableColumn<Formateur, Integer> experienceColumn;
    
    @FXML
    private TableColumn<Formateur, String> linkedinColumn;
    
    @FXML
    private TableColumn<Formateur, Double> noteColumn;
    
    @FXML
    private TableColumn<Formateur, String> userEmailColumn;
    
    @FXML
    private TableColumn<Formateur, String> userNomColumn;
    
    @FXML
    private TableColumn<Formateur, String> portfolioColumn;
    
        
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
    
    private FormateurService formateurService;
    private UserService userService;
    private ObservableList<Formateur> formateurList;

    @FXML
    public void initialize() {
        formateurService = new FormateurService();
        userService = new UserService();
        
        setupTableColumns();
        loadFormateurs();
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
        specialiteColumn.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        bioColumn.setCellValueFactory(new PropertyValueFactory<>("bio"));
        experienceColumn.setCellValueFactory(new PropertyValueFactory<>("experience_annees"));
        linkedinColumn.setCellValueFactory(new PropertyValueFactory<>("linkedin"));
        portfolioColumn.setCellValueFactory(new PropertyValueFactory<>("portfolio"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note_moyenne"));
        
        userEmailColumn.setCellValueFactory(cellData -> {
            Formateur formateur = cellData.getValue();
            return formateur.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> formateur.getUser().getEmail()) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        
        userNomColumn.setCellValueFactory(cellData -> {
            Formateur formateur = cellData.getValue();
            return formateur.getUser() != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> 
                    formateur.getUser().getPrenom() + " " + formateur.getUser().getNom()
                ) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
    }

    private void loadFormateurs() {
        try {
            statusLabel.setText("Chargement...");
            List<Formateur> formateurs = formateurService.afficher();
            formateurList = FXCollections.observableArrayList(formateurs);
            tableView.setItems(formateurList);
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les formateurs: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        // Update count label
        int count = formateurList.size();
        countLabel.setText("Total: " + count + " formateur" + (count > 1 ? "s" : ""));
        
        // Update last update time
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }
    
    private void updateSelectionStatus(Formateur selected) {
        if (selected != null) {
            statusLabel.setText("Sélectionné: " + selected.getUser().getPrenom() + " " + selected.getUser().getNom());
        } else {
            updateUI();
        }
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/formateur-form.fxml"));
            Parent root = loader.load();
            
            FormateurFormController controller = loader.getController();
            controller.setMode(FormateurFormController.Mode.ADD);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Formateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadFormateurs();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        Formateur selectedFormateur = tableView.getSelectionModel().getSelectedItem();
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/formateur-form.fxml"));
            Parent root = loader.load();
            
            FormateurFormController controller = loader.getController();
            controller.setMode(FormateurFormController.Mode.EDIT);
            controller.setFormateur(selectedFormateur);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier un Formateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadFormateurs();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Formateur selectedFormateur = tableView.getSelectionModel().getSelectedItem();
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer ce formateur ?");
        confirmDialog.setContentText("Formateur ID: " + selectedFormateur.getId());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                formateurService.supprimer(selectedFormateur.getId());
                loadFormateurs();
                showAlert("Succès", "Formateur supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadFormateurs();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadFormateurs();
        } else {
            searchFormateurs(searchText);
        }
    }

    private void searchFormateurs(String searchText) {
        try {
            int searchId = Integer.parseInt(searchText);
            Formateur formateur = formateurService.findById(searchId);
            if (formateur != null) {
                formateurList = FXCollections.observableArrayList(formateur);
            } else {
                formateurList = FXCollections.observableArrayList();
            }
        } catch (NumberFormatException e) {
            formateurList = FXCollections.observableArrayList(formateurService.findBySpecialite(searchText));
        }
        tableView.setItems(formateurList);
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
