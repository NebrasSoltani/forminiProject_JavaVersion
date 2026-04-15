package tn.formini.controllers.crud;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.UserService;

import java.util.List;

public class FormateurFormController {

    @FXML
    private TextField specialiteTextField;
    
    @FXML
    private TextArea bioTextArea;
    
    @FXML
    private TextField experienceTextField;
    
    @FXML
    private TextField linkedinTextField;
    
    @FXML
    private TextField portfolioTextField;
    
    @FXML
    private TextField cvTextField;
    
    @FXML
    private TextField noteTextField;
    
    @FXML
    private ComboBox<User> userComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private FormateurService formateurService;
    private UserService userService;
    
    private Formateur formateur;
    private Mode mode;
    
    public enum Mode {
        ADD, EDIT
    }

    @FXML
    public void initialize() {
        formateurService = new FormateurService();
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

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
        populateForm();
    }

    private void populateForm() {
        if (formateur != null) {
            specialiteTextField.setText(formateur.getSpecialite() != null ? formateur.getSpecialite() : "");
            bioTextArea.setText(formateur.getBio() != null ? formateur.getBio() : "");
            experienceTextField.setText(formateur.getExperience_annees() != null ? formateur.getExperience_annees().toString() : "");
            linkedinTextField.setText(formateur.getLinkedin() != null ? formateur.getLinkedin() : "");
            portfolioTextField.setText(formateur.getPortfolio() != null ? formateur.getPortfolio() : "");
            cvTextField.setText(formateur.getCv() != null ? formateur.getCv() : "");
            noteTextField.setText(formateur.getNote_moyenne() != null ? formateur.getNote_moyenne().toString() : "");
            
            if (formateur.getUser() != null) {
                userComboBox.setValue(formateur.getUser());
            }
        }
    }

    private void clearForm() {
        specialiteTextField.clear();
        bioTextArea.clear();
        experienceTextField.clear();
        linkedinTextField.clear();
        portfolioTextField.clear();
        cvTextField.clear();
        noteTextField.clear();
        userComboBox.setValue(null);
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            if (mode == Mode.ADD) {
                formateur = new Formateur();
            }

            formateur.setSpecialite(specialiteTextField.getText().trim());
            formateur.setBio(bioTextArea.getText().trim().isEmpty() ? null : bioTextArea.getText().trim());
            
            String experienceText = experienceTextField.getText().trim();
            if (!experienceText.isEmpty()) {
                formateur.setExperience_annees(Integer.parseInt(experienceText));
            } else {
                formateur.setExperience_annees(null);
            }
            
            String linkedinText = linkedinTextField.getText().trim();
            formateur.setLinkedin(linkedinText.isEmpty() ? null : linkedinText);
            
            String portfolioText = portfolioTextField.getText().trim();
            formateur.setPortfolio(portfolioText.isEmpty() ? null : portfolioText);
            
            String cvText = cvTextField.getText().trim();
            formateur.setCv(cvText.isEmpty() ? null : cvText);
            
            String noteText = noteTextField.getText().trim();
            if (!noteText.isEmpty()) {
                formateur.setNote_moyenne(Double.parseDouble(noteText));
            } else {
                formateur.setNote_moyenne(null);
            }
            
            formateur.setUser(userComboBox.getValue());

            if (mode == Mode.ADD) {
                formateurService.ajouter(formateur);
                showAlert("Succès", "Formateur ajouté avec succès", Alert.AlertType.INFORMATION);
            } else {
                formateurService.modifier(formateur);
                showAlert("Succès", "Formateur modifié avec succès", Alert.AlertType.INFORMATION);
            }

            closeForm();
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Veuillez entrer des valeurs numériques valides pour l'expérience et la note", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        closeForm();
    }

    private boolean validateForm() {
        if (specialiteTextField.getText().trim().isEmpty()) {
            showAlert("Erreur de validation", "La spécialité est obligatoire", Alert.AlertType.ERROR);
            return false;
        }

        if (userComboBox.getValue() == null) {
            showAlert("Erreur de validation", "Veuillez sélectionner un utilisateur", Alert.AlertType.ERROR);
            return false;
        }

        String experienceText = experienceTextField.getText().trim();
        if (!experienceText.isEmpty()) {
            try {
                int experience = Integer.parseInt(experienceText);
                if (experience < 0 || experience > 70) {
                    showAlert("Erreur de validation", "L'expérience doit être comprise entre 0 et 70 ans", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur de validation", "L'expérience doit être un nombre entier", Alert.AlertType.ERROR);
                return false;
            }
        }

        String noteText = noteTextField.getText().trim();
        if (!noteText.isEmpty()) {
            try {
                double note = Double.parseDouble(noteText);
                if (note < 0 || note > 5) {
                    showAlert("Erreur de validation", "La note doit être comprise entre 0 et 5", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur de validation", "La note doit être un nombre", Alert.AlertType.ERROR);
                return false;
            }
        }

        String linkedinText = linkedinTextField.getText().trim();
        if (!linkedinText.isEmpty() && !linkedinText.startsWith("http://") && !linkedinText.startsWith("https://")) {
            showAlert("Erreur de validation", "L'URL LinkedIn doit commencer par http:// ou https://", Alert.AlertType.ERROR);
            return false;
        }

        String portfolioText = portfolioTextField.getText().trim();
        if (!portfolioText.isEmpty() && !portfolioText.startsWith("http://") && !portfolioText.startsWith("https://")) {
            showAlert("Erreur de validation", "L'URL portfolio doit commencer par http:// ou https://", Alert.AlertType.ERROR);
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
