package tn.formini.controllers.crud;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;

import java.util.List;

public class SocieteFormController {

    @FXML
    private TextField nomTextField;
    
    @FXML
    private TextField secteurTextField;
    
    @FXML
    private TextArea descriptionTextArea;
    
    @FXML
    private TextField adresseTextField;
    
    @FXML
    private TextField siteWebTextField;
    
    @FXML
    private ComboBox<User> userComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private SocieteService societeService;
    private UserService userService;
    
    private Societe societe;
    private Mode mode;
    
    public enum Mode {
        ADD, EDIT
    }

    @FXML
    public void initialize() {
        societeService = new SocieteService();
        userService = new UserService();
        
        setupUserComboBox();
    }

    private void setupUserComboBox() {
        List<User> users = userService.afficher();
        userComboBox.setItems(javafx.collections.FXCollections.observableArrayList(users));
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.ADD) {
            clearForm();
        }
    }

    public void setSociete(Societe societe) {
        this.societe = societe;
        populateForm();
    }

    private void populateForm() {
        if (societe != null) {
            nomTextField.setText(societe.getNom_societe() != null ? societe.getNom_societe() : "");
            secteurTextField.setText(societe.getSecteur() != null ? societe.getSecteur() : "");
            descriptionTextArea.setText(societe.getDescription() != null ? societe.getDescription() : "");
            adresseTextField.setText(societe.getAdresse() != null ? societe.getAdresse() : "");
            siteWebTextField.setText(societe.getSite_web() != null ? societe.getSite_web() : "");
            
            if (societe.getUser() != null) {
                userComboBox.setValue(societe.getUser());
            }
        }
    }

    private void clearForm() {
        nomTextField.clear();
        secteurTextField.clear();
        descriptionTextArea.clear();
        adresseTextField.clear();
        siteWebTextField.clear();
        userComboBox.setValue(null);
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            if (mode == Mode.ADD) {
                societe = new Societe();
            }

            societe.setNom_societe(nomTextField.getText().trim());
            societe.setSecteur(secteurTextField.getText().trim().isEmpty() ? null : secteurTextField.getText().trim());
            societe.setDescription(descriptionTextArea.getText().trim().isEmpty() ? null : descriptionTextArea.getText().trim());
            societe.setAdresse(adresseTextField.getText().trim().isEmpty() ? null : adresseTextField.getText().trim());
            
            String siteWebText = siteWebTextField.getText().trim();
            societe.setSite_web(siteWebText.isEmpty() ? null : siteWebText);
            
            societe.setUser(userComboBox.getValue());

            if (mode == Mode.ADD) {
                societeService.ajouter(societe);
                showAlert("Succès", "Société ajoutée avec succès", Alert.AlertType.INFORMATION);
            } else {
                societeService.modifier(societe);
                showAlert("Succès", "Société modifiée avec succès", Alert.AlertType.INFORMATION);
            }

            closeForm();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        closeForm();
    }

    private boolean validateForm() {
        if (nomTextField.getText().trim().isEmpty()) {
            showAlert("Erreur de validation", "Le nom de la société est obligatoire", Alert.AlertType.ERROR);
            return false;
        }

        if (userComboBox.getValue() == null) {
            showAlert("Erreur de validation", "Veuillez sélectionner un utilisateur", Alert.AlertType.ERROR);
            return false;
        }

        String siteWebText = siteWebTextField.getText().trim();
        if (!siteWebText.isEmpty() && !siteWebText.startsWith("http://") && !siteWebText.startsWith("https://")) {
            showAlert("Erreur de validation", "L'URL du site web doit commencer par http:// ou https://", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeForm() {
        cancelButton.getScene().getWindow().hide();
    }
}
