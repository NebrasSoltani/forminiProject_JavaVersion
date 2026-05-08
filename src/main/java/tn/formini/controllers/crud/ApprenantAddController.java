package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.FileUploadService;
import tn.formini.utils.SignupFieldValidation;
import tn.formini.utils.TunisiaGovernorates;
import tn.formini.controllers.MainController;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ApprenantAddController implements Initializable {

    @FXML private Label lblMessage;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordConfirmField;
    @FXML private TextField nomTextField;
    @FXML private TextField prenomTextField;
    @FXML private TextField telephoneTextField;
    @FXML private ComboBox<String> gouvernoratComboBox;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ComboBox<String> etatCivilComboBox;
    @FXML private ComboBox<String> niveauComboBox;
    @FXML private ComboBox<String> disponibiliteComboBox;
    @FXML private DatePicker dateNaissanceField;
    @FXML private TextField photoField;
    @FXML private Label lblPhotoFileName;
    @FXML private Button btnUploadPhoto;
    @FXML private ImageView imageViewPhoto;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnTogglePasswordConfirm;
    @FXML private Label errorEmail;
    @FXML private Label errorTelephone;
    @FXML private Label errorNom;
    @FXML private Label errorPrenom;
    @FXML private Label errorDateNaissance;
    @FXML private Label errorPassword;
    @FXML private Label errorPasswordConfirm;
    @FXML private Label errorObjectif;
    @FXML private TextField objectifTextField;
    @FXML private TextArea domainesInteretTextArea;
    @FXML private Button saveButton;

    private ApprenantService apprenantService;
    private UserService userService;
    private FileUploadService fileUploadService;
    private File uploadedPhotoFile;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apprenantService = new ApprenantService();
        userService = new UserService();
        fileUploadService = new FileUploadService();

        setupComboBoxes();
        setupValidationListeners();
    }

    private void setupComboBoxes() {
        genreComboBox.setItems(FXCollections.observableArrayList("homme", "femme", "autre"));
        etatCivilComboBox.setItems(FXCollections.observableArrayList("celibataire", "marie", "divorce", "veuf"));
        gouvernoratComboBox.setItems(TunisiaGovernorates.asObservableList());
        niveauComboBox.setItems(FXCollections.observableArrayList("Débutant", "Intermédiaire", "Avancé", "Expert"));
        disponibiliteComboBox.setItems(FXCollections.observableArrayList(
            "Moins de 5h/semaine", "5-10h/semaine", "10-20h/semaine", "Plus de 20h/semaine"
        ));
    }

    private void setupValidationListeners() {
        emailTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorEmail));
        telephoneTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorTelephone));
        nomTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorNom));
        prenomTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorPrenom));
        passwordField.textProperty().addListener((obs, oldV, newV) -> hideError(errorPassword));
        passwordConfirmField.textProperty().addListener((obs, oldV, newV) -> hideError(errorPasswordConfirm));
        objectifTextField.textProperty().addListener((obs, oldV, newV) -> hideError(errorObjectif));
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        hideMessage();
        if (!validateForm()) {
            showMessage("Veuillez corriger les erreurs dans le formulaire.");
            return;
        }

        try {
            String email = emailTextField.getText().trim();
            String password = passwordField.getText();
            String phoneNorm = SignupFieldValidation.normalizePhone(telephoneTextField.getText());

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
            newUser.setGouvernorat(gouvernoratComboBox.getValue());
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

            Apprenant apprenant = new Apprenant();
            apprenant.setGenre(genreComboBox.getValue());
            apprenant.setEtat_civil(etatCivilComboBox.getValue());
            apprenant.setObjectif(objectifTextField.getText().trim().isEmpty() ? null : objectifTextField.getText().trim());
            apprenant.setDomaines_interet(domainesInteretTextArea.getText().trim().isEmpty() ? null : domainesInteretTextArea.getText().trim());
            apprenant.setUser(newUser);

            apprenantService.ajouter(apprenant);
            showMessage("Apprenant ajouté avec succès.");

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
        boolean isValid = true;

        if (emailTextField.getText().trim().isEmpty()) {
            showError(errorEmail, "L'email est obligatoire");
            isValid = false;
        } else if (!SignupFieldValidation.isValidEmail(emailTextField.getText().trim())) {
            showError(errorEmail, "Email invalide");
            isValid = false;
        }

        if (passwordField.getText().isEmpty()) {
            showError(errorPassword, "Le mot de passe est obligatoire");
            isValid = false;
        } else if (!SignupFieldValidation.isValidPassword(passwordField.getText())) {
            showError(errorPassword, "Mot de passe trop faible");
            isValid = false;
        }

        if (!passwordField.getText().equals(passwordConfirmField.getText())) {
            showError(errorPasswordConfirm, "Les mots de passe ne correspondent pas");
            isValid = false;
        }

        if (nomTextField.getText().trim().isEmpty()) {
            showError(errorNom, "Le nom est obligatoire");
            isValid = false;
        }

        if (prenomTextField.getText().trim().isEmpty()) {
            showError(errorPrenom, "Le prénom est obligatoire");
            isValid = false;
        }

        if (telephoneTextField.getText().trim().isEmpty()) {
            showError(errorTelephone, "Le téléphone est obligatoire");
            isValid = false;
        } else if (!SignupFieldValidation.isValidPhone(telephoneTextField.getText())) {
            showError(errorTelephone, "Téléphone invalide");
            isValid = false;
        }

        if (dateNaissanceField.getValue() == null) {
            showError(errorDateNaissance, "La date de naissance est obligatoire");
            isValid = false;
        }

        if (objectifTextField.getText().trim().isEmpty()) {
            showError(errorObjectif, "L'objectif est obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void showMessage(String text) {
        lblMessage.setText(text);
        if (!lblMessage.getStyleClass().contains("signup-alert")) {
            lblMessage.getStyleClass().add("signup-alert");
        }
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {
        lblMessage.setText("");
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    private void showError(Label label, String msg) {
        if (label == null) return;
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        if (label == null) return;
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private void closeForm() {
        if (mainController != null) {
            mainController.showApprenantManagement();
        } else if (saveButton.getScene() != null && saveButton.getScene().getWindow() != null) {
            saveButton.getScene().getWindow().hide();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onTogglePassword(ActionEvent event) {
        if (passwordField.getText().isEmpty()) return;
        
        // Toggle between eye and eye-slash icons
        if ("👁".equals(btnTogglePassword.getText())) {
            btnTogglePassword.setText("👁‍🗨");
        } else {
            btnTogglePassword.setText("👁");
        }
    }

    @FXML
    private void onTogglePasswordConfirm(ActionEvent event) {
        if (passwordConfirmField.getText().isEmpty()) return;
        
        // Toggle between eye and eye-slash icons
        if ("👁".equals(btnTogglePasswordConfirm.getText())) {
            btnTogglePasswordConfirm.setText("👁‍🗨");
        } else {
            btnTogglePasswordConfirm.setText("👁");
        }
    }

    @FXML
    private void onUploadPhoto(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(btnUploadPhoto.getScene().getWindow());
        if (file != null) {
            uploadedPhotoFile = file;
            lblPhotoFileName.setText(file.getName());
            try {
                imageViewPhoto.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
