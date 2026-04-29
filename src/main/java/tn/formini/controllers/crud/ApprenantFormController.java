package tn.formini.controllers.crud;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Domaine;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Gouvernorat;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.DomaineService;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.FileUploadService;
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

public class ApprenantFormController implements Initializable {

    @FXML private Label lblMessage;
    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private Label heroSubLabel;
    @FXML private TextField fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldPasswordConfirm;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldTelephone;
    @FXML private ComboBox<String> fieldGouvernorat;
    @FXML private DatePicker fieldDateNaissance;
    @FXML private TextField photoField;
    @FXML private Label lblPhotoFileName;
    @FXML private Button btnUploadPhoto;
    @FXML private ImageView imageViewPhoto;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnTogglePasswordConfirm;
    @FXML private Label eyeIcon;
    @FXML private Label eyeSlashIcon;
    @FXML private Label eyeIconConfirm;
    @FXML private Label eyeSlashIconConfirm;
    @FXML private Label errorEmail;
    @FXML private Label errorTelephone;
    @FXML private Label errorNom;
    @FXML private Label errorPrenom;
    @FXML private Label errorDateNaissance;
    @FXML private Label errorPassword;
    @FXML private Label errorPasswordConfirm;
    @FXML private VBox passwordGroup;
    @FXML private VBox passwordConfirmGroup;
    @FXML private Label passwordHintLabel;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private TextField objectifField;
    @FXML private TextField fieldDomaineInput;
    @FXML private FlowPane flowPaneDomaines;
    @FXML private Label errorGenre;
    @FXML private Label errorEtatCivil;
    @FXML private Label errorDomaine;
    @FXML private Label errorObjectif;
    @FXML private Label errorDomainesInteret;

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private ComboBox<String> etatCivilComboBox;

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
    private UserService userService;
    private FileUploadService fileUploadService;
    private ObservableList<String> domainesList = FXCollections.observableArrayList();
    private File uploadedPhotoFile;
    
    private Apprenant apprenant;
    private Mode mode;
    
    public enum Mode {
        ADD, EDIT
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apprenantService = new ApprenantService();
        domaineService = new DomaineService();
        userService = new UserService();
        fileUploadService = new FileUploadService();

        setupComboBoxes();
        setupValidationListeners();
    }

    private void setupComboBoxes() {
        genreComboBox.setItems(FXCollections.observableArrayList("homme", "femme", "autre"));
        etatCivilComboBox.setItems(FXCollections.observableArrayList("celibataire", "marie", "divorce", "veuf"));
        fieldGouvernorat.setItems(TunisiaGovernorates.asObservableList());

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
            fieldPassword.clear();
            fieldPasswordConfirm.clear();
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
        fieldEmail.clear();
        fieldPassword.clear();
        fieldPasswordConfirm.clear();
        fieldNom.clear();
        fieldPrenom.clear();
        fieldTelephone.clear();
        fieldGouvernorat.setValue(null);
        fieldDateNaissance.setValue(null);
        photoField.clear();
        lblPhotoFileName.setText("Aucune photo sélectionnée");
        imageViewPhoto.setImage(null);
        uploadedPhotoFile = null;
        genreComboBox.setValue(null);
        etatCivilComboBox.setValue(null);
        objectifField.clear();
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
            String email = fieldEmail.getText().trim();
            String password = fieldPassword.getText();
            String phoneNorm = SignupFieldValidation.normalizePhone(fieldTelephone.getText());

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
                dbUser.setNom(fieldNom.getText().trim());
                dbUser.setPrenom(fieldPrenom.getText().trim());
                dbUser.setTelephone(phoneNorm);
                dbUser.setGouvernorat(fieldGouvernorat.getValue());
                LocalDate localDateEdit = fieldDateNaissance.getValue();
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
                newUser.setNom(fieldNom.getText().trim());
                newUser.setPrenom(fieldPrenom.getText().trim());
                newUser.setTelephone(phoneNorm);
                newUser.setGouvernorat(fieldGouvernorat.getValue());
                newUser.setRole_utilisateur("apprenant");
                newUser.setIs_email_verified(true);

                LocalDate localDate = fieldDateNaissance.getValue();
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
            apprenant.setDomaines_interet(convertDomainesToJson(domainesList));
            apprenant.setUser(userToUse);
            apprenant.setDomaine(domaineComboBox.getValue());

            if (mode == Mode.ADD) {
                apprenantService.ajouter(apprenant);
                showMessage("Apprenant ajouté avec succès.");
            } else {
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
        if (userComboBox.getValue() == null && (fieldEmail.getText().trim().isEmpty() || fieldPassword.getText().isEmpty())) {
            showAlert("Erreur de validation", "Veuillez sélectionner un utilisateur ou remplir les champs email/mot de passe", Alert.AlertType.ERROR);
            return false;
        }
        return true;
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

    private void closeForm() {
        if (cancelButton.getScene() != null && cancelButton.getScene().getWindow() != null) {
            cancelButton.getScene().getWindow().hide();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setDomainesFromRaw(String rawDomaines) {
        domainesList.clear();
        if (rawDomaines == null || rawDomaines.trim().isEmpty() || "[]".equals(rawDomaines.trim())) {
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
        objectifField.textProperty().addListener((obs, oldV, newV) -> hideError(errorObjectif));
        domainesInteretTextArea.textProperty().addListener((obs, oldV, newV) -> hideError(errorDomainesInteret));
    }
}
