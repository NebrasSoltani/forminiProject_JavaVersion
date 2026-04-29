package tn.formini.controllers.auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.EmailVerificationService;
import tn.formini.services.UsersService.EmailService;
import tn.formini.services.FileUploadService;
import tn.formini.utils.TunisiaGovernorates;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class EditProfileController implements Initializable {

    @FXML private Label lblMessage;
    @FXML private Label lblRole;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldTelephone;
    @FXML private ComboBox<String> fieldGouvernorat;
    @FXML private DatePicker fieldDateNaissance;

    @FXML private VBox apprenantSection;
    @FXML private ComboBox<String> comboGenre;
    @FXML private ComboBox<String> comboEtatCivil;
    @FXML private TextField fieldObjectif;
    @FXML private TextField fieldDomainesInteret;

    @FXML private VBox formateurSection;
    @FXML private TextField fieldSpecialite;
    @FXML private TextArea fieldBio;
    @FXML private Spinner<Integer> spinnerExperience;
    @FXML private TextField fieldLinkedin;
    @FXML private TextField fieldPortfolio;
    @FXML private TextField fieldCv;
    @FXML private TextField fieldPhoto;
    @FXML private Button btnUploadPhoto;
    @FXML private Label lblPhotoFileName;
    @FXML private ImageView imageViewPhoto;
    @FXML private Button btnChangePassword;
    @FXML private VBox passwordChangeSection;
    @FXML private TextField fieldPasswordVerificationToken;
    @FXML private Button btnVerifyToken;
    @FXML private VBox newPasswordSection;
    @FXML private PasswordField fieldNewPassword;
    @FXML private PasswordField fieldNewPasswordConfirm;
    @FXML private Button btnTwoFactor;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final UserService userService = new UserService();
    private final ApprenantService apprenantService = new ApprenantService();
    private final FormateurService formateurService = new FormateurService();
    private final FileUploadService fileUploadService = new FileUploadService();
    private final EmailVerificationService emailVerificationService = new EmailVerificationService();
    private final EmailService emailService = new EmailService();
    private java.io.File uploadedPhotoFile;

    private Runnable onBack;
    private User currentUser;
    private Apprenant currentApprenant;
    private Formateur currentFormateur;

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboGenre.getItems().addAll("homme", "femme");
        comboEtatCivil.getItems().addAll("celibataire", "marie", "divorce", "veuf");
        fieldGouvernorat.setItems(TunisiaGovernorates.asObservableList());
        spinnerExperience.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 70, 0));
        hideMessage();
        loadConnectedUser();
    }

    private void loadConnectedUser() {
        if (!sessionManager.isLoggedIn() || sessionManager.getCurrentUser() == null) {
            showMessage("Aucun utilisateur connecté.");
            setDisableForm(true);
            return;
        }

        User sessionUser = sessionManager.getCurrentUser();
        currentUser = userService.findById(sessionUser.getId());
        if (currentUser == null) {
            currentUser = sessionUser;
        }

        fillGeneralData(currentUser);
        configureRoleSections(currentUser.getRole_utilisateur());
        loadRoleData(currentUser);
    }

    private void fillGeneralData(User user) {
        fieldEmail.setText(safe(user.getEmail()));
        fieldNom.setText(safe(user.getNom()));
        fieldPrenom.setText(safe(user.getPrenom()));
        fieldTelephone.setText(safe(user.getTelephone()));
        if (user.getGouvernorat() != null) {
            fieldGouvernorat.setValue(user.getGouvernorat());
        }
        fieldPhoto.setText(safe(user.getPhoto()));

        // Load and display user photo
        String photoPath = user.getPhoto();
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            try {
                // Check if it's a local file path
                File photoFile = fileUploadService.getFile(photoPath);
                if (photoFile != null) {
                    Image image = new Image(photoFile.toURI().toString());
                    imageViewPhoto.setImage(image);
                    lblPhotoFileName.setText(photoFile.getName());
                } else {
                    // Try loading as URL
                    Image image = new Image(photoPath);
                    imageViewPhoto.setImage(image);
                    lblPhotoFileName.setText(photoPath);
                }
            } catch (Exception e) {
                System.err.println("Failed to load photo: " + e.getMessage());
            }
        }

        if (user.getDate_naissance() != null) {
            fieldDateNaissance.setValue(user.getDate_naissance().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
        }
    }

    private void configureRoleSections(String role) {
        String normalized = role == null ? "" : role.trim().toLowerCase();
        lblRole.setText("Rôle : " + (role == null || role.isBlank() ? "non défini" : role));
        boolean isApprenant = "apprenant".equals(normalized);
        boolean isFormateur = "formateur".equals(normalized);

        apprenantSection.setVisible(isApprenant);
        apprenantSection.setManaged(isApprenant);
        formateurSection.setVisible(isFormateur);
        formateurSection.setManaged(isFormateur);
    }

    private void loadRoleData(User user) {
        String role = user.getRole_utilisateur() == null ? "" : user.getRole_utilisateur().trim().toLowerCase();
        if ("apprenant".equals(role)) {
            currentApprenant = apprenantService.findByUserId(user.getId());
            if (currentApprenant != null) {
                comboGenre.setValue(currentApprenant.getGenre());
                comboEtatCivil.setValue(currentApprenant.getEtat_civil());
                fieldObjectif.setText(safe(currentApprenant.getObjectif()));
                fieldDomainesInteret.setText(safe(currentApprenant.getDomaines_interet()));
            }
        } else if ("formateur".equals(role)) {
            currentFormateur = formateurService.findByUserId(user.getId());
            if (currentFormateur != null) {
                fieldSpecialite.setText(safe(currentFormateur.getSpecialite()));
                fieldBio.setText(safe(currentFormateur.getBio()));
                spinnerExperience.getValueFactory().setValue(
                    currentFormateur.getExperience_annees() == null ? 0 : currentFormateur.getExperience_annees()
                );
                fieldLinkedin.setText(safe(currentFormateur.getLinkedin()));
                fieldPortfolio.setText(safe(currentFormateur.getPortfolio()));
                fieldCv.setText(safe(currentFormateur.getCv()));
            }
        }
    }

    private void setDisableForm(boolean disable) {
        fieldEmail.setDisable(disable);
        fieldNom.setDisable(disable);
        fieldPrenom.setDisable(disable);
        fieldTelephone.setDisable(disable);
        fieldGouvernorat.setDisable(disable);
        fieldDateNaissance.setDisable(disable);
        fieldPhoto.setDisable(disable);
        btnUploadPhoto.setDisable(disable);
        apprenantSection.setDisable(disable);
        formateurSection.setDisable(disable);
    }

    private void applyGeneralChanges(User user) {
        user.setEmail(trim(fieldEmail.getText()));
        user.setNom(trim(fieldNom.getText()));
        user.setPrenom(trim(fieldPrenom.getText()));
        user.setTelephone(trim(fieldTelephone.getText()));
        String selectedGouvernorat = fieldGouvernorat.getValue();
        user.setGouvernorat(selectedGouvernorat);

        // Handle photo upload
        String photoPath = null;
        if (uploadedPhotoFile != null) {
            photoPath = fileUploadService.uploadPhoto(uploadedPhotoFile);
            uploadedPhotoFile = null; // Reset after upload
        } else {
            photoPath = trim(fieldPhoto.getText());
        }
        user.setPhoto(photoPath);

        LocalDate birthDate = fieldDateNaissance.getValue();
        if (birthDate != null) {
            Date date = Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            user.setDate_naissance(date);
        } else {
            user.setDate_naissance(null);
        }
    }

    private void saveApprenant() {
        if (currentApprenant == null) {
            currentApprenant = new Apprenant();
            currentApprenant.setUser(currentUser);
        }
        currentApprenant.setGenre(comboGenre.getValue());
        currentApprenant.setEtat_civil(comboEtatCivil.getValue());
        currentApprenant.setObjectif(trim(fieldObjectif.getText()));
        currentApprenant.setDomaines_interet(trim(fieldDomainesInteret.getText()));
        if (currentApprenant.getId() > 0) {
            apprenantService.modifier(currentApprenant);
        } else {
            apprenantService.ajouter(currentApprenant);
        }
    }

    private void saveFormateur() {
        if (currentFormateur == null) {
            currentFormateur = new Formateur();
            currentFormateur.setUser(currentUser);
        }
        currentFormateur.setSpecialite(trim(fieldSpecialite.getText()));
        currentFormateur.setBio(trim(fieldBio.getText()));
        Integer exp = spinnerExperience.getValue() == null ? 0 : spinnerExperience.getValue();
        currentFormateur.setExperience_annees(exp == 0 ? null : exp);
        currentFormateur.setLinkedin(trim(fieldLinkedin.getText()));
        currentFormateur.setPortfolio(trim(fieldPortfolio.getText()));
        currentFormateur.setCv(trim(fieldCv.getText()));
        if (currentFormateur.getId() > 0) {
            formateurService.modifier(currentFormateur);
        } else {
            formateurService.ajouter(currentFormateur);
        }
    }

    @FXML
    private void onBack() {
        if (onBack != null) {
            onBack.run();
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showMessage(String text) {
        lblMessage.setText(text);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {
        lblMessage.setText("");
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
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
        File selectedFile = fileChooser.showOpenDialog(stage);

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
    private void onChangePassword() {
        if (currentUser == null) {
            showMessage("Aucun utilisateur connecté.");
            return;
        }

        // Generate and send verification token for password change
        String token = emailVerificationService.generateAndSaveToken(currentUser.getId());
        if (token != null) {
            boolean emailSent = emailService.sendVerificationEmail(currentUser.getEmail(), currentUser.getNom(), token);
            if (emailSent) {
                showMessage("Token de vérification envoyé à votre email.");
                // Show password change section
                passwordChangeSection.setVisible(true);
                passwordChangeSection.setManaged(true);
                btnChangePassword.setVisible(false);
                btnChangePassword.setManaged(false);
            } else {
                showMessage("Erreur lors de l'envoi du token.");
            }
        } else {
            showMessage("Erreur lors de la génération du token.");
        }
    }

    @FXML
    private void onVerifyToken() {
        String token = fieldPasswordVerificationToken.getText();
        if (token == null || token.trim().isEmpty()) {
            showMessage("Veuillez entrer le token de vérification.");
            return;
        }

        // Verify token
        boolean isVerified = emailVerificationService.verifyEmail(token);
        if (isVerified) {
            showMessage("Token vérifié avec succès.");
            // Show new password section
            newPasswordSection.setVisible(true);
            newPasswordSection.setManaged(true);
            fieldPasswordVerificationToken.setDisable(true);
            btnVerifyToken.setDisable(true);
        } else {
            showMessage("Token invalide ou expiré.");
        }
    }

    @FXML
    private void onSave() {
        if (currentUser == null) {
            showMessage("Aucun utilisateur connecté.");
            return;
        }
        try {
            applyGeneralChanges(currentUser);
            userService.modifier(currentUser);

            String role = currentUser.getRole_utilisateur() == null ? "" : currentUser.getRole_utilisateur().trim().toLowerCase();
            if ("apprenant".equals(role)) {
                saveApprenant();
            } else if ("formateur".equals(role)) {
                saveFormateur();
            }

            // Handle password change if password fields are visible
            if (newPasswordSection.isVisible()) {
                String newPassword = fieldNewPassword.getText();
                String newPasswordConfirm = fieldNewPasswordConfirm.getText();

                // Validate password
                if (newPassword == null || newPassword.trim().isEmpty()) {
                    showMessage("Veuillez entrer un nouveau mot de passe.");
                    return;
                }

                if (!newPassword.equals(newPasswordConfirm)) {
                    showMessage("Les mots de passe ne correspondent pas.");
                    return;
                }

                if (newPassword.length() < 8) {
                    showMessage("Le mot de passe doit contenir au moins 8 caractères.");
                    return;
                }

                // Change password (token already verified)
                currentUser.setPassword(newPassword);
                userService.modifier(currentUser);

                // Reset password change UI
                resetPasswordChangeUI();
            }

            sessionManager.login(currentUser);
            showMessage("Profil mis à jour avec succès.");
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage() == null ? "Données invalides." : ex.getMessage());
        } catch (Exception ex) {
            showMessage("Erreur lors de la sauvegarde du profil.");
        }
    }

    private void resetPasswordChangeUI() {
        fieldNewPassword.clear();
        fieldNewPasswordConfirm.clear();
        fieldPasswordVerificationToken.clear();
        fieldPasswordVerificationToken.setDisable(false);
        btnVerifyToken.setDisable(false);
        newPasswordSection.setVisible(false);
        newPasswordSection.setManaged(false);
        passwordChangeSection.setVisible(false);
        passwordChangeSection.setManaged(false);
        btnChangePassword.setVisible(true);
        btnChangePassword.setManaged(true);
    }

    @FXML
    private void handleTwoFactor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/TwoFactorSetup.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Formini - Configuration 2FA");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            showMessage("Erreur lors de l'ouverture de la configuration 2FA.");
            e.printStackTrace();
        }
    }
}
