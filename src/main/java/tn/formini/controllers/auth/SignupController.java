package tn.formini.controllers.auth;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.regex.Pattern;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SignupService;
import tn.formini.services.FileUploadService;
import tn.formini.utils.TunisiaGovernorates;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    @FXML private Button btnTogglePassword;
    @FXML private Button btnTogglePasswordConfirm;
    @FXML private Button btnGoToLogin;
    @FXML private Button btnUploadCv;
    @FXML private Button btnUploadPhoto;
    @FXML private Label lblCvFileName;
    @FXML private Label lblPhotoFileName;
    @FXML private ImageView imageViewPhoto;
    @FXML private Label eyeIcon;
    @FXML private Label eyeSlashIcon;
    @FXML private Label eyeIconConfirm;
    @FXML private Label eyeSlashIconConfirm;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private ComboBox<String> fieldGouvernorat;
    @FXML private DatePicker fieldDateNaissance;
    @FXML private ComboBox<String> comboGenre;
    @FXML private ComboBox<String> comboEtatCivil;
    @FXML private TextField fieldObjectif;
    @FXML private TextField fieldDomaineInput;
    @FXML private Button btnAddDomaine;
    @FXML private HBox flowPaneDomaines;
    @FXML private TextField fieldSpecialite;
    @FXML private TextArea fieldBio;
    @FXML private Spinner<Integer> spinnerExperience;
    @FXML private TextField fieldLinkedin;
    @FXML private TextField fieldPortfolio;
    @FXML private TextField fieldCv;
    @FXML private TextField fieldPhoto;
    
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
    private final FileUploadService fileUploadService = new FileUploadService();
    private ToggleGroup roleGroup;
    private java.io.File uploadedCvFile;
    private java.io.File uploadedPhotoFile;
    private final List<String> domainesList = new ArrayList<>();

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
        fieldGouvernorat.setItems(TunisiaGovernorates.asObservableList());

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
            user.setGouvernorat(fieldGouvernorat.getValue());
            user.setDate_naissance(dateNaissance);

            // Handle photo upload
            String photoPath = null;
            if (uploadedPhotoFile != null) {
                photoPath = fileUploadService.uploadPhoto(uploadedPhotoFile);
            } else {
                String photoUrl = fieldPhoto.getText();
                if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                    photoPath = photoUrl.trim();
                }
            }
            user.setPhoto(photoPath);

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
                if (!domainesList.isEmpty()) {
                    a.setDomaines_interet(convertDomainesToJson(domainesList));
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
            redirectToLogin();
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
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            uploadedCvFile = selectedFile;
            lblCvFileName.setText(selectedFile.getName());
            fieldCv.setText(selectedFile.getAbsolutePath());

            // Optional: You could copy the file to a specific uploads directory
            // For now, we'll just store the reference
        }
    }

    @FXML
    private void onUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");

        // Set extension filters for images
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");

        fileChooser.getExtensionFilters().addAll(imageFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(imageFilter);

        // Show open dialog
        Stage stage = (Stage) btnUploadPhoto.getScene().getWindow();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            uploadedPhotoFile = selectedFile;
            lblPhotoFileName.setText(selectedFile.getName());
            fieldPhoto.setText(selectedFile.getAbsolutePath());

            // Load and display the image
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imageViewPhoto.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onAddDomaine() {
        String domaine = fieldDomaineInput.getText().trim();
        if (domaine != null && !domaine.isEmpty() && !domainesList.contains(domaine)) {
            domainesList.add(domaine);
            fieldDomaineInput.clear();
            displayDomainesTags();
        }
    }

    private void displayDomainesTags() {
        flowPaneDomaines.getChildren().clear();
        for (String domaine : domainesList) {
            HBox tag = createDomaineTag(domaine);
            flowPaneDomaines.getChildren().add(tag);
        }
    }

    private HBox createDomaineTag(String domaine) {
        HBox tag = new HBox();
        tag.getStyleClass().add("domaine-tag");
        tag.setSpacing(4);

        Label label = new Label(domaine);
        label.getStyleClass().add("domaine-tag-label");

        Button removeBtn = new Button("×");
        removeBtn.getStyleClass().add("domaine-tag-remove");
        removeBtn.setOnAction(e -> {
            domainesList.remove(domaine);
            displayDomainesTags();
        });

        tag.getChildren().addAll(label, removeBtn);
        return tag;
    }

    private String convertDomainesToJson(List<String> domaines) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < domaines.size(); i++) {
            json.append("\"").append(domaines.get(i)).append("\"");
            if (i < domaines.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    
    /**
     * Redirect to login interface after successful signup
     */
    private void redirectToLogin() {
        if (lblMessage.getScene() == null) {
            return;
        }
        try {
            URL resource = getClass().getResource("/fxml/auth/Login.fxml");
            if (resource != null) {
                Parent root = FXMLLoader.load(resource);
                lblMessage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGoToLogin() {
        redirectToLogin();
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
