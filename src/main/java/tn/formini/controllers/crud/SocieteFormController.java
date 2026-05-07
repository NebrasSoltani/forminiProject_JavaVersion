package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.FileUploadService;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;
import tn.formini.utils.SignupFieldValidation;
import tn.formini.utils.TunisiaGovernorates;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.io.File;

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

    private FileUploadService fileUploadService;

    private Societe societe;
    private Mode mode;
    private File uploadedPhotoFile;

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
        
        // Always show new user panel
        panelNewUser.setVisible(true);
        panelNewUser.setManaged(true);
        fileUploadService = new FileUploadService();
        gouvernoratField.setItems(TunisiaGovernorates.asObservableList());
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Edit mode validation listeners
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
        
        // Update header text based on mode
        if (mode == Mode.ADD) {
            lblTitle.setText("Ajouter une société");
            lblSubtitle.setText("Complétez les informations de la société. Les champs marqués * sont obligatoires.");
            clearForm();
        } else {
            lblTitle.setText("Modifier une société");
            lblSubtitle.setText("Modifiez les informations de la société. Les champs marqués * sont obligatoires. Le mot de passe est optionnel.");
            setPasswordSectionVisible(true);
            if (heroSubLabel != null) {
                heroSubLabel.setText(
                    "Comme à l'inscription : d'abord le compte du contact (rôle société), puis les informations de l'entreprise.");
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
    private void onSubmit() {
        hideMessage();

        // Validate all fields first
        if (!validateAllFields()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
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

            societe.setNom_societe(trimToNull(fieldNomSociete.getText()));
            societe.setSecteur(trimToNull(fieldSecteur.getText()));
            societe.setDescription(trimToNull(fieldDescription.getText()));
            societe.setAdresse(trimToNull(fieldAdresse.getText()));
            societe.setSite_web(trimToNull(fieldSiteWeb.getText()));

            // Validate entity
            societe.valider();
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

    @FXML
    private void onToggleUserPassword() {
        toggleUserPasswordField(fieldUserPassword, btnToggleUserPassword);
    }

    @FXML
    private void onToggleUserPasswordConfirm() {
        toggleUserPasswordField(fieldUserPasswordConfirm, btnToggleUserPasswordConfirm);
    }

    private void toggleUserPasswordField(javafx.scene.control.PasswordField passwordField, Button toggleButton) {
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
        label.setStyle("-fx-text-fill: #dc2626;");
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
        if (btnCancel.getScene() != null && btnCancel.getScene().getWindow() != null) {
            btnCancel.getScene().getWindow().hide();
        }
    }
}
