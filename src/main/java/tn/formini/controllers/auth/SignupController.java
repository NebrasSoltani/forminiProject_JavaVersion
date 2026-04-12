package tn.formini.controllers.auth;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.regex.Pattern;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SignupService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class SignupController implements Initializable {

    /** Quand défini (ex. depuis FrontMain), retourne au shell parent au lieu de remplacer la scène. */
    private Runnable onBack;

    @FXML private Label lblMessage;
    @FXML private RadioButton rbApprenant;
    @FXML private RadioButton rbFormateur;
    @FXML private VBox panelApprenant;
    @FXML private VBox panelFormateur;
    @FXML private VBox roleTileApprenant;
    @FXML private VBox roleTileFormateur;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelephone;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldPasswordConfirm;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldGouvernorat;
    @FXML private DatePicker fieldDateNaissance;
    @FXML private ComboBox<String> comboGenre;
    @FXML private ComboBox<String> comboEtatCivil;
    @FXML private TextField fieldObjectif;
    @FXML private TextField fieldDomainesInteret;
    @FXML private TextField fieldSpecialite;
    @FXML private TextArea fieldBio;
    @FXML private Spinner<Integer> spinnerExperience;
    @FXML private TextField fieldLinkedin;
    @FXML private TextField fieldPortfolio;
    @FXML private TextField fieldCv;
    
    // Error labels
    @FXML private Label errorEmail;
    @FXML private Label errorTelephone;
    @FXML private Label errorPassword;
    @FXML private Label errorPasswordConfirm;
    @FXML private Label errorNom;
    @FXML private Label errorPrenom;
    @FXML private Label errorDateNaissance;
    @FXML private Label errorSpecialite;

    private final SignupService signupService = new SignupService();
    private ToggleGroup roleGroup;

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleGroup = new ToggleGroup();
        rbApprenant.setToggleGroup(roleGroup);
        rbFormateur.setToggleGroup(roleGroup);

        comboGenre.getItems().addAll("homme", "femme", "autre");
        comboEtatCivil.getItems().addAll("celibataire", "marie", "divorce", "veuf");

        spinnerExperience.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));

        roleGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            updateRolePanels();
            updateRoleTileStyles();
        });

        roleTileApprenant.setOnMouseClicked(e -> rbApprenant.setSelected(true));
        roleTileFormateur.setOnMouseClicked(e -> rbFormateur.setSelected(true));

        updateRolePanels();
        updateRoleTileStyles();
        setupValidationListeners();
    }

    private void updateRolePanels() {
        boolean apprenant = rbApprenant.isSelected();
        panelApprenant.setVisible(apprenant);
        panelApprenant.setManaged(apprenant);
        panelFormateur.setVisible(!apprenant);
        panelFormateur.setManaged(!apprenant);
    }

    private void updateRoleTileStyles() {
        if (roleTileApprenant == null || roleTileFormateur == null) {
            return;
        }
        boolean apprenant = rbApprenant.isSelected();
        roleTileApprenant.getStyleClass().remove("signup-role-tile-selected");
        roleTileFormateur.getStyleClass().remove("signup-role-tile-selected");
        if (apprenant) {
            roleTileApprenant.getStyleClass().add("signup-role-tile-selected");
        } else {
            roleTileFormateur.getStyleClass().add("signup-role-tile-selected");
        }
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
            LocalDate birth = fieldDateNaissance.getValue();
            Date dateNaissance = Date.from(birth.atStartOfDay(ZoneId.systemDefault()).toInstant());

            User user = new User();
            user.setEmail(trimToNull(fieldEmail.getText()));
            user.setPassword(fieldPassword.getText());
            user.setNom(trimToNull(fieldNom.getText()));
            user.setPrenom(trimToNull(fieldPrenom.getText()));
            user.setTelephone(normalizePhone(fieldTelephone.getText()));
            String gouv = fieldGouvernorat.getText();
            user.setGouvernorat(gouv != null && !gouv.isBlank() ? gouv.trim() : null);
            user.setDate_naissance(dateNaissance);

            if (rbApprenant.isSelected()) {
                Apprenant a = new Apprenant();
                a.setUser(user);
                if (comboGenre.getValue() != null && !comboGenre.getValue().isBlank()) {
                    a.setGenre(comboGenre.getValue());
                }
                if (comboEtatCivil.getValue() != null && !comboEtatCivil.getValue().isBlank()) {
                    a.setEtat_civil(comboEtatCivil.getValue());
                }
                String obj = fieldObjectif.getText();
                a.setObjectif(obj != null && !obj.isBlank() ? obj.trim() : null);
                String dom = fieldDomainesInteret.getText();
                if (dom != null && !dom.isBlank()) {
                    a.setDomaines_interet(dom.trim());
                }
                signupService.signupApprenant(a);
            } else {
                Formateur f = new Formateur();
                f.setUser(user);
                f.setSpecialite(fieldSpecialite.getText() != null ? fieldSpecialite.getText().trim() : "");
                String bio = fieldBio.getText();
                f.setBio(bio != null && !bio.isBlank() ? bio.trim() : null);
                int exp = spinnerExperience.getValue() != null ? spinnerExperience.getValue() : 0;
                f.setExperience_annees(exp > 0 ? exp : null);
                String li = fieldLinkedin.getText();
                f.setLinkedin(li != null && !li.isBlank() ? li.trim() : null);
                String po = fieldPortfolio.getText();
                f.setPortfolio(po != null && !po.isBlank() ? po.trim() : null);
                String cv = fieldCv.getText();
                f.setCv(cv != null && !cv.isBlank() ? cv.trim() : null);
                signupService.signupFormateur(f);
            }

            new Alert(Alert.AlertType.INFORMATION, "Inscription réussie. Vous pouvez vous connecter.", ButtonType.OK).showAndWait();
            goBack();
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage() != null ? ex.getMessage() : "Données invalides.");
        } catch (IllegalStateException ex) {
            showMessage(ex.getMessage() != null ? ex.getMessage() : "Impossible de finaliser l'inscription.");
        }
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

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @FXML
    private void goBack() {
        if (onBack != null) {
            onBack.run();
            return;
        }
        if (lblMessage.getScene() == null) {
            return;
        }
        try {
            URL resource = getClass().getResource("/fxml/frontend/FrontMain.fxml");
            if (resource != null) {
                Parent root = FXMLLoader.load(resource);
                lblMessage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Parent p = lblMessage.getParent();
        while (p != null) {
            if (p instanceof ScrollPane sp) {
                sp.setVvalue(0);
                return;
            }
            p = p.getParent();
        }
    }

    // Validation methods
    private void setupValidationListeners() {
        // Email validation
        fieldEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail();
        });
        
        // Telephone validation
        fieldTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
            validateTelephone();
        });
        
        // Password validation
        fieldPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword();
            if (!fieldPasswordConfirm.getText().isEmpty()) {
                validatePasswordConfirm();
            }
        });
        
        // Password confirmation validation
        fieldPasswordConfirm.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordConfirm();
        });
        
        // Name validations
        fieldNom.textProperty().addListener((obs, oldVal, newVal) -> {
            validateNom();
        });
        
        fieldPrenom.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePrenom();
        });
        
        // Date validation
        fieldDateNaissance.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateNaissance();
        });
        
        // Specialite validation (for formateur)
        fieldSpecialite.textProperty().addListener((obs, oldVal, newVal) -> {
            if (rbFormateur.isSelected()) {
                validateSpecialite();
            }
        });
    }
    
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
    
    private boolean validateSpecialite() {
        String specialite = fieldSpecialite.getText().trim();
        if (specialite.isEmpty()) {
            showError(errorSpecialite, "La spécialité est obligatoire pour le formateur");
            return false;
        }
        
        if (specialite.length() < 3) {
            showError(errorSpecialite, "Minimum 3 caractères");
            return false;
        }
        
        hideError(errorSpecialite);
        return true;
    }
    
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void hideError(Label errorLabel) {
        errorLabel.setText("");
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
    }
    
    private boolean validateAllFields() {
        clearAllErrors();
        
        boolean isValid = true;
        
        isValid &= validateEmail();
        isValid &= validateTelephone();
        isValid &= validatePassword();
        isValid &= validatePasswordConfirm();
        isValid &= validateNom();
        isValid &= validatePrenom();
        isValid &= validateDateNaissance();
        
        if (rbFormateur.isSelected()) {
            isValid &= validateSpecialite();
        }
        
        return isValid;
    }
}
