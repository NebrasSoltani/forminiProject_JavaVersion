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
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.FileUploadService;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;
import tn.formini.utils.SignupFieldValidation;
import tn.formini.utils.TunisiaGovernorates;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;

public class SocieteFormController {

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
    private Label errorNomSociete;

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
    private TextField nomSocieteField;

    @FXML
    private TextField secteurTextField;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private TextField adresseTextField;

    @FXML
    private TextField siteWebTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private SocieteService societeService;
    private UserService userService;
    private FileUploadService fileUploadService;

    private Societe societe;
    private Mode mode;
    private File uploadedPhotoFile;

    public enum Mode {
        ADD, EDIT
    }

    @FXML
    public void initialize() {
        societeService = new SocieteService();
        userService = new UserService();
        fileUploadService = new FileUploadService();
        gouvernoratField.setItems(TunisiaGovernorates.asObservableList());
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
        nomSocieteField.textProperty().addListener((obs, o, n) -> validateSocieteName());
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

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.ADD) {
            clearForm();
            setPasswordSectionVisible(true);
            if (heroSubLabel != null) {
                heroSubLabel.setText(
                    "Comme à l'inscription : d'abord le compte du contact (rôle société), puis les informations de l'entreprise.");
            }
        } else {
            passwordField.clear();
            passwordConfirmField.clear();
            setPasswordSectionVisible(false);
            if (heroSubLabel != null) {
                heroSubLabel.setText(
                    "Même formulaire qu'à l'ajout : vous pouvez modifier la fiche société et le compte contact, sauf le mot de passe (inchangé depuis cet écran).");
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

    public void setSociete(Societe societe) {
        this.societe = societe;
        populateForm();
    }

    private void populateForm() {
        if (societe != null) {
            nomSocieteField.setText(societe.getNom_societe() != null ? societe.getNom_societe() : "");
            secteurTextField.setText(societe.getSecteur() != null ? societe.getSecteur() : "");
            descriptionTextArea.setText(societe.getDescription() != null ? societe.getDescription() : "");
            adresseTextField.setText(societe.getAdresse() != null ? societe.getAdresse() : "");
            siteWebTextField.setText(societe.getSite_web() != null ? societe.getSite_web() : "");

            if (societe.getUser() != null) {
                User user = societe.getUser();
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
            return "Aucune photo sélectionnée";
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
        nomSocieteField.clear();
        secteurTextField.clear();
        descriptionTextArea.clear();
        adresseTextField.clear();
        siteWebTextField.clear();
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

            if (mode == Mode.EDIT && societe != null && societe.getUser() != null) {
                User dbUser = userService.getUserByEmail(societe.getUser().getEmail());
                if (dbUser == null) {
                    dbUser = userService.findById(societe.getUser().getId());
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
                newUser.setRole_utilisateur("societe");
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
            } else {
                showAlert("Erreur de validation",
                    "Renseignez l'email et le mot de passe pour créer le compte contact.",
                    Alert.AlertType.ERROR);
                return;
            }

            boolean isNew = (mode == Mode.ADD);
            if (isNew) {
                societe = new Societe();
            }

            societe.setNom_societe(nomSocieteField.getText().trim());
            societe.setSecteur(secteurTextField.getText().trim().isEmpty() ? null : secteurTextField.getText().trim());
            societe.setDescription(descriptionTextArea.getText().trim().isEmpty() ? null : descriptionTextArea.getText().trim());
            societe.setAdresse(adresseTextField.getText().trim().isEmpty() ? null : adresseTextField.getText().trim());

            String siteWebText = siteWebTextField.getText().trim();
            if (!siteWebText.isEmpty() && !siteWebText.startsWith("http")) {
                siteWebText = "https://" + siteWebText;
            }
            societe.setSite_web(siteWebText.isEmpty() ? null : siteWebText);

            societe.setUser(userToUse);

            if (isNew) {
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
        boolean societeNameOk = validateSocieteName();

        if (mode == Mode.EDIT) {
            return societeNameOk && validateEditForm();
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

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
            showAlert("Erreur de validation", "Le nom du contact est obligatoire (min. 2 caractères).", Alert.AlertType.ERROR);
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
        LocalDate birth = dateNaissanceField.getValue();
        if (birth.isAfter(LocalDate.now().minusYears(13))) {
            showAlert("Erreur de validation", "Le représentant doit avoir au moins 13 ans.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean validateEditForm() {
        clearEditErrors();
        boolean valid = true;
        valid &= validateEditEmail();
        valid &= validateEditPhone();
        valid &= validateEditNom();
        valid &= validateEditPrenom();
        valid &= validateEditBirthDate();
        return valid;
    }

    private boolean validateSocieteName() {
        if (nomSocieteField.getText().trim().isEmpty()) {
            showError(errorNomSociete, "Le nom de la société est obligatoire.");
            return false;
        }
        hideError(errorNomSociete);
        return true;
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

    private void clearEditErrors() {
        hideError(errorEmail);
        hideError(errorTelephone);
        hideError(errorNom);
        hideError(errorPrenom);
        hideError(errorDateNaissance);
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
