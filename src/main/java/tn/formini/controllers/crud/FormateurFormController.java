package tn.formini.controllers.crud;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import tn.formini.services.FileUploadService;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.UserService;
import tn.formini.utils.SignupFieldValidation;
import tn.formini.utils.TunisiaGovernorates;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class FormateurFormController {

    @FXML
    private Label heroSubLabel;

    @FXML
    private VBox passwordGroup;

    @FXML
    private VBox passwordConfirmGroup;

    @FXML
    private Label passwordHintLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordConfirmField;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private Button btnTogglePasswordConfirm;

    @FXML
    private Label eyeIcon;

    @FXML
    private Label eyeSlashIcon;

    @FXML
    private Label eyeIconConfirm;

    @FXML
    private Label eyeSlashIconConfirm;

    @FXML
    private Label errorEmail;

    @FXML
    private Label errorTelephone;

    @FXML
    private Label errorNom;

    @FXML
    private Label errorPrenom;

    @FXML
    private Label errorDateNaissance;

    @FXML
    private Label errorSpecialite;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField telephoneField;

    @FXML
    private ComboBox<String> gouvernoratField;

    @FXML
    private DatePicker dateNaissanceField;

    @FXML
    private TextField photoField;

    @FXML
    private Label lblPhotoFileName;

    @FXML
    private Button btnUploadPhoto;

    @FXML
    private ImageView imageViewPhoto;

    @FXML
    private TextField specialiteTextField;

    @FXML
    private TextArea bioTextArea;

    @FXML
    private Spinner<Integer> spinnerExperience;

    @FXML
    private TextField linkedinTextField;

    @FXML
    private TextField portfolioTextField;

    @FXML
    private TextField cvTextField;

    @FXML
    private Button btnUploadCv;

    @FXML
    private Label lblCvFileName;

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
    private FileUploadService fileUploadService;

    private Formateur formateur;
    private Mode mode;
    private File uploadedPhotoFile;
    private File uploadedCvFile;

    public enum Mode {
        ADD, EDIT
    }

    @FXML
    public void initialize() {
        formateurService = new FormateurService();
        userService = new UserService();
        fileUploadService = new FileUploadService();

        spinnerExperience.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));
        spinnerExperience.setEditable(true);
        gouvernoratField.setItems(TunisiaGovernorates.asObservableList());

        setupUserComboBox();
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        emailField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditEmail();
            }
        });
        telephoneField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditPhone();
            }
        });
        nomField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditNom();
            }
        });
        prenomField.textProperty().addListener((obs, o, n) -> {
            if (mode == Mode.EDIT) {
                validateEditPrenom();
            }
        });
        dateNaissanceField.valueProperty().addListener((obs, o, n) -> {
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
    private void onTogglePassword() {
        if (passwordGroup == null || !passwordGroup.isVisible()) {
            return;
        }
        togglePasswordField(passwordField, btnTogglePassword, true);
    }

    @FXML
    private void onTogglePasswordConfirm() {
        if (passwordConfirmGroup == null || !passwordConfirmGroup.isVisible()) {
            return;
        }
        togglePasswordField(passwordConfirmField, btnTogglePasswordConfirm, false);
    }

    private void togglePasswordField(PasswordField targetField, Button toggleButton, boolean primary) {
        HBox parent = (HBox) toggleButton.getParent();
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

        if (currentField == null) {
            return;
        }

        if (currentField instanceof PasswordField currentPasswordField) {
            TextField visiblePassword = new TextField();
            visiblePassword.setPromptText(currentPasswordField.getPromptText());
            visiblePassword.getStyleClass().addAll(currentPasswordField.getStyleClass());
            visiblePassword.setStyle(currentPasswordField.getStyle());
            visiblePassword.textProperty().bindBidirectional(currentPasswordField.textProperty());

            parent.getChildren().set(fieldIndex, visiblePassword);

            if (primary) {
                eyeIcon.setVisible(false);
                eyeIcon.setManaged(false);
                eyeSlashIcon.setVisible(true);
                eyeSlashIcon.setManaged(true);
            } else {
                eyeIconConfirm.setVisible(false);
                eyeIconConfirm.setManaged(false);
                eyeSlashIconConfirm.setVisible(true);
                eyeSlashIconConfirm.setManaged(true);
            }
        } else if (currentField instanceof TextField visiblePasswordField) {
            PasswordField restore = primary ? passwordField : passwordConfirmField;
            if (restore == null) {
                return;
            }
            visiblePasswordField.textProperty().unbindBidirectional(restore.textProperty());
            restore.setPromptText(visiblePasswordField.getPromptText());
            restore.getStyleClass().setAll(visiblePasswordField.getStyleClass());
            restore.setStyle(visiblePasswordField.getStyle());
            parent.getChildren().set(fieldIndex, restore);

            if (primary) {
                eyeIcon.setVisible(true);
                eyeIcon.setManaged(true);
                eyeSlashIcon.setVisible(false);
                eyeSlashIcon.setManaged(false);
            } else {
                eyeIconConfirm.setVisible(true);
                eyeIconConfirm.setManaged(true);
                eyeSlashIconConfirm.setVisible(false);
                eyeSlashIconConfirm.setManaged(false);
            }
        }
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

    @FXML
    private void onUploadCv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier CV");

        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
        FileChooser.ExtensionFilter docFilter = new FileChooser.ExtensionFilter("Documents", "*.doc", "*.docx");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");

        fileChooser.getExtensionFilters().addAll(pdfFilter, docFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(pdfFilter);

        Stage stage = (Stage) btnUploadCv.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            uploadedCvFile = selectedFile;
            cvTextField.setText(selectedFile.getAbsolutePath());
            lblCvFileName.setText(selectedFile.getName());
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
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

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
        populateForm();
    }

    private void populateForm() {
        if (formateur != null) {
            specialiteTextField.setText(formateur.getSpecialite() != null ? formateur.getSpecialite() : "");
            bioTextArea.setText(formateur.getBio() != null ? formateur.getBio() : "");
            int exp = formateur.getExperience_annees() != null ? formateur.getExperience_annees() : 0;
            spinnerExperience.getValueFactory().setValue(Math.min(70, Math.max(0, exp)));
            spinnerExperience.getEditor().setText(String.valueOf(exp));
            linkedinTextField.setText(formateur.getLinkedin() != null ? formateur.getLinkedin() : "");
            portfolioTextField.setText(formateur.getPortfolio() != null ? formateur.getPortfolio() : "");
            cvTextField.setText(formateur.getCv() != null ? formateur.getCv() : "");
            if (formateur.getCv() != null && !formateur.getCv().isEmpty()) {
                lblCvFileName.setText(stripToFileName(formateur.getCv()));
            }
            if (noteTextField != null) {
                noteTextField.setText(formateur.getNote_moyenne() != null ? formateur.getNote_moyenne().toString() : "");
            }

            if (formateur.getUser() != null) {
                if (userComboBox != null) {
                    userComboBox.setValue(formateur.getUser());
                }
                User user = formateur.getUser();
                emailField.setText(user.getEmail() != null ? user.getEmail() : "");
                nomField.setText(user.getNom() != null ? user.getNom() : "");
                prenomField.setText(user.getPrenom() != null ? user.getPrenom() : "");
                telephoneField.setText(user.getTelephone() != null ? user.getTelephone() : "");
                gouvernoratField.setValue(user.getGouvernorat());
                photoField.setText(user.getPhoto() != null ? user.getPhoto() : "");
                if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                    lblPhotoFileName.setText(stripToFileName(user.getPhoto()));
                }

                if (user.getDate_naissance() != null) {
                    dateNaissanceField.setValue(user.getDate_naissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
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
    }

    private static String stripToFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "Aucun fichier";
        }
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private void clearForm() {
        emailField.clear();
        passwordField.clear();
        passwordConfirmField.clear();
        nomField.clear();
        prenomField.clear();
        telephoneField.clear();
        gouvernoratField.setValue(null);
        dateNaissanceField.setValue(null);
        photoField.clear();
        lblPhotoFileName.setText("Aucune photo sélectionnée");
        imageViewPhoto.setImage(null);
        uploadedPhotoFile = null;
        specialiteTextField.clear();
        bioTextArea.clear();
        spinnerExperience.getValueFactory().setValue(0);
        spinnerExperience.getEditor().setText("0");
        linkedinTextField.clear();
        portfolioTextField.clear();
        cvTextField.clear();
        lblCvFileName.setText("Aucun fichier sélectionné");
        uploadedCvFile = null;
        if (noteTextField != null) {
            noteTextField.clear();
        }
        if (userComboBox != null) {
            userComboBox.setValue(null);
        }
    }

    private int readExperienceYears() {
        try {
            String t = spinnerExperience.getEditor().getText();
            if (t == null || t.isBlank()) {
                Integer v = spinnerExperience.getValue();
                return v != null ? v : 0;
            }
            return Integer.parseInt(t.trim());
        } catch (NumberFormatException e) {
            return spinnerExperience.getValue() != null ? spinnerExperience.getValue() : 0;
        }
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String phoneNorm = SignupFieldValidation.normalizePhone(telephoneField.getText());

            User userToUse;

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
                dbUser.setNom(nomField.getText().trim());
                dbUser.setPrenom(prenomField.getText().trim());
                dbUser.setTelephone(phoneNorm);
                dbUser.setGouvernorat(gouvernoratField.getValue());
                LocalDate localDateEdit = dateNaissanceField.getValue();
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
                newUser.setNom(nomField.getText().trim());
                newUser.setPrenom(prenomField.getText().trim());
                newUser.setTelephone(phoneNorm);
                newUser.setGouvernorat(gouvernoratField.getValue());
                newUser.setRole_utilisateur("formateur");
                newUser.setIs_email_verified(true);

                LocalDate localDate = dateNaissanceField.getValue();
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

            formateur.setSpecialite(specialiteTextField.getText().trim());
            formateur.setBio(bioTextArea.getText().trim().isEmpty() ? null : bioTextArea.getText().trim());

            int experienceYears = readExperienceYears();
            if (experienceYears < 0 || experienceYears > 70) {
                showAlert("Erreur de validation", "L'expérience doit être comprise entre 0 et 70 ans", Alert.AlertType.ERROR);
                return;
            }
            formateur.setExperience_annees(experienceYears > 0 ? experienceYears : null);

            String linkedinText = linkedinTextField.getText().trim();
            formateur.setLinkedin(linkedinText.isEmpty() ? null : linkedinText);

            String portfolioText = portfolioTextField.getText().trim();
            formateur.setPortfolio(portfolioText.isEmpty() ? null : portfolioText);

            String cvText = cvTextField.getText().trim();
            if (uploadedCvFile != null) {
                cvText = fileUploadService.uploadCv(uploadedCvFile);
            }
            formateur.setCv(cvText.isEmpty() ? null : cvText);

            if (noteTextField != null) {
                String noteText = noteTextField.getText().trim();
                if (!noteText.isEmpty()) {
                    formateur.setNote_moyenne(Double.parseDouble(noteText));
                } else {
                    formateur.setNote_moyenne(null);
                }
            }

            formateur.setUser(userToUse);

            if (mode == Mode.ADD) {
                formateurService.ajouter(formateur);
                showAlert("Succès", "Formateur ajouté avec succès", Alert.AlertType.INFORMATION);
            } else {
                formateurService.modifier(formateur);
                showAlert("Succès", "Formateur modifié avec succès", Alert.AlertType.INFORMATION);
            }

            closeForm();
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Vérifiez les valeurs numériques (expérience, note).", Alert.AlertType.ERROR);
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

        if (mode == Mode.EDIT) {
            return validateEditForm();
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!email.isEmpty() || (password != null && !password.isEmpty())) {
            if (!SignupFieldValidation.isValidEmail(email)) {
                showAlert("Erreur de validation", "Email invalide ou manquant.", Alert.AlertType.ERROR);
                return false;
            }
            String phoneNorm = SignupFieldValidation.normalizePhone(telephoneField.getText());
            if (!SignupFieldValidation.isValidPhoneNormalized(phoneNorm)) {
                showAlert("Erreur de validation", "Téléphone invalide (8–12 chiffres).", Alert.AlertType.ERROR);
                return false;
            }
            String pwdErr = SignupFieldValidation.validatePasswordStrength(password != null ? password : "");
            if (pwdErr != null) {
                showAlert("Erreur de validation", pwdErr, Alert.AlertType.ERROR);
                return false;
            }
            String passwordConfirm = passwordConfirmField.getText();
            if (passwordConfirm == null || passwordConfirm.isEmpty()) {
                showAlert("Erreur de validation", "Confirmez le mot de passe.", Alert.AlertType.ERROR);
                return false;
            }
            if (!password.equals(passwordConfirm)) {
                showAlert("Erreur de validation", "Les mots de passe ne correspondent pas.", Alert.AlertType.ERROR);
                return false;
            }
            if (!SignupFieldValidation.isValidNomPrenom(nomField.getText())) {
                showAlert("Erreur de validation", "Le nom est obligatoire (min. 2 caractères).", Alert.AlertType.ERROR);
                return false;
            }
            if (!SignupFieldValidation.isValidNomPrenom(prenomField.getText())) {
                showAlert("Erreur de validation", "Le prénom est obligatoire (min. 2 caractères).", Alert.AlertType.ERROR);
                return false;
            }
            if (dateNaissanceField.getValue() == null) {
                showAlert("Erreur de validation", "La date de naissance est obligatoire", Alert.AlertType.ERROR);
                return false;
            }
            return true;
        }

        if (userComboBox != null && userComboBox.getValue() != null) {
            return true;
        }
        showAlert("Erreur de validation",
            "Renseignez email et mot de passe (comme à l'inscription) ou sélectionnez un utilisateur existant.",
            Alert.AlertType.ERROR);
        return false;
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
        if (!SignupFieldValidation.isValidEmail(emailField.getText().trim())) {
            showError(errorEmail, "Email invalide ou manquant.");
            return false;
        }
        hideError(errorEmail);
        return true;
    }

    private boolean validateEditPhone() {
        String phoneNorm = SignupFieldValidation.normalizePhone(telephoneField.getText());
        if (!SignupFieldValidation.isValidPhoneNormalized(phoneNorm)) {
            showError(errorTelephone, "Téléphone invalide (8-12 chiffres).");
            return false;
        }
        hideError(errorTelephone);
        return true;
    }

    private boolean validateEditNom() {
        if (!SignupFieldValidation.isValidNomPrenom(nomField.getText())) {
            showError(errorNom, "Le nom est obligatoire (min. 2 caractères).");
            return false;
        }
        hideError(errorNom);
        return true;
    }

    private boolean validateEditPrenom() {
        if (!SignupFieldValidation.isValidNomPrenom(prenomField.getText())) {
            showError(errorPrenom, "Le prénom est obligatoire (min. 2 caractères).");
            return false;
        }
        hideError(errorPrenom);
        return true;
    }

    private boolean validateEditBirthDate() {
        if (dateNaissanceField.getValue() == null) {
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
    }

    private void showError(Label label, String msg) {
        if (label == null) {
            return;
        }
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        if (label == null) {
            return;
        }
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
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
