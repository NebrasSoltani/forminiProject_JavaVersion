package tn.formini.controllers.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Domaine;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.DomaineService;

import java.util.List;

public class ApprenantFormController {

    @FXML
    private ComboBox<String> genreComboBox;
    
    @FXML
    private ComboBox<String> etatCivilComboBox;
    
    @FXML
    private TextArea objectifTextArea;
    
    @FXML
    private TextArea domainesInteretTextArea;
    
    @FXML
    private ComboBox<User> userComboBox;
    
    @FXML
    private ComboBox<Domaine> domaineComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private ApprenantService apprenantService;
    private UserService userService;
    private DomaineService domaineService;
    
    private Apprenant apprenant;
    private Mode mode;
    
    public enum Mode {
        ADD, EDIT
    }

    @FXML
    public void initialize() {
        apprenantService = new ApprenantService();
        userService = new UserService();
        domaineService = new DomaineService();
        
        setupComboBoxes();
    }

    private void setupComboBoxes() {
        genreComboBox.setItems(FXCollections.observableArrayList("homme", "femme", "autre"));
        etatCivilComboBox.setItems(FXCollections.observableArrayList("celibataire", "marie", "divorce", "veuf"));
        
        List<User> users = userService.afficher();
        userComboBox.setItems(FXCollections.observableArrayList(users));
        
        List<Domaine> domaines = domaineService.afficher();
        domaineComboBox.setItems(FXCollections.observableArrayList(domaines));
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.ADD) {
            clearForm();
        }
    }

    public void setApprenant(Apprenant apprenant) {
        this.apprenant = apprenant;
        populateForm();
    }

    private void populateForm() {
        if (apprenant != null) {
            genreComboBox.setValue(apprenant.getGenre());
            etatCivilComboBox.setValue(apprenant.getEtat_civil());
            objectifTextArea.setText(apprenant.getObjectif() != null ? apprenant.getObjectif() : "");
            domainesInteretTextArea.setText(apprenant.getDomaines_interet() != null ? apprenant.getDomaines_interet() : "");
            
            if (apprenant.getUser() != null) {
                userComboBox.setValue(apprenant.getUser());
            }
            
            if (apprenant.getDomaine() != null) {
                domaineComboBox.setValue(apprenant.getDomaine());
            }
        }
    }

    private void clearForm() {
        genreComboBox.setValue(null);
        etatCivilComboBox.setValue(null);
        objectifTextArea.clear();
        domainesInteretTextArea.clear();
        userComboBox.setValue(null);
        domaineComboBox.setValue(null);
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            if (mode == Mode.ADD) {
                apprenant = new Apprenant();
            }

            apprenant.setGenre(genreComboBox.getValue());
            apprenant.setEtat_civil(etatCivilComboBox.getValue());
            apprenant.setObjectif(objectifTextArea.getText().trim().isEmpty() ? null : objectifTextArea.getText().trim());
            apprenant.setDomaines_interet(domainesInteretTextArea.getText().trim().isEmpty() ? null : domainesInteretTextArea.getText().trim());
            apprenant.setUser(userComboBox.getValue());
            apprenant.setDomaine(domaineComboBox.getValue());

            if (mode == Mode.ADD) {
                apprenantService.ajouter(apprenant);
                showAlert("Succès", "Apprenant ajouté avec succès", Alert.AlertType.INFORMATION);
            } else {
                apprenantService.modifier(apprenant);
                showAlert("Succès", "Apprenant modifié avec succès", Alert.AlertType.INFORMATION);
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
        if (userComboBox.getValue() == null) {
            showAlert("Erreur de validation", "Veuillez sélectionner un utilisateur", Alert.AlertType.ERROR);
            return false;
        }

        String domainesInteret = domainesInteretTextArea.getText().trim();
        if (!domainesInteret.isEmpty()) {
            if (!domainesInteret.startsWith("[") || !domainesInteret.endsWith("]")) {
                showAlert("Erreur de validation", "Les domaines d'intérêt doivent être au format JSON array (ex: [\"domaine1\", \"domaine2\"])", Alert.AlertType.ERROR);
                return false;
            }
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
