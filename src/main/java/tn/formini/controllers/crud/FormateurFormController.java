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
    @FXML private TextField emailTextField = new TextField();
    @FXML private TextField telephoneTextField = new TextField();
    @FXML private PasswordField passwordField = new PasswordField();
    @FXML private PasswordField passwordConfirmField = new PasswordField();
    @FXML private Button btnTogglePassword = new Button();
    @FXML private Button btnTogglePasswordConfirm = new Button();
    @FXML private Label eyeIcon = new Label();
    @FXML private Label eyeSlashIcon = new Label();
    @FXML private Label eyeIconConfirm = new Label();
    @FXML private Label eyeSlashIconConfirm = new Label();
    @FXML private TextField nomTextField = new TextField();
    @FXML private TextField prenomTextField = new TextField();
    @FXML private ComboBox<String> gouvernoratComboBox = new ComboBox<>();
    @FXML private DatePicker dateNaissanceField = new DatePicker();

    // Formateur fields
    @FXML private TextField specialiteTextField = new TextField();
    @FXML private TextArea bioTextArea = new TextArea();
    @FXML private Spinner<Integer> experienceSpinner = new Spinner<>();
    @FXML private TextField linkedinTextField = new TextField();
    @FXML private TextField portfolioTextField = new TextField();
    @FXML private TextField cvTextField = new TextField();
    @FXML private Button btnUploadCv = new Button();
    @FXML private Label lblCvFileName = new Label();
    @FXML private TextField noteTextField = new TextField();
    @FXML private Button saveButton = new Button();
    @FXML private Button cancelButton = new Button();

    // Error labels
    @FXML private Label errorEmail = new Label();
    @FXML private Label errorTelephone = new Label();
    @FXML private Label errorPassword = new Label();
    @FXML private Label errorPasswordConfirm = new Label();
    @FXML private Label errorNom = new Label();
    @FXML private Label errorPrenom = new Label();
    @FXML private Label errorDateNaissance = new Label();
    @FXML private Label errorSpecialite = new Label();
    @FXML private Label errorExperience = new Label();
    @FXML private Label errorLinkedin = new Label();
    @FXML private Label errorPortfolio = new Label();
    @FXML private Label errorCv = new Label();
    @FXML private Label errorBio = new Label();

    // Alternate aliases kept for the merged code paths
    @FXML private Label heroSubLabel;
    @FXML private VBox passwordGroup;
    @FXML private VBox passwordConfirmGroup;
    @FXML private Label passwordHintLabel;

    private TextField emailField = new TextField();
    private TextField nomField = new TextField();
    private TextField prenomField = new TextField();
    private TextField telephoneField = new TextField();
    private ComboBox<String> gouvernoratField = new ComboBox<>();
    private DatePicker dateNaissancePicker = new DatePicker();
    private TextField photoField = new TextField();
    private Label lblPhotoFileName = new Label();
    private Button btnUploadPhoto = new Button();
    private ImageView imageViewPhoto = new ImageView();
    private Spinner<Integer> spinnerExperience = new Spinner<>();
    @FXML private ComboBox<User> userComboBox = new ComboBox<>();

    private FormateurService formateurService;
    private UserService userService;
    private FileUploadService fileUploadService = new FileUploadService();

    private Formateur formateur;
    private Mode mode;
    private File uploadedPhotoFile;
    private File uploadedCvFile;

    public enum Mode {
        ADD, EDIT
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formateurService = new FormateurService();
        userService = new UserService();

        emailField = emailTextField;
        nomField = nomTextField;
        prenomField = prenomTextField;
        telephoneField = telephoneTextField;
        dateNaissancePicker = dateNaissanceField;
        spinnerExperience = experienceSpinner;

        gouvernoratComboBox.setItems(TunisiaGovernorates.asObservableList());
        setupExperienceSpinner();
        setupValidationListeners();
    }

    private void setupExperienceSpinner() {
        experienceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));
        fileUploadService = new FileUploadService();

        spinnerExperience.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));
        spinnerExperience.setEditable(true);
        gouvernoratField.setItems(TunisiaGovernorates.asObservableList());

        setupUserComboBox();
        setupValidationListeners();
    }

    // setupValidationListeners moved/defined later in the file (single authoritative version)

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
                gouvernoratComboBox.setValue(user.getGouvernorat());
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
                User innerUser = formateur.getUser();
                emailField.setText(innerUser.getEmail() != null ? innerUser.getEmail() : "");
                nomField.setText(innerUser.getNom() != null ? innerUser.getNom() : "");
                prenomField.setText(innerUser.getPrenom() != null ? innerUser.getPrenom() : "");
                telephoneField.setText(innerUser.getTelephone() != null ? innerUser.getTelephone() : "");
                gouvernoratField.setValue(innerUser.getGouvernorat());
                photoField.setText(innerUser.getPhoto() != null ? innerUser.getPhoto() : "");
                if (innerUser.getPhoto() != null && !innerUser.getPhoto().isEmpty()) {
                    lblPhotoFileName.setText(stripToFileName(innerUser.getPhoto()));
                }

                if (innerUser.getDate_naissance() != null) {
                    dateNaissanceField.setValue(innerUser.getDate_naissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }

                if (innerUser.getPhoto() != null && !innerUser.getPhoto().isEmpty()) {
                    try {
                        Image image = new Image(innerUser.getPhoto());
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
        hideMessage();
        if (!validateForm()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
            return;
        }

        try {
            User user;
            
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
            String selectedGouvernorat = gouvernoratComboBox.getValue();
            user.setGouvernorat(selectedGouvernorat != null ? selectedGouvernorat : null);

            LocalDate birth = dateNaissancePicker.getValue();
            if (birth != null) {
                user.setDate_naissance(Date.from(birth.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            formateur.setUser(user);
            formateur.setSpecialite(specialiteTextField.getText().trim());
            formateur.setBio(bioTextArea.getText().trim().isEmpty() ? null : bioTextArea.getText().trim());
            
            int exp = experienceSpinner.getValue() != null ? experienceSpinner.getValue() : 0;
            formateur.setExperience_annees(exp > 0 ? exp : null);
            

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
            formateur.setCv(cvText.isEmpty() ? null : cvText);
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
                showMessage("Formateur créé avec succès.");
            } else {
                formateurService.modifier(formateur);
                showMessage("Formateur modifié avec succès.");
            }

            Platform.runLater(this::closeForm);
            closeForm();
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Vérifiez les valeurs numériques (expérience, note).", Alert.AlertType.ERROR);
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
        hideError(errorExperience);
        hideError(errorLinkedin);
        hideError(errorPortfolio);
        hideError(errorCv);
        hideError(errorBio);
    }

    // ...existing code...

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
