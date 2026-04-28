package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.UserService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class FormateurFormController implements Initializable {

    @FXML private Label lblMessage;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    
    // User fields
    @FXML private TextField emailTextField;
    @FXML private TextField telephoneTextField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordConfirmField;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnTogglePasswordConfirm;
    @FXML private Label eyeIcon;
    @FXML private Label eyeSlashIcon;
    @FXML private Label eyeIconConfirm;
    @FXML private Label eyeSlashIconConfirm;
    @FXML private TextField nomTextField;
    @FXML private TextField prenomTextField;
    @FXML private ComboBox<Gouvernorat> gouvernoratComboBox;
    @FXML private DatePicker dateNaissancePicker;
    
    // Formateur fields
    @FXML private TextField specialiteTextField;
    @FXML private TextArea bioTextArea;
    @FXML private Spinner<Integer> experienceSpinner;
    @FXML private TextField linkedinTextField;
    @FXML private TextField portfolioTextField;
    @FXML private TextField cvTextField;
    @FXML private Button btnUploadCv;
    @FXML private Label lblCvFileName;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Error labels
    @FXML private Label errorEmail;
    @FXML private Label errorTelephone;
    @FXML private Label errorPassword;
    @FXML private Label errorPasswordConfirm;
    @FXML private Label errorNom;
    @FXML private Label errorPrenom;
    @FXML private Label errorDateNaissance;
    @FXML private Label errorSpecialite;
    @FXML private Label errorExperience;
    @FXML private Label errorLinkedin;
    @FXML private Label errorPortfolio;
    @FXML private Label errorCv;
    @FXML private Label errorBio;

    private FormateurService formateurService;
    private UserService userService;

    private Formateur formateur;
    private Mode mode;
    private File uploadedCvFile;

    public enum Mode {
        ADD, EDIT
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formateurService = new FormateurService();
        userService = new UserService();

        gouvernoratComboBox.getItems().addAll(Gouvernorat.values());
        setupExperienceSpinner();
        setupValidationListeners();
    }

    private void setupExperienceSpinner() {
        experienceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));
    }


    public void setMode(Mode mode) {
        this.mode = mode;
        updateFormForMode(mode);
        if (mode == Mode.ADD) {
            clearForm();
        }
    }

    private void updateFormForMode(Mode mode) {
        // Keep ADD and EDIT visual presentation identical.
        if (formTitle != null) {
            formTitle.setText("Ajouter un formateur");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Complétez les informations du formateur. Les champs marqués * sont obligatoires.");
        }
        passwordField.setPromptText("8+ caractères, maj., min., chiffre");
        passwordConfirmField.setPromptText("Même mot de passe");
        if (saveButton != null) {
            saveButton.setText("Créer le compte");
        }
    }

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
        populateForm();
    }

    private void populateForm() {
        if (formateur != null && formateur.getUser() != null) {
            User user = formateur.getUser();
            emailTextField.setText(user.getEmail() != null ? user.getEmail() : "");
            telephoneTextField.setText(user.getTelephone() != null ? user.getTelephone() : "");
            nomTextField.setText(user.getNom() != null ? user.getNom() : "");
            prenomTextField.setText(user.getPrenom() != null ? user.getPrenom() : "");
            if (user.getGouvernorat() != null) {
                Gouvernorat gouvernorat = Gouvernorat.fromDisplayName(user.getGouvernorat());
                gouvernoratComboBox.setValue(gouvernorat);
            }
            
            if (user.getDate_naissance() != null) {
                dateNaissancePicker.setValue(user.getDate_naissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            
            specialiteTextField.setText(formateur.getSpecialite() != null ? formateur.getSpecialite() : "");
            bioTextArea.setText(formateur.getBio() != null ? formateur.getBio() : "");
            
            if (formateur.getExperience_annees() != null) {
                experienceSpinner.getValueFactory().setValue(formateur.getExperience_annees());
            }
            
            linkedinTextField.setText(formateur.getLinkedin() != null ? formateur.getLinkedin() : "");
            portfolioTextField.setText(formateur.getPortfolio() != null ? formateur.getPortfolio() : "");
            cvTextField.setText(formateur.getCv() != null ? formateur.getCv() : "");
        }
    }

    private void clearForm() {
        // Clear user fields
        emailTextField.clear();
        telephoneTextField.clear();
        passwordField.clear();
        passwordConfirmField.clear();
        nomTextField.clear();
        prenomTextField.clear();
        gouvernoratComboBox.setValue(null);
        dateNaissancePicker.setValue(null);
        
        // Clear formateur fields
        specialiteTextField.clear();
        bioTextArea.clear();
        experienceSpinner.getValueFactory().setValue(0);
        linkedinTextField.clear();
        portfolioTextField.clear();
        cvTextField.clear();
        lblCvFileName.setText("Aucun fichier sélectionné");
        uploadedCvFile = null;
        
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
            User user;
            
            if (mode == Mode.ADD) {
                // Create new user for ADD mode
                user = new User();
                user.setPassword(passwordField.getText());
                formateur = new Formateur();
            } else {
                // Use existing user for EDIT mode
                user = formateur.getUser();
                // Only update password if provided
                if (!passwordField.getText().isEmpty()) {
                    user.setPassword(passwordField.getText());
                }
            }
            
            user.setEmail(trimToNull(emailTextField.getText()));
            user.setNom(trimToNull(nomTextField.getText()));
            user.setPrenom(trimToNull(prenomTextField.getText()));
            user.setTelephone(normalizePhone(telephoneTextField.getText()));
            Gouvernorat selectedGouvernorat = gouvernoratComboBox.getValue();
            user.setGouvernorat(selectedGouvernorat != null ? selectedGouvernorat.getDisplayName() : null);
            
            LocalDate birth = dateNaissancePicker.getValue();
            if (birth != null) {
                user.setDate_naissance(Date.from(birth.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            formateur.setUser(user);
            formateur.setSpecialite(specialiteTextField.getText().trim());
            formateur.setBio(bioTextArea.getText().trim().isEmpty() ? null : bioTextArea.getText().trim());
            
            int exp = experienceSpinner.getValue() != null ? experienceSpinner.getValue() : 0;
            formateur.setExperience_annees(exp > 0 ? exp : null);
            
            String linkedinText = linkedinTextField.getText().trim();
            formateur.setLinkedin(linkedinText.isEmpty() ? null : linkedinText);
            
            String portfolioText = portfolioTextField.getText().trim();
            formateur.setPortfolio(portfolioText.isEmpty() ? null : portfolioText);
            
            String cvText = cvTextField.getText().trim();
            formateur.setCv(cvText.isEmpty() ? null : cvText);

            if (mode == Mode.ADD) {
                formateurService.ajouter(formateur);
                showMessage("Formateur créé avec succès.");
            } else {
                formateurService.modifier(formateur);
                showMessage("Formateur modifié avec succès.");
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
            String password = passwordField.getText();
            if (!password.isEmpty()) {
                isValid &= validatePassword();
                isValid &= validatePasswordConfirm();
            }
        }
        
        isValid &= validateNom();
        isValid &= validatePrenom();
        isValid &= validateDateNaissance();

        // Validate formateur fields
        if (specialiteTextField.getText().trim().isEmpty()) {
            showError(errorSpecialite, "La spécialité est obligatoire");
            isValid = false;
        }


        String linkedinText = linkedinTextField.getText().trim();
        if (!linkedinText.isEmpty() && !isValidHttpUrl(linkedinText)) {
            showError(errorLinkedin, "L'URL LinkedIn doit commencer par http:// ou https://");
            isValid = false;
        }

        String portfolioText = portfolioTextField.getText().trim();
        if (!portfolioText.isEmpty() && !isValidHttpUrl(portfolioText)) {
            showError(errorPortfolio, "L'URL portfolio doit commencer par http:// ou https://");
            isValid = false;
        }

        String cvText = cvTextField.getText().trim();
        if (!cvText.isEmpty() && !isValidHttpUrl(cvText)) {
            showError(errorCv, "L'URL du CV doit commencer par http:// ou https://");
            isValid = false;
        }

        if (bioTextArea.getText() != null && bioTextArea.getText().trim().length() > 2000) {
            showError(errorBio, "La bio ne doit pas dépasser 2000 caractères");
            isValid = false;
        }

        return isValid;
    }

    private void setupValidationListeners() {
        // Email validation
        emailTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail();
        });
        
        // Telephone validation
        telephoneTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateTelephone();
        });
        
        // Password validation
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword();
            if (!passwordConfirmField.getText().isEmpty()) {
                validatePasswordConfirm();
            }
        });
        
        // Password confirmation validation
        passwordConfirmField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordConfirm();
        });
        
        // Name validations
        nomTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateNom();
        });
        
        prenomTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePrenom();
        });
        
        // Date validation
        dateNaissancePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateNaissance();
        });
        
        // Formateur field validations
        specialiteTextField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.trim().isEmpty()) {
                hideError(errorSpecialite);
            }
        });
        linkedinTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorLinkedin));
        portfolioTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorPortfolio));
        cvTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorCv));
        bioTextArea.textProperty().addListener((obs, oldV, newV) -> hideError(errorBio));
    }

    private boolean isValidHttpUrl(String value) {
        String urlRegex = "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$";
        return Pattern.matches(urlRegex, value);
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
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #dc2626;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setStyle("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearAllErrors() {
        hideError(errorEmail);
        hideError(errorTelephone);
        hideError(errorPassword);
        hideError(errorPasswordConfirm);
        hideError(errorNom);
        hideError(errorPrenom);
        hideError(errorDateNaissance);
        hideError(errorSpecialite);
        hideError(errorExperience);
        hideError(errorLinkedin);
        hideError(errorPortfolio);
        hideError(errorCv);
        hideError(errorBio);
    }

    private void closeForm() {
        if (cancelButton.getScene() != null && cancelButton.getScene().getWindow() != null) {
            cancelButton.getScene().getWindow().hide();
        }
    }

    // Validation methods
    private boolean validateEmail() {
        String email = emailTextField.getText().trim();
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
        String telephone = telephoneTextField.getText().trim();
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
        String password = passwordField.getText();
        
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
        String password = passwordField.getText();
        String passwordConfirm = passwordConfirmField.getText();
        
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
        String nom = nomTextField.getText().trim();
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
        String prenom = prenomTextField.getText().trim();
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
        LocalDate date = dateNaissancePicker.getValue();
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
    /** Retire espaces / séparateurs ; conserve un + initial. Doit matcher ^\\+?[0-9]{8,12}$ après nettoyage. */
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

    @FXML
    private void onTogglePassword() {
        togglePasswordField(passwordField, btnTogglePassword);
    }

    @FXML
    private void onTogglePasswordConfirm() {
        togglePasswordField(passwordConfirmField, btnTogglePasswordConfirm);
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
            PasswordField targetField = toggleButton == btnTogglePassword ? passwordField : passwordConfirmField;
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

    @FXML
    private void onUploadCv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier CV");
        
        // Set extension filters
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf");
        FileChooser.ExtensionFilter docFilter = new FileChooser.ExtensionFilter("Documents Word", "*.doc", "*.docx");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");
        
        fileChooser.getExtensionFilters().addAll(pdfFilter, docFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(pdfFilter);
        
        // Show open dialog
        Stage stage = (Stage) btnUploadCv.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            uploadedCvFile = selectedFile;
            lblCvFileName.setText(selectedFile.getName());
            cvTextField.setText(selectedFile.getAbsolutePath());
        }
    }
}
