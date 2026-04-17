package tn.formini.controllers.crud;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Domaine;
import tn.formini.entities.Users.User;
import tn.formini.services.FileUploadService;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.DomaineService;
import tn.formini.services.UsersService.UserService;
import tn.formini.utils.SignupFieldValidation;
import tn.formini.utils.TunisiaGovernorates;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ApprenantFormController {

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
    private ComboBox<String> genreComboBox;

    @FXML
    private ComboBox<String> etatCivilComboBox;

    @FXML
    private TextField objectifField;

    @FXML
    private TextField fieldDomaineInput;

    @FXML
    private Button btnAddDomaine;

    @FXML
    private HBox flowPaneDomaines;

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
    private FileUploadService fileUploadService;

    private Apprenant apprenant;
    private Mode mode;
    private File uploadedPhotoFile;
    private final List<String> domainesList = new ArrayList<>();

    public enum Mode {
        ADD, EDIT
    }

    @FXML
    public void initialize() {
        apprenantService = new ApprenantService();
        userService = new UserService();
        domaineService = new DomaineService();
        fileUploadService = new FileUploadService();

        setupComboBoxes();
        setupValidationListeners();
    }

    private void setupComboBoxes() {
        genreComboBox.setItems(FXCollections.observableArrayList("homme", "femme", "autre"));
        etatCivilComboBox.setItems(FXCollections.observableArrayList("celibataire", "marie", "divorce", "veuf"));
        gouvernoratField.setItems(TunisiaGovernorates.asObservableList());

        if (userComboBox != null) {
            List<User> users = userService.afficher();
            userComboBox.setItems(FXCollections.observableArrayList(users));
        }
        if (domaineComboBox != null) {
            List<Domaine> domaines = domaineService.afficher();
            domaineComboBox.setItems(FXCollections.observableArrayList(domaines));
        }
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
                    "Même présentation que l'inscription : identité, connexion, puis profil apprenant. Les champs * sont obligatoires pour la création.");
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

    public void setApprenant(Apprenant apprenant) {
        this.apprenant = apprenant;
        populateForm();
    }

    private void populateForm() {
        if (apprenant != null) {
            genreComboBox.setValue(apprenant.getGenre());
            etatCivilComboBox.setValue(apprenant.getEtat_civil());
            objectifField.setText(apprenant.getObjectif() != null ? apprenant.getObjectif() : "");
            setDomainesFromRaw(apprenant.getDomaines_interet());

            if (apprenant.getUser() != null) {
                if (userComboBox != null) {
                    userComboBox.setValue(apprenant.getUser());
                }
                User user = apprenant.getUser();
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

            if (apprenant.getDomaine() != null && domaineComboBox != null) {
                domaineComboBox.setValue(apprenant.getDomaine());
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
        genreComboBox.setValue(null);
        etatCivilComboBox.setValue(null);
        objectifField.clear();
        if (fieldDomaineInput != null) {
            fieldDomaineInput.clear();
        }
        domainesList.clear();
        displayDomainesTags();
        if (userComboBox != null) {
            userComboBox.setValue(null);
        }
        if (domaineComboBox != null) {
            domaineComboBox.setValue(null);
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

            if (mode == Mode.EDIT && apprenant != null && apprenant.getUser() != null) {
                User dbUser = userService.getUserByEmail(apprenant.getUser().getEmail());
                if (dbUser == null) {
                    dbUser = userService.findById(apprenant.getUser().getId());
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
                newUser.setRole_utilisateur("apprenant");
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
                apprenant = new Apprenant();
            }

            apprenant.setGenre(genreComboBox.getValue());
            apprenant.setEtat_civil(etatCivilComboBox.getValue());
            apprenant.setObjectif(objectifField.getText().trim().isEmpty() ? null : objectifField.getText().trim());

            String domainesInteret = convertDomainesToJson(domainesList);
            if ("[]".equals(domainesInteret)) {
                if (mode == Mode.EDIT && apprenant.getDomaines_interet() != null && !apprenant.getDomaines_interet().trim().isEmpty()) {
                    apprenant.setDomaines_interet(apprenant.getDomaines_interet());
                } else {
                    apprenant.setDomaines_interet("[]");
                }
            } else {
                apprenant.setDomaines_interet(domainesInteret);
            }

            apprenant.setUser(userToUse);
            apprenant.setDomaine(domaineComboBox != null ? domaineComboBox.getValue() : null);

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
        if (mode == Mode.EDIT) {
            return validateEditForm();
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String passwordConfirm = passwordConfirmField.getText();

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
        if (userComboBox == null) {
            showAlert("Erreur de validation", "Renseignez l'email et le mot de passe pour créer un compte apprenant.", Alert.AlertType.ERROR);
            return false;
        }
        showAlert("Erreur de validation", "Veuillez sélectionner un utilisateur ou remplir les informations de base", Alert.AlertType.ERROR);
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
        return valid;
    }

    private boolean validateEditEmail() {
        String email = emailField.getText().trim();
        if (!SignupFieldValidation.isValidEmail(email)) {
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

    @FXML
    private void onAddDomaine() {
        if (fieldDomaineInput == null) {
            return;
        }
        String domaine = fieldDomaineInput.getText() != null ? fieldDomaineInput.getText().trim() : "";
        if (domaine.isEmpty()) {
            return;
        }

        boolean alreadyExists = domainesList.stream().anyMatch(existing -> existing.equalsIgnoreCase(domaine));
        if (!alreadyExists) {
            domainesList.add(domaine);
            displayDomainesTags();
        }
        fieldDomaineInput.clear();
    }

    private void displayDomainesTags() {
        if (flowPaneDomaines == null) {
            return;
        }
        flowPaneDomaines.getChildren().clear();
        for (String domaine : domainesList) {
            flowPaneDomaines.getChildren().add(createDomaineTag(domaine));
        }
    }

    private HBox createDomaineTag(String domaine) {
        HBox tag = new HBox();
        tag.getStyleClass().add("domaine-tag");
        tag.setSpacing(4);

        Label label = new Label(domaine);
        label.getStyleClass().add("domaine-tag-label");

        Button removeBtn = new Button("x");
        removeBtn.getStyleClass().add("domaine-tag-remove");
        removeBtn.setOnAction(e -> {
            domainesList.remove(domaine);
            displayDomainesTags();
        });

        tag.getChildren().addAll(label, removeBtn);
        return tag;
    }

    private void setDomainesFromRaw(String rawDomaines) {
        domainesList.clear();
        if (rawDomaines == null || rawDomaines.trim().isEmpty() || "[]".equals(rawDomaines.trim())) {
            displayDomainesTags();
            return;
        }

        String cleaned = rawDomaines.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");
        if (!cleaned.isBlank()) {
            for (String part : cleaned.split(",")) {
                String domaine = part.trim();
                if (!domaine.isEmpty()) {
                    domainesList.add(domaine);
                }
            }
        }
        displayDomainesTags();
    }

    private String convertDomainesToJson(List<String> domaines) {
        if (domaines == null || domaines.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < domaines.size(); i++) {
            json.append("\"").append(domaines.get(i).replace("\"", "\\\"")).append("\"");
            if (i < domaines.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
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

