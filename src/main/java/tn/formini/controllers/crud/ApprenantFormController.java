package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Domaine;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.DomaineService;

import java.util.List;

public class ApprenantFormController {

    @FXML
    private ComboBox<String> genreComboBox;
    
    @FXML
    private ComboBox<String> etatCivilComboBox;
    
    @FXML
    private TextArea objectifTextArea;
    
    @FXML
    private TextArea domainesInteretTextArea;
    
    @FXML
    private ComboBox<User> userComboBox;
    
    @FXML
    private ComboBox<Domaine> domaineComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private ApprenantService apprenantService;
    private DomaineService domaineService;
    
    private Apprenant apprenant;
    private Mode mode;
    
    public enum Mode {
        ADD, EDIT
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apprenantService = new ApprenantService();
        domaineService = new DomaineService();
        
        setupComboBoxes();
        setupValidationListeners();
    }

    private void setupComboBoxes() {
        genreComboBox.setItems(FXCollections.observableArrayList("homme", "femme", "autre"));
        etatCivilComboBox.setItems(FXCollections.observableArrayList("celibataire", "marie", "divorce", "veuf"));
        
        List<User> users = userService.afficher();
        userComboBox.setItems(FXCollections.observableArrayList(users));
        
        List<Domaine> domaines = domaineService.afficher();
        domaineComboBox.setItems(FXCollections.observableArrayList(domaines));
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        updateFormForMode(mode);
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
            objectifField.setText(apprenant.getObjectif() != null ? apprenant.getObjectif() : "");
            setDomainesFromRaw(apprenant.getDomaines_interet());

            if (apprenant.getUser() != null) {
                userComboBox.setValue(apprenant.getUser());
            }
            
            if (apprenant.getDomaine() != null) {
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
        objectifTextArea.clear();
        domainesInteretTextArea.clear();
        userComboBox.setValue(null);
        domaineComboBox.setValue(null);
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        hideMessage();
        if (!validateForm()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
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
            }

            apprenant.setGenre(genreComboBox.getValue());
            apprenant.setEtat_civil(etatCivilComboBox.getValue());
            apprenant.setObjectif(objectifTextArea.getText().trim().isEmpty() ? null : objectifTextArea.getText().trim());
            apprenant.setDomaines_interet(domainesInteretTextArea.getText().trim().isEmpty() ? null : domainesInteretTextArea.getText().trim());
            apprenant.setUser(userComboBox.getValue());
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
        if (userComboBox.getValue() == null) {
            showAlert("Erreur de validation", "Veuillez sélectionner un utilisateur", Alert.AlertType.ERROR);
            return false;
        }

        String domainesInteret = domainesInteretTextArea.getText().trim();
        if (!domainesInteret.isEmpty()) {
            if (!domainesInteret.startsWith("[") || !domainesInteret.endsWith("]")) {
                showAlert("Erreur de validation", "Les domaines d'intérêt doivent être au format JSON array (ex: [\"domaine1\", \"domaine2\"])", Alert.AlertType.ERROR);
                return false;
            }
        }

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

