package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class SocieteFormController implements Initializable {

    /** Quand défini (ex. depuis MainController), retourne au shell parent au lieu de remplacer la scène. */
    private Runnable onBack;

    @FXML private Label lblMessage;
    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private TextField fieldNomSociete;
    @FXML private TextField fieldSecteur;
    @FXML private TextArea fieldDescription;
    @FXML private TextField fieldAdresse;
    @FXML private TextField fieldSiteWeb;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    // User creation fields
    @FXML private VBox panelNewUser;
    @FXML private TextField fieldUserEmail;
    @FXML private TextField fieldUserTelephone;
    @FXML private PasswordField fieldUserPassword;
    @FXML private PasswordField fieldUserPasswordConfirm;
    @FXML private Button btnToggleUserPassword;
    @FXML private Button btnToggleUserPasswordConfirm;
    @FXML private Label eyeIconUser;
    @FXML private Label eyeSlashIconUser;
    @FXML private Label eyeIconUserConfirm;
    @FXML private Label eyeSlashIconUserConfirm;
    @FXML private TextField fieldUserNom;
    @FXML private TextField fieldUserPrenom;
    @FXML private DatePicker fieldUserDateNaissance;
    @FXML private ComboBox<Gouvernorat> fieldUserGouvernorat;
    
    // Error labels
    @FXML private Label errorNomSociete;
    @FXML private Label errorSecteur;
    @FXML private Label errorDescription;
    @FXML private Label errorAdresse;
    @FXML private Label errorSiteWeb;
    @FXML private Label errorUserEmail;
    @FXML private Label errorUserTelephone;
    @FXML private Label errorUserPassword;
    @FXML private Label errorUserPasswordConfirm;
    @FXML private Label errorUserNom;
    @FXML private Label errorUserPrenom;
    @FXML private Label errorUserDateNaissance;

    private SocieteService societeService;
    private UserService userService;
    private Societe societe;
    private Mode mode;
    
    public enum Mode {
        ADD, EDIT
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        societeService = new SocieteService();
        userService = new UserService();
        
        fieldUserGouvernorat.getItems().addAll(Gouvernorat.values());
        setupValidationListeners();
        
        // Always show new user panel
        panelNewUser.setVisible(true);
        panelNewUser.setManaged(true);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        
        // Update header text based on mode
        if (mode == Mode.ADD) {
            lblTitle.setText("Ajouter une société");
            lblSubtitle.setText("Complétez les informations de la société. Les champs marqués * sont obligatoires.");
            clearForm();
        } else {
            lblTitle.setText("Modifier une société");
            lblSubtitle.setText("Modifiez les informations de la société. Les champs marqués * sont obligatoires. Le mot de passe est optionnel.");
        }
    }

    public void setSociete(Societe societe) {
        this.societe = societe;
        populateForm();
    }

    private void populateForm() {
        if (societe != null) {
            fieldNomSociete.setText(societe.getNom_societe() != null ? societe.getNom_societe() : "");
            fieldSecteur.setText(societe.getSecteur() != null ? societe.getSecteur() : "");
            fieldDescription.setText(societe.getDescription() != null ? societe.getDescription() : "");
            fieldAdresse.setText(societe.getAdresse() != null ? societe.getAdresse() : "");
            fieldSiteWeb.setText(societe.getSite_web() != null ? societe.getSite_web() : "");
            
            // Populate user fields if editing and user exists
            if (mode == Mode.EDIT && societe.getUser() != null) {
                User user = societe.getUser();
                fieldUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                fieldUserTelephone.setText(user.getTelephone() != null ? user.getTelephone() : "");
                fieldUserNom.setText(user.getNom() != null ? user.getNom() : "");
                fieldUserPrenom.setText(user.getPrenom() != null ? user.getPrenom() : "");
                
                if (user.getGouvernorat() != null) {
                    Gouvernorat gouvernorat = Gouvernorat.fromDisplayName(user.getGouvernorat());
                    fieldUserGouvernorat.setValue(gouvernorat);
                }
                
                if (user.getDate_naissance() != null) {
                    fieldUserDateNaissance.setValue(user.getDate_naissance().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
                }
                
                // Clear password fields in edit mode (optional update)
                fieldUserPassword.clear();
                fieldUserPasswordConfirm.clear();
            }
        }
    }

    private void clearForm() {
        fieldNomSociete.clear();
        fieldSecteur.clear();
        fieldDescription.clear();
        fieldAdresse.clear();
        fieldSiteWeb.clear();
        
        // Clear user fields
        fieldUserEmail.clear();
        fieldUserTelephone.clear();
        fieldUserPassword.clear();
        fieldUserPasswordConfirm.clear();
        fieldUserNom.clear();
        fieldUserPrenom.clear();
        fieldUserGouvernorat.setValue(null);
        fieldUserDateNaissance.setValue(null);
        
        clearAllErrors();
    }

    @FXML
    private void onSubmit() {
        hideMessage();

        // Validate all fields first
        if (!validateAllFields()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
            return;
        }

        try {
            if (mode == Mode.ADD) {
                societe = new Societe();
            }

            societe.setNom_societe(trimToNull(fieldNomSociete.getText()));
            societe.setSecteur(trimToNull(fieldSecteur.getText()));
            societe.setDescription(trimToNull(fieldDescription.getText()));
            societe.setAdresse(trimToNull(fieldAdresse.getText()));
            societe.setSite_web(trimToNull(fieldSiteWeb.getText()));
            
            // Create or update user
            User user = createOrUpdateUser();
            
            // Update password only if provided in EDIT mode
            if (mode == Mode.EDIT && !fieldUserPassword.getText().isEmpty()) {
                user.setPassword(fieldUserPassword.getText());
            }
            
            societe.setUser(user);

            // Validate entity
            societe.valider();

            if (mode == Mode.ADD) {
                societeService.ajouter(societe);
                showMessage("Société ajoutée avec succès.");
            } else {
                societeService.modifier(societe);
                showMessage("Société modifiée avec succès.");
            }

            // Close form after a short delay to show success message
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1500);
                    closeForm();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    closeForm();
                }
            });
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage() != null ? ex.getMessage() : "Données invalides.");
        } catch (IllegalStateException ex) {
            showMessage(ex.getMessage() != null ? ex.getMessage() : "Impossible de finaliser l'opération.");
        } catch (Exception ex) {
            showMessage("Erreur lors de l'enregistrement: " + ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeForm();
    }

    @FXML
    private void goBack() {
        if (onBack != null) {
            onBack.run();
            return;
        }
        closeForm();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
            if (parent instanceof ScrollPane) {
                ((ScrollPane) parent).setVvalue(0);
                return;
            }
            parent = parent.getParent();
        }
    }

    // Validation methods
    private void setupValidationListeners() {
        // Nom société validation
        fieldNomSociete.textProperty().addListener((obs, oldVal, newVal) -> {
            validateNomSociete();
        });
        
        // Secteur validation
        fieldSecteur.textProperty().addListener((obs, oldVal, newVal) -> {
            validateSecteur();
        });
        
        // Description validation
        fieldDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            validateDescription();
        });
        
        // Adresse validation
        fieldAdresse.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAdresse();
        });
        
        // Site web validation
        fieldSiteWeb.textProperty().addListener((obs, oldVal, newVal) -> {
            validateSiteWeb();
        });
        
        // New user validation
        fieldUserEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            validateUserEmail();
        });
        
        fieldUserTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
            validateUserTelephone();
        });
        
        fieldUserPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validateUserPassword();
            if (!fieldUserPasswordConfirm.getText().isEmpty()) {
                validateUserPasswordConfirm();
            }
        });
        
        fieldUserPasswordConfirm.textProperty().addListener((obs, oldVal, newVal) -> {
            validateUserPasswordConfirm();
        });
        
        fieldUserNom.textProperty().addListener((obs, oldVal, newVal) -> {
            validateUserNom();
        });
        
        fieldUserPrenom.textProperty().addListener((obs, oldVal, newVal) -> {
            validateUserPrenom();
        });
        
        fieldUserDateNaissance.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateUserDateNaissance();
        });
    }

    @FXML
    private void onToggleUserPassword() {
        togglePasswordField(fieldUserPassword, btnToggleUserPassword);
    }

    @FXML
    private void onToggleUserPasswordConfirm() {
        togglePasswordField(fieldUserPasswordConfirm, btnToggleUserPasswordConfirm);
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

            if (toggleButton == btnToggleUserPassword) {
                eyeIconUser.setVisible(false);
                eyeIconUser.setManaged(false);
                eyeSlashIconUser.setVisible(true);
                eyeSlashIconUser.setManaged(true);
            } else if (toggleButton == btnToggleUserPasswordConfirm) {
                eyeIconUserConfirm.setVisible(false);
                eyeIconUserConfirm.setManaged(false);
                eyeSlashIconUserConfirm.setVisible(true);
                eyeSlashIconUserConfirm.setManaged(true);
            }
        } else if (currentField instanceof TextField visiblePasswordField) {
            // Restore the original injected PasswordField to keep listeners/validation stable.
            PasswordField targetField = toggleButton == btnToggleUserPassword ? fieldUserPassword : fieldUserPasswordConfirm;
            if (targetField == null) {
                return;
            }

            visiblePasswordField.textProperty().unbindBidirectional(targetField.textProperty());
            targetField.setPromptText(visiblePasswordField.getPromptText());
            targetField.getStyleClass().setAll(visiblePasswordField.getStyleClass());
            targetField.setStyle(visiblePasswordField.getStyle());
            parent.getChildren().set(fieldIndex, targetField);

            if (toggleButton == btnToggleUserPassword) {
                eyeIconUser.setVisible(true);
                eyeIconUser.setManaged(true);
                eyeSlashIconUser.setVisible(false);
                eyeSlashIconUser.setManaged(false);
            } else if (toggleButton == btnToggleUserPasswordConfirm) {
                eyeIconUserConfirm.setVisible(true);
                eyeIconUserConfirm.setManaged(true);
                eyeSlashIconUserConfirm.setVisible(false);
                eyeSlashIconUserConfirm.setManaged(false);
            }
        }
    }
    
    private boolean validateNomSociete() {
        String nom = fieldNomSociete.getText().trim();
        if (nom.isEmpty()) {
            showError(errorNomSociete, "Le nom de la société est obligatoire");
            return false;
        }
        
        if (nom.length() < 2) {
            showError(errorNomSociete, "Minimum 2 caractères");
            return false;
        }
        
        if (nom.length() > 255) {
            showError(errorNomSociete, "Maximum 255 caractères");
            return false;
        }
        
        hideError(errorNomSociete);
        return true;
    }
    
    private boolean validateSecteur() {
        String secteur = fieldSecteur.getText().trim();
        if (!secteur.isEmpty() && secteur.length() > 100) {
            showError(errorSecteur, "Maximum 100 caractères");
            return false;
        }
        
        hideError(errorSecteur);
        return true;
    }
    
    private boolean validateDescription() {
        String description = fieldDescription.getText().trim();
        if (!description.isEmpty() && description.length() > 2000) {
            showError(errorDescription, "Maximum 2000 caractères");
            return false;
        }
        
        hideError(errorDescription);
        return true;
    }
    
    private boolean validateAdresse() {
        String adresse = fieldAdresse.getText().trim();
        if (!adresse.isEmpty() && adresse.length() > 500) {
            showError(errorAdresse, "Maximum 500 caractères");
            return false;
        }
        
        hideError(errorAdresse);
        return true;
    }
    
    private boolean validateSiteWeb() {
        String siteWeb = fieldSiteWeb.getText().trim();
        if (!siteWeb.isEmpty()) {
            if (!siteWeb.startsWith("http://") && !siteWeb.startsWith("https://")) {
                showError(errorSiteWeb, "Doit commencer par http:// ou https://");
                return false;
            }
            
            if (siteWeb.length() > 500) {
                showError(errorSiteWeb, "Maximum 500 caractères");
                return false;
            }
            
            // Basic URL validation
            String urlRegex = "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$";
            if (!Pattern.matches(urlRegex, siteWeb)) {
                showError(errorSiteWeb, "Format d'URL invalide");
                return false;
            }
        }
        
        hideError(errorSiteWeb);
        return true;
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
        hideError(errorNomSociete);
        hideError(errorSecteur);
        hideError(errorDescription);
        hideError(errorAdresse);
        hideError(errorSiteWeb);
        hideError(errorUserEmail);
        hideError(errorUserTelephone);
        hideError(errorUserPassword);
        hideError(errorUserPasswordConfirm);
        hideError(errorUserNom);
        hideError(errorUserPrenom);
        hideError(errorUserDateNaissance);
    }
    
    private boolean validateAllFields() {
        clearAllErrors();
        
        boolean isValid = true;
        
        // Validate societe fields
        isValid &= validateNomSociete();
        isValid &= validateSecteur();
        isValid &= validateDescription();
        isValid &= validateAdresse();
        isValid &= validateSiteWeb();
        
        // Validate user fields
        isValid &= validateUserEmail();
        isValid &= validateUserTelephone();
        isValid &= validateUserNom();
        isValid &= validateUserPrenom();
        isValid &= validateUserDateNaissance();
        
        // Password validation - only required in ADD mode or if password is provided in EDIT mode
        if (mode == Mode.ADD || !fieldUserPassword.getText().isEmpty() || !fieldUserPasswordConfirm.getText().isEmpty()) {
            isValid &= validateUserPassword();
            isValid &= validateUserPasswordConfirm();
        }
        
        return isValid;
    }
    
    private User createOrUpdateUser() {
        User user;
        
        if (mode == Mode.EDIT && societe.getUser() != null) {
            // Update existing user
            user = societe.getUser();
        } else {
            // Create new user
            user = new User();
            user.setPassword(fieldUserPassword.getText());
        }
        
        user.setEmail(trimToNull(fieldUserEmail.getText()));
        user.setNom(trimToNull(fieldUserNom.getText()));
        user.setPrenom(trimToNull(fieldUserPrenom.getText()));
        user.setTelephone(normalizePhone(fieldUserTelephone.getText()));
        
        Gouvernorat selectedGouvernorat = fieldUserGouvernorat.getValue();
        user.setGouvernorat(selectedGouvernorat != null ? selectedGouvernorat.getDisplayName() : null);
        
        LocalDate birth = fieldUserDateNaissance.getValue();
        if (birth != null) {
            Date dateNaissance = Date.from(birth.atStartOfDay(ZoneId.systemDefault()).toInstant());
            user.setDate_naissance(dateNaissance);
        }
        
        return user;
    }
    
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
    
    // User validation methods
    private boolean validateUserEmail() {
        String email = fieldUserEmail.getText().trim();
        if (email.isEmpty()) {
            showError(errorUserEmail, "L'email est obligatoire");
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!Pattern.matches(emailRegex, email)) {
            showError(errorUserEmail, "Format d'email invalide");
            return false;
        }
        
        hideError(errorUserEmail);
        return true;
    }
    
    private boolean validateUserTelephone() {
        String telephone = fieldUserTelephone.getText().trim();
        if (telephone.isEmpty()) {
            showError(errorUserTelephone, "Le téléphone est obligatoire");
            return false;
        }
        
        String normalized = normalizePhone(telephone);
        if (normalized == null || !normalized.matches("\\+?[0-9]{8,12}$")) {
            showError(errorUserTelephone, "Format invalide: 8-12 chiffres");
            return false;
        }
        
        hideError(errorUserTelephone);
        return true;
    }
    
    private boolean validateUserPassword() {
        String password = fieldUserPassword.getText();
        if (password.isEmpty()) {
            // In EDIT mode, password is optional
            if (mode == Mode.EDIT) {
                hideError(errorUserPassword);
                return true;
            } else {
                showError(errorUserPassword, "Le mot de passe est obligatoire");
                return false;
            }
        }
        
        if (password.length() < 8) {
            showError(errorUserPassword, "Minimum 8 caractères");
            return false;
        }
        
        if (!password.matches(".*[A-Z].*")) {
            showError(errorUserPassword, "Une majuscule requise");
            return false;
        }
        
        if (!password.matches(".*[a-z].*")) {
            showError(errorUserPassword, "Une minuscule requise");
            return false;
        }
        
        if (!password.matches(".*\\d.*")) {
            showError(errorUserPassword, "Un chiffre requis");
            return false;
        }
        
        hideError(errorUserPassword);
        return true;
    }
    
    private boolean validateUserPasswordConfirm() {
        String password = fieldUserPassword.getText();
        String passwordConfirm = fieldUserPasswordConfirm.getText();
        
        if (passwordConfirm.isEmpty()) {
            // In EDIT mode, password confirmation is optional if password is also empty
            if (mode == Mode.EDIT && password.isEmpty()) {
                hideError(errorUserPasswordConfirm);
                return true;
            } else {
                showError(errorUserPasswordConfirm, "La confirmation est obligatoire");
                return false;
            }
        }
        
        if (!password.equals(passwordConfirm)) {
            showError(errorUserPasswordConfirm, "Les mots de passe ne correspondent pas");
            return false;
        }
        
        hideError(errorUserPasswordConfirm);
        return true;
    }
    
    private boolean validateUserNom() {
        String nom = fieldUserNom.getText().trim();
        if (nom.isEmpty()) {
            showError(errorUserNom, "Le nom est obligatoire");
            return false;
        }
        
        if (nom.length() < 2) {
            showError(errorUserNom, "Minimum 2 caractères");
            return false;
        }
        
        hideError(errorUserNom);
        return true;
    }
    
    private boolean validateUserPrenom() {
        String prenom = fieldUserPrenom.getText().trim();
        if (prenom.isEmpty()) {
            showError(errorUserPrenom, "Le prénom est obligatoire");
            return false;
        }
        
        if (prenom.length() < 2) {
            showError(errorUserPrenom, "Minimum 2 caractères");
            return false;
        }
        
        hideError(errorUserPrenom);
        return true;
    }
    
    private boolean validateUserDateNaissance() {
        LocalDate date = fieldUserDateNaissance.getValue();
        if (date == null) {
            showError(errorUserDateNaissance, "La date de naissance est obligatoire");
            return false;
        }
        
        if (date.isAfter(LocalDate.now())) {
            showError(errorUserDateNaissance, "Date invalide");
            return false;
        }
        
        if (date.isBefore(LocalDate.now().minusYears(120))) {
            showError(errorUserDateNaissance, "Date invalide");
            return false;
        }
        
        hideError(errorUserDateNaissance);
        return true;
    }

    private void closeForm() {
        if (btnCancel.getScene() != null && btnCancel.getScene().getWindow() != null) {
            btnCancel.getScene().getWindow().hide();
        }
    }
}
