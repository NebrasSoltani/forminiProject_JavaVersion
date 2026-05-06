package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.services.FileUploadService;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.UserService;
import tn.formini.utils.SignupFieldValidation;
import tn.formini.utils.TunisiaGovernorates;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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
    @FXML private ComboBox<String> gouvernoratComboBox;
    @FXML private DatePicker dateNaissanceField;

    // Formateur fields
    @FXML private TextField specialiteTextField;
    @FXML private TextArea bioTextArea;
    @FXML private Spinner<Integer> experienceSpinner;
    @FXML private TextField linkedinTextField;
    @FXML private TextField portfolioTextField;
    @FXML private TextField cvTextField;
    @FXML private Button btnUploadCv;
    @FXML private Label lblCvFileName;
    @FXML private TextField noteTextField;
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

    // Alternate aliases kept for the merged code paths
    @FXML private Label heroSubLabel;
    @FXML private VBox passwordGroup;
    @FXML private VBox passwordConfirmGroup;
    @FXML private Label passwordHintLabel;
    @FXML private ComboBox<String> gouvernoratField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField photoField;
    @FXML private Label lblPhotoFileName;
    @FXML private Button btnUploadPhoto;
    @FXML private ImageView imageViewPhoto;
    @FXML private ComboBox<User> userComboBox;

    private FormateurService formateurService;
    private UserService userService;
    private FileUploadService fileUploadService;
    private File uploadedCvFile;
    private File uploadedPhotoFile;

    private Formateur formateur;
    private Mode mode;

    public enum Mode {
        ADD, EDIT
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formateurService = new FormateurService();
        userService = new UserService();
        fileUploadService = new FileUploadService();

        emailField = emailTextField;
        nomField = nomTextField;
        prenomField = prenomTextField;
        telephoneField = telephoneTextField;
        dateNaissancePicker = dateNaissanceField;
        spinnerExperience = experienceSpinner;

        gouvernoratField.setItems(TunisiaGovernorates.asObservableList());
        experienceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));
        experienceSpinner.setEditable(true);

        setupUserComboBox();
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        emailTextField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditEmail();
            }
        });
        telephoneTextField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditPhone();
            }
        });
        nomTextField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditNom();
            }
        });
        prenomTextField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditPrenom();
            }
        });
        dateNaissancePicker.valueProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditBirthDate();
            }
        });
        specialiteTextField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditSpecialite();
            }
        });
    }

    private void setupUserComboBox() {
        if (userComboBox == null) {
            return;
        }
        List<User> users = userService.afficher();
        userComboBox.setItems(javafx.collections.FXCollections.observableArrayList(users));
        userComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User u) {
                if (u == null) {
                    return "";
                }
                return (u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "")
                        + " <" + (u.getEmail() != null ? u.getEmail() : "") + ">";
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });
    }


    @FXML
    private void onUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");

        fileChooser.getExtensionFilters().addAll(imageFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(imageFilter);

        Stage stage = (Stage) btnUploadPhoto.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            uploadedPhotoFile = selectedFile;
            photoField.setText(selectedFile.getAbsolutePath());
            lblPhotoFileName.setText(selectedFile.getName());

            try {
                Image image = new Image(selectedFile.toURI().toString());
                imageViewPhoto.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
            }
        }
    }

    // onUploadCv defined later in file (single authoritative version)


    public void setMode(Mode mode) {
        this.mode = mode;
        updateFormForMode(mode);
        if (mode == Mode.ADD) {
            clearForm();
            setPasswordSectionVisible(true);
            if (heroSubLabel != null) {
                heroSubLabel.setText(
                    "Même présentation que l'inscription formateur : identité puis profil pro. Les champs * sont obligatoires pour la création.");
            }
        } else {
            passwordField.clear();
            passwordConfirmField.clear();
            setPasswordSectionVisible(false);
            if (heroSubLabel != null) {
                heroSubLabel.setText(
                    "Même formulaire qu'à l'ajout : vous pouvez modifier le profil et le compte, sauf le mot de passe (inchangé depuis cet écran).");
            }
        }
    }

    private void setPasswordSectionVisible(boolean visible) {
        if (passwordGroup != null) {
            passwordGroup.setVisible(visible);
            passwordGroup.setManaged(visible);
        }
        if (passwordConfirmGroup != null) {
            passwordConfirmGroup.setVisible(visible);
            passwordConfirmGroup.setManaged(visible);
        }
        if (passwordHintLabel != null) {
            passwordHintLabel.setVisible(visible);
            passwordHintLabel.setManaged(visible);
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
                gouvernoratField.setValue(user.getGouvernorat());
            }
            
            if (user.getDate_naissance() != null) {
                dateNaissancePicker.setValue(user.getDate_naissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            
            specialiteTextField.setText(formateur.getSpecialite() != null ? formateur.getSpecialite() : "");
            bioTextArea.setText(formateur.getBio() != null ? formateur.getBio() : "");

            int exp = formateur.getExperience_annees() != null ? formateur.getExperience_annees() : 0;
            experienceSpinner.getValueFactory().setValue(Math.min(70, Math.max(0, exp)));
            experienceSpinner.getEditor().setText(String.valueOf(exp));

            linkedinTextField.setText(formateur.getLinkedin() != null ? formateur.getLinkedin() : "");
            portfolioTextField.setText(formateur.getPortfolio() != null ? formateur.getPortfolio() : "");
            cvTextField.setText(formateur.getCv() != null ? formateur.getCv() : "");

            if (formateur.getCv() != null && !formateur.getCv().isEmpty()) {
                lblCvFileName.setText(stripToFileName(formateur.getCv()));
            }

            if (noteTextField != null) {
                noteTextField.setText(formateur.getNote_moyenne() != null ? formateur.getNote_moyenne().toString() : "");
            }

            if (userComboBox != null) {
                userComboBox.setValue(formateur.getUser());
            }
            
            photoField.setText(user.getPhoto() != null ? user.getPhoto() : "");
            if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                lblPhotoFileName.setText(stripToFileName(user.getPhoto()));
            }

            if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                try {
                    Image image = new Image(user.getPhoto());
                    imageViewPhoto.setImage(image);
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + e.getMessage());
                }
            }
        }
    }

    private static String stripToFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "Aucun fichier";
        }
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private void clearForm() {
        // Clear user fields
        emailTextField.clear();
        telephoneTextField.clear();
        passwordField.clear();
        passwordConfirmField.clear();
        nomTextField.clear();
        prenomTextField.clear();
        gouvernoratField.setValue(null);
        dateNaissancePicker.setValue(null);
        photoField.clear();
        lblPhotoFileName.setText("Aucune photo sélectionnée");
        imageViewPhoto.setImage(null);
        uploadedPhotoFile = null;

        // Clear formateur fields
        specialiteTextField.clear();
        bioTextArea.clear();
        experienceSpinner.getValueFactory().setValue(0);
        experienceSpinner.getEditor().setText("0");
        linkedinTextField.clear();
        portfolioTextField.clear();
        cvTextField.clear();
        lblCvFileName.setText("Aucun fichier sélectionné");
        uploadedCvFile = null;
        
        clearAllErrors();
        hideMessage();
        if (noteTextField != null) {
            noteTextField.clear();
        }
        if (userComboBox != null) {
            userComboBox.setValue(null);
        }
    }

    private int readExperienceYears() {
        try {
            String t = experienceSpinner.getEditor().getText();
            if (t == null || t.isBlank()) {
                Integer v = experienceSpinner.getValue();
                return v != null ? v : 0;
            }
            return Integer.parseInt(t.trim());
        } catch (NumberFormatException e) {
            return experienceSpinner.getValue() != null ? experienceSpinner.getValue() : 0;
        }
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        hideMessage();
        if (!validateForm()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
            return;
        }

        try {
            User userToUse;

            String email = emailTextField.getText().trim();
            String password = passwordField.getText();
            String phoneNorm = SignupFieldValidation.normalizePhone(telephoneTextField.getText());

            if (mode == Mode.EDIT && formateur != null && formateur.getUser() != null) {
                User dbUser = userService.getUserByEmail(formateur.getUser().getEmail());
                if (dbUser == null) {
                    dbUser = userService.findById(formateur.getUser().getId());
                }
                if (dbUser == null) {
                    showAlert("Erreur", "Utilisateur introuvable.", Alert.AlertType.ERROR);
                    return;
                }
                if (email.isEmpty()) {
                    showAlert("Erreur de validation", "L'email est obligatoire", Alert.AlertType.ERROR);
                    return;
                }
                if (userService.emailExists(email) && !email.equalsIgnoreCase(dbUser.getEmail())) {
                    showAlert("Erreur de validation", "Cet email existe déjà", Alert.AlertType.ERROR);
                    return;
                }
                dbUser.setEmail(email);
                dbUser.setNom(nomTextField.getText().trim());
                dbUser.setPrenom(prenomTextField.getText().trim());
                dbUser.setTelephone(phoneNorm);
                dbUser.setGouvernorat(gouvernoratField.getValue());
                LocalDate localDateEdit = dateNaissancePicker.getValue();
                if (localDateEdit != null) {
                    dbUser.setDate_naissance(java.sql.Date.valueOf(localDateEdit));
                }
                String photoPathEdit = photoField.getText().trim();
                if (uploadedPhotoFile != null) {
                    photoPathEdit = fileUploadService.uploadPhoto(uploadedPhotoFile);
                }
                dbUser.setPhoto(photoPathEdit.isEmpty() ? null : photoPathEdit);
                userService.modifier(dbUser);
                userToUse = dbUser;
            } else if (!email.isEmpty() && password != null && !password.isEmpty()) {
                if (userService.emailExists(email)) {
                    showAlert("Erreur de validation", "Cet email existe déjà", Alert.AlertType.ERROR);
                    return;
                }

                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setNom(nomTextField.getText().trim());
                newUser.setPrenom(prenomTextField.getText().trim());
                newUser.setTelephone(phoneNorm);
                newUser.setGouvernorat(gouvernoratField.getValue());
                newUser.setRole_utilisateur("formateur");
                newUser.setIs_email_verified(true);

                LocalDate localDate = dateNaissancePicker.getValue();
                if (localDate != null) {
                    newUser.setDate_naissance(java.sql.Date.valueOf(localDate));
                }

                String photoPath = photoField.getText().trim();
                if (uploadedPhotoFile != null) {
                    photoPath = fileUploadService.uploadPhoto(uploadedPhotoFile);
                }
                newUser.setPhoto(photoPath.isEmpty() ? null : photoPath);

                userService.ajouter(newUser);
                userToUse = newUser;
            } else if (userComboBox != null && userComboBox.getValue() != null) {
                userToUse = userComboBox.getValue();
            } else {
                showAlert("Erreur de validation",
                    "Veuillez remplir l'email et le mot de passe pour créer un compte, ou sélectionner un utilisateur.",
                    Alert.AlertType.ERROR);
                return;
            }

            if (mode == Mode.ADD) {
                formateur = new Formateur();
            }

            formateur.setSpecialite(specialiteTextField.getText().trim().isEmpty() ? null : specialiteTextField.getText().trim());
            formateur.setBio(bioTextArea.getText().trim().isEmpty() ? null : bioTextArea.getText().trim());
            formateur.setExperience_annees(experienceSpinner.getValue() == 0 ? null : experienceSpinner.getValue());
            formateur.setLinkedin(linkedinTextField.getText().trim().isEmpty() ? null : linkedinTextField.getText().trim());
            formateur.setPortfolio(portfolioTextField.getText().trim().isEmpty() ? null : portfolioTextField.getText().trim());

            String cvPath = cvTextField.getText().trim();
            if (uploadedCvFile != null) {
                cvPath = fileUploadService.uploadCv(uploadedCvFile);
            }
            formateur.setCv(cvPath.isEmpty() ? null : cvPath);

            if (noteTextField != null) {
                String noteText = noteTextField.getText().trim();
                if (!noteText.isEmpty()) {
                    try {
                        formateur.setNote_moyenne(Double.parseDouble(noteText));
                    } catch (NumberFormatException e) {
                        // Ignore invalid note
                    }
                }
            }

            formateur.setUser(userToUse);

            if (mode == Mode.ADD) {
                formateurService.ajouter(formateur);
                showMessage("Formateur ajouté avec succès.");
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
        if (errorLabel == null) return;
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #dc2626;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        if (errorLabel == null) return;
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

    private boolean validateEditForm() {
        clearEditErrors();
        boolean valid = true;
        valid &= validateEditEmail();
        valid &= validateEditPhone();
        valid &= validateEditNom();
        valid &= validateEditPrenom();
        valid &= validateEditBirthDate();
        valid &= validateEditSpecialite();

        int experienceYears = readExperienceYears();
        if (experienceYears < 0 || experienceYears > 70) {
            showAlert("Erreur de validation", "L'expérience doit être comprise entre 0 et 70 ans", Alert.AlertType.ERROR);
            return false;
        }

        if (noteTextField != null) {
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
        }

        return valid;
    }

    private boolean validateEditEmail() {
        if (!SignupFieldValidation.isValidEmail(emailTextField.getText().trim())) {
            showError(errorEmail, "Email invalide ou manquant.");
            return false;
        }
        hideError(errorEmail);
        return true;
    }

    private boolean validateEditPhone() {
        String phoneNorm = SignupFieldValidation.normalizePhone(telephoneTextField.getText());
        if (!SignupFieldValidation.isValidPhoneNormalized(phoneNorm)) {
            showError(errorTelephone, "Téléphone invalide (8-12 chiffres).");
            return false;
        }
        hideError(errorTelephone);
        return true;
    }

    private boolean validateEditNom() {
        if (!SignupFieldValidation.isValidNomPrenom(nomTextField.getText())) {
            showError(errorNom, "Le nom est obligatoire (min. 2 caractères).");
            return false;
        }
        hideError(errorNom);
        return true;
    }

    private boolean validateEditPrenom() {
        if (!SignupFieldValidation.isValidNomPrenom(prenomTextField.getText())) {
            showError(errorPrenom, "Le prénom est obligatoire (min. 2 caractères).");
            return false;
        }
        hideError(errorPrenom);
        return true;
    }

    private boolean validateEditBirthDate() {
        if (dateNaissancePicker.getValue() == null) {
            showError(errorDateNaissance, "La date de naissance est obligatoire.");
            return false;
        }
        hideError(errorDateNaissance);
        return true;
    }

    private boolean validateEditSpecialite() {
        if (specialiteTextField.getText().trim().isEmpty()) {
            showError(errorSpecialite, "La spécialité est obligatoire.");
            return false;
        }
        hideError(errorSpecialite);
        return true;
    }

    private void clearEditErrors() {
        hideError(errorEmail);
        hideError(errorTelephone);
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
}
