package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Domaine;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.DomaineService;
import tn.formini.services.UsersService.SignupService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ApprenantFormController implements Initializable {

    @FXML private Label lblMessage;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ComboBox<String> etatCivilComboBox;
    @FXML private TextArea objectifTextArea;
    @FXML private TextArea domainesInteretTextArea;
    @FXML private ComboBox<Domaine> domaineComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    // User creation fields
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelephone;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldPasswordConfirm;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnTogglePasswordConfirm;
    @FXML private Label eyeIcon;
    @FXML private Label eyeSlashIcon;
    @FXML private Label eyeIconConfirm;
    @FXML private Label eyeSlashIconConfirm;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private ComboBox<Gouvernorat> fieldGouvernorat;
    @FXML private DatePicker fieldDateNaissance;

    @FXML private Label errorGenre;
    @FXML private Label errorEtatCivil;
    @FXML private Label errorDomaine;
    @FXML private Label errorObjectif;
    @FXML private Label errorDomainesInteret;
    @FXML private Label errorEmail;
    @FXML private Label errorTelephone;
    @FXML private Label errorPassword;
    @FXML private Label errorPasswordConfirm;
    @FXML private Label errorNom;
    @FXML private Label errorPrenom;
    @FXML private Label errorDateNaissance;

    private ApprenantService apprenantService;
    private DomaineService domaineService;
    private SignupService signupService;

    private Apprenant apprenant;
    private Mode mode;

    public enum Mode {
        ADD, EDIT
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apprenantService = new ApprenantService();
        domaineService = new DomaineService();
        signupService = new SignupService();

        setupComboBoxes();
        setupValidationListeners();
    }

    private void setupComboBoxes() {
        genreComboBox.setItems(FXCollections.observableArrayList("homme", "femme", "autre"));
        etatCivilComboBox.setItems(FXCollections.observableArrayList("celibataire", "marie", "divorce", "veuf"));
        fieldGouvernorat.setItems(FXCollections.observableArrayList(Gouvernorat.values()));
        
        List<Domaine> domaines = domaineService.afficher();
        domaineComboBox.setItems(FXCollections.observableArrayList(domaines));
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        updateFormForMode(mode);
        if (mode == Mode.ADD) {
            clearForm();
        }
    }

    public void setApprenant(Apprenant apprenant) {
        this.apprenant = apprenant;
        populateForm();
    }

    private void updateFormForMode(Mode mode) {
        // Keep ADD and EDIT visual presentation identical.
        if (formTitle != null) {
            formTitle.setText("Ajouter un apprenant");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Créez un compte apprenant avec ses informations. Les champs marqués * sont obligatoires.");
        }
        fieldPassword.setPromptText("8+ caractères, maj., min., chiffre");
        fieldPasswordConfirm.setPromptText("Même mot de passe");
        if (saveButton != null) {
            saveButton.setText("Enregistrer");
        }
    }

    private void populateForm() {
        if (apprenant != null) {
            genreComboBox.setValue(apprenant.getGenre());
            etatCivilComboBox.setValue(apprenant.getEtat_civil());
            objectifTextArea.setText(apprenant.getObjectif() != null ? apprenant.getObjectif() : "");
            domainesInteretTextArea.setText(apprenant.getDomaines_interet() != null ? apprenant.getDomaines_interet() : "");
            
            if (apprenant.getUser() != null) {
                User user = apprenant.getUser();
                fieldEmail.setText(user.getEmail());
                fieldTelephone.setText(user.getTelephone());
                fieldNom.setText(user.getNom());
                fieldPrenom.setText(user.getPrenom());
                if (user.getGouvernorat() != null) {
                    Gouvernorat gouvernorat = Gouvernorat.fromDisplayName(user.getGouvernorat());
                    fieldGouvernorat.setValue(gouvernorat);
                }
                if (user.getDate_naissance() != null) {
                    fieldDateNaissance.setValue(user.getDate_naissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }
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
        domaineComboBox.setValue(null);
        
        // Clear user fields
        fieldEmail.clear();
        fieldTelephone.clear();
        fieldPassword.clear();
        fieldPasswordConfirm.clear();
        fieldNom.clear();
        fieldPrenom.clear();
        fieldGouvernorat.setValue(null);
        fieldDateNaissance.setValue(null);
        
        clearAllErrors();
        hideMessage();
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        hideMessage();
        if (!validateForm()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
            return;
        }

        try {
            if (mode == Mode.ADD) {
                // Create user first
                LocalDate birth = fieldDateNaissance.getValue();
                Date dateNaissance = Date.from(birth.atStartOfDay(ZoneId.systemDefault()).toInstant());

                User user = new User();
                user.setEmail(trimToNull(fieldEmail.getText()));
                user.setPassword(fieldPassword.getText());
                user.setNom(trimToNull(fieldNom.getText()));
                user.setPrenom(trimToNull(fieldPrenom.getText()));
                user.setTelephone(normalizePhone(fieldTelephone.getText()));
                Gouvernorat selectedGouvernorat = fieldGouvernorat.getValue();
                user.setGouvernorat(selectedGouvernorat != null ? selectedGouvernorat.getDisplayName() : null);
                user.setDate_naissance(dateNaissance);

                // Create apprenant
                apprenant = new Apprenant();
                apprenant.setUser(user);
                
                apprenant.setGenre(genreComboBox.getValue());
                apprenant.setEtat_civil(etatCivilComboBox.getValue());
                apprenant.setObjectif(objectifTextArea.getText().trim().isEmpty() ? null : objectifTextArea.getText().trim());
                apprenant.setDomaines_interet(domainesInteretTextArea.getText().trim().isEmpty() ? null : domainesInteretTextArea.getText().trim());
                apprenant.setDomaine(domaineComboBox.getValue());

                signupService.signupApprenant(apprenant);
                showMessage("Apprenant ajouté avec succès.");
            } else {
                // Update existing apprenant
                if (apprenant.getUser() != null) {
                    User user = apprenant.getUser();
                    user.setEmail(trimToNull(fieldEmail.getText()));
                    if (!fieldPassword.getText().isEmpty()) {
                        user.setPassword(fieldPassword.getText());
                    }
                    user.setNom(trimToNull(fieldNom.getText()));
                    user.setPrenom(trimToNull(fieldPrenom.getText()));
                    user.setTelephone(normalizePhone(fieldTelephone.getText()));
                    Gouvernorat selectedGouvernorat = fieldGouvernorat.getValue();
                    user.setGouvernorat(selectedGouvernorat != null ? selectedGouvernorat.getDisplayName() : null);
                    if (fieldDateNaissance.getValue() != null) {
                        Date dateNaissance = Date.from(fieldDateNaissance.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        user.setDate_naissance(dateNaissance);
                    }
                }
                
                apprenant.setGenre(genreComboBox.getValue());
                apprenant.setEtat_civil(etatCivilComboBox.getValue());
                apprenant.setObjectif(objectifTextArea.getText().trim().isEmpty() ? null : objectifTextArea.getText().trim());
                apprenant.setDomaines_interet(domainesInteretTextArea.getText().trim().isEmpty() ? null : domainesInteretTextArea.getText().trim());
                apprenant.setDomaine(domaineComboBox.getValue());

                apprenantService.modifier(apprenant);
                showMessage("Apprenant modifié avec succès.");
            }

            Platform.runLater(this::closeForm);
        } catch (Exception e) {
            showMessage("Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        closeForm();
    }

    private boolean validateForm() {
        clearAllErrors();
        boolean isValid = true;

        // Validate user fields - email and phone are always required, password only for ADD mode
        isValid &= validateEmail();
        isValid &= validateTelephone();
        
        if (mode == Mode.ADD) {
            isValid &= validatePassword();
            isValid &= validatePasswordConfirm();
        } else {
            // For EDIT mode, only validate password if it's provided
            String password = fieldPassword.getText();
            if (!password.isEmpty()) {
                isValid &= validatePassword();
                isValid &= validatePasswordConfirm();
            }
        }
        
        isValid &= validateNom();
        isValid &= validatePrenom();
        isValid &= validateDateNaissance();

        String domainesInteret = domainesInteretTextArea.getText().trim();
        if (!domainesInteret.isEmpty()) {
            if (!domainesInteret.startsWith("[") || !domainesInteret.endsWith("]")) {
                showError(errorDomainesInteret, "Les domaines d'intérêt doivent être au format JSON array (ex: [\"domaine1\", \"domaine2\"])" );
                isValid = false;
            }
        }

        if (objectifTextArea.getText() != null && objectifTextArea.getText().trim().length() > 2000) {
            showError(errorObjectif, "L'objectif ne doit pas dépasser 2000 caractères");
            isValid = false;
        }

        return isValid;
    }

    private void setupValidationListeners() {
        genreComboBox.valueProperty().addListener((obs, oldV, newV) -> hideError(errorGenre));
        etatCivilComboBox.valueProperty().addListener((obs, oldV, newV) -> hideError(errorEtatCivil));
        domaineComboBox.valueProperty().addListener((obs, oldV, newV) -> hideError(errorDomaine));
        objectifTextArea.textProperty().addListener((obs, oldV, newV) -> hideError(errorObjectif));
        domainesInteretTextArea.textProperty().addListener((obs, oldV, newV) -> hideError(errorDomainesInteret));
        
        // User field validation listeners
        fieldEmail.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        fieldTelephone.textProperty().addListener((obs, oldVal, newVal) -> validateTelephone());
        fieldPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword();
            if (!fieldPasswordConfirm.getText().isEmpty()) {
                validatePasswordConfirm();
            }
        });
        fieldPasswordConfirm.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordConfirm());
        fieldNom.textProperty().addListener((obs, oldVal, newVal) -> validateNom());
        fieldPrenom.textProperty().addListener((obs, oldVal, newVal) -> validatePrenom());
        fieldDateNaissance.valueProperty().addListener((obs, oldVal, newVal) -> validateDateNaissance());
    }

    private void showMessage(String text) {
        lblMessage.setText(text);
        if (!lblMessage.getStyleClass().contains("signup-alert")) {
            lblMessage.getStyleClass().add("signup-alert");
        }
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
        Platform.runLater(() -> {
            lblMessage.requestLayout();
            scrollToMessageIfNeeded();
        });
    }

    private void hideMessage() {
        lblMessage.setText("");
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    private void scrollToMessageIfNeeded() {
        javafx.scene.Parent parent = lblMessage.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane scrollPane) {
                scrollPane.setVvalue(0);
                return;
            }
            parent = parent.getParent();
        }
    }

    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #dc2626;");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void hideError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setStyle("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void clearAllErrors() {
        hideError(errorGenre);
        hideError(errorEtatCivil);
        hideError(errorDomaine);
        hideError(errorObjectif);
        hideError(errorDomainesInteret);
        hideError(errorEmail);
        hideError(errorTelephone);
        hideError(errorPassword);
        hideError(errorPasswordConfirm);
        hideError(errorNom);
        hideError(errorPrenom);
        hideError(errorDateNaissance);
    }

    private void closeForm() {
        if (cancelButton.getScene() != null && cancelButton.getScene().getWindow() != null) {
            cancelButton.getScene().getWindow().hide();
        }
    }
    
    // Password toggle functionality
    @FXML
    private void onTogglePassword() {
        togglePasswordField(fieldPassword, btnTogglePassword);
    }

    @FXML
    private void onTogglePasswordConfirm() {
        togglePasswordField(fieldPasswordConfirm, btnTogglePasswordConfirm);
    }

    private void togglePasswordField(javafx.scene.control.PasswordField passwordField, Button toggleButton) {
        HBox parent = (HBox) toggleButton.getParent();
        
        // Find current password field (either PasswordField or TextField)
        javafx.scene.control.TextInputControl currentField = null;
        int fieldIndex = -1;
        
        for (int i = 0; i < parent.getChildren().size(); i++) {
            javafx.scene.Node node = parent.getChildren().get(i);
            if ((node instanceof PasswordField || node instanceof TextField) && !node.equals(toggleButton)) {
                currentField = (javafx.scene.control.TextInputControl) node;
                fieldIndex = i;
                break;
            }
        }
        
        if (currentField == null) return;
        
        if (currentField instanceof PasswordField currentPasswordField) {
            // Show plain text while keeping bidirectional sync with injected field.
            TextField visiblePassword = new TextField();
            visiblePassword.setPromptText(currentPasswordField.getPromptText());
            visiblePassword.getStyleClass().addAll(currentPasswordField.getStyleClass());
            visiblePassword.setStyle(currentPasswordField.getStyle());
            visiblePassword.textProperty().bindBidirectional(currentPasswordField.textProperty());

            parent.getChildren().set(fieldIndex, visiblePassword);

            if (toggleButton == btnTogglePassword) {
                eyeIcon.setVisible(false);
                eyeIcon.setManaged(false);
                eyeSlashIcon.setVisible(true);
                eyeSlashIcon.setManaged(true);
            } else if (toggleButton == btnTogglePasswordConfirm) {
                eyeIconConfirm.setVisible(false);
                eyeIconConfirm.setManaged(false);
                eyeSlashIconConfirm.setVisible(true);
                eyeSlashIconConfirm.setManaged(true);
            }
        } else if (currentField instanceof TextField visiblePasswordField) {
            // Restore the original injected PasswordField to keep listeners/validation stable.
            PasswordField targetField = toggleButton == btnTogglePassword ? fieldPassword : fieldPasswordConfirm;
            if (targetField == null) {
                return;
            }

            visiblePasswordField.textProperty().unbindBidirectional(targetField.textProperty());
            targetField.setPromptText(visiblePasswordField.getPromptText());
            targetField.getStyleClass().setAll(visiblePasswordField.getStyleClass());
            targetField.setStyle(visiblePasswordField.getStyle());
            parent.getChildren().set(fieldIndex, targetField);

            if (toggleButton == btnTogglePassword) {
                eyeIcon.setVisible(true);
                eyeIcon.setManaged(true);
                eyeSlashIcon.setVisible(false);
                eyeSlashIcon.setManaged(false);
            } else if (toggleButton == btnTogglePasswordConfirm) {
                eyeIconConfirm.setVisible(true);
                eyeIconConfirm.setManaged(true);
                eyeSlashIconConfirm.setVisible(false);
                eyeSlashIconConfirm.setManaged(false);
            }
        }
    }
    
    // Validation methods
    private boolean validateEmail() {
        String email = fieldEmail.getText().trim();
        if (email.isEmpty()) {
            showError(errorEmail, "L'email est obligatoire");
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!Pattern.matches(emailRegex, email)) {
            showError(errorEmail, "Format d'email invalide");
            return false;
        }
        
        hideError(errorEmail);
        return true;
    }
    
    private boolean validateTelephone() {
        String telephone = fieldTelephone.getText().trim();
        if (telephone.isEmpty()) {
            showError(errorTelephone, "Le téléphone est obligatoire");
            return false;
        }
        
        String normalized = normalizePhone(telephone);
        if (normalized == null || !normalized.matches("\\+?[0-9]{8,12}$")) {
            showError(errorTelephone, "Format invalide: 8-12 chiffres");
            return false;
        }
        
        hideError(errorTelephone);
        return true;
    }
    
    private boolean validatePassword() {
        String password = fieldPassword.getText();
        
        // For EDIT mode, password is optional
        if (mode == Mode.EDIT && password.isEmpty()) {
            hideError(errorPassword);
            return true;
        }
        
        // For ADD mode, password is required
        if (password.isEmpty()) {
            showError(errorPassword, "Le mot de passe est obligatoire");
            return false;
        }
        
        if (password.length() < 8) {
            showError(errorPassword, "Minimum 8 caractères");
            return false;
        }
        
        if (!password.matches(".*[A-Z].*")) {
            showError(errorPassword, "Une majuscule requise");
            return false;
        }
        
        if (!password.matches(".*[a-z].*")) {
            showError(errorPassword, "Une minuscule requise");
            return false;
        }
        
        if (!password.matches(".*\\d.*")) {
            showError(errorPassword, "Un chiffre requis");
            return false;
        }
        
        hideError(errorPassword);
        return true;
    }
    
    private boolean validatePasswordConfirm() {
        String password = fieldPassword.getText();
        String passwordConfirm = fieldPasswordConfirm.getText();
        
        // For EDIT mode, password confirmation is optional if password is empty
        if (mode == Mode.EDIT && password.isEmpty() && passwordConfirm.isEmpty()) {
            hideError(errorPasswordConfirm);
            return true;
        }
        
        // If password is provided, confirmation is required
        if (passwordConfirm.isEmpty()) {
            showError(errorPasswordConfirm, "La confirmation est obligatoire");
            return false;
        }
        
        if (!password.equals(passwordConfirm)) {
            showError(errorPasswordConfirm, "Les mots de passe ne correspondent pas");
            return false;
        }
        
        hideError(errorPasswordConfirm);
        return true;
    }
    
    private boolean validateNom() {
        String nom = fieldNom.getText().trim();
        if (nom.isEmpty()) {
            showError(errorNom, "Le nom est obligatoire");
            return false;
        }
        
        if (nom.length() < 2) {
            showError(errorNom, "Minimum 2 caractères");
            return false;
        }
        
        hideError(errorNom);
        return true;
    }
    
    private boolean validatePrenom() {
        String prenom = fieldPrenom.getText().trim();
        if (prenom.isEmpty()) {
            showError(errorPrenom, "Le prénom est obligatoire");
            return false;
        }
        
        if (prenom.length() < 2) {
            showError(errorPrenom, "Minimum 2 caractères");
            return false;
        }
        
        hideError(errorPrenom);
        return true;
    }
    
    private boolean validateDateNaissance() {
        LocalDate date = fieldDateNaissance.getValue();
        if (date == null) {
            showError(errorDateNaissance, "La date de naissance est obligatoire");
            return false;
        }
        
        if (date.isAfter(LocalDate.now())) {
            showError(errorDateNaissance, "Date invalide");
            return false;
        }
        
        if (date.isBefore(LocalDate.now().minusYears(120))) {
            showError(errorDateNaissance, "Date invalide");
            return false;
        }
        
        hideError(errorDateNaissance);
        return true;
    }
    
    // Utility methods
    private static String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        boolean plus = t.startsWith("+");
        String digits = t.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return plus ? "+" : "";
        }
        return plus ? "+" + digits : digits;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
