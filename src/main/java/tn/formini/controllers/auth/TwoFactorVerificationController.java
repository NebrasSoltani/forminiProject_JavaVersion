package tn.formini.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.services.UsersService.RememberMeService;
import tn.formini.utils.TOTPService;

public class TwoFactorVerificationController {

    @FXML
    private Label lblMessage;

    @FXML
    private TextField fieldTotpCode;

    @FXML
    private Label errorTotp;

    @FXML
    private TextField fieldBackupCode;

    @FXML
    private Label errorBackup;

    @FXML
    private VBox totpBox;

    @FXML
    private VBox backupBox;

    @FXML
    private Button btnUseBackup;

    @FXML
    private Button btnUseTotp;

    @FXML
    private Button btnCancel;

    private UserService userService;
    private TOTPService totpService;
    private SessionManager sessionManager;
    private RememberMeService rememberMeService;
    private User user;
    private boolean rememberMe;

    @FXML
    public void initialize() {
        userService = new UserService();
        totpService = new TOTPService();
        sessionManager = SessionManager.getInstance();
        rememberMeService = new RememberMeService();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @FXML
    public void onVerifyTotp(ActionEvent event) {
        clearErrors();

        String code = fieldTotpCode.getText().trim();

        if (code.isEmpty()) {
            showFieldError(errorTotp, "Le code d'authentification est obligatoire.");
            return;
        }

        if (code.length() != 6) {
            showFieldError(errorTotp, "Le code doit contenir 6 chiffres.");
            return;
        }

        // Verify the TOTP code
        String secret = user.getGoogle_authenticator_secret();
        boolean isValid = totpService.verifyCode(secret, code);

        if (isValid) {
            handleSuccessfulLogin();
        } else {
            showFieldError(errorTotp, "Code incorrect. Veuillez réessayer.");
        }
    }

    @FXML
    public void onVerifyBackup(ActionEvent event) {
        clearErrors();

        String code = fieldBackupCode.getText().trim();

        if (code.isEmpty()) {
            showFieldError(errorBackup, "Le code de récupération est obligatoire.");
            return;
        }

        if (!totpService.isValidBackupCodeFormat(code)) {
            showFieldError(errorBackup, "Format de code invalide.");
            return;
        }

        // Verify and use the backup code
        boolean isValid = userService.useBackupCode(user.getId(), code);

        if (isValid) {
            showSuccess("Code de récupération utilisé avec succès. Veuillez générer de nouveaux codes de récupération.");
            handleSuccessfulLogin();
        } else {
            showFieldError(errorBackup, "Code de récupération invalide ou déjà utilisé.");
        }
    }

    @FXML
    public void onUseBackup(ActionEvent event) {
        totpBox.setVisible(false);
        totpBox.setManaged(false);
        backupBox.setVisible(true);
        backupBox.setManaged(true);
        btnUseBackup.setVisible(false);
        btnUseBackup.setManaged(false);
        btnUseTotp.setVisible(true);
        btnUseTotp.setManaged(true);
    }

    @FXML
    public void onUseTotp(ActionEvent event) {
        backupBox.setVisible(false);
        backupBox.setManaged(false);
        totpBox.setVisible(true);
        totpBox.setManaged(true);
        btnUseTotp.setVisible(false);
        btnUseTotp.setManaged(false);
        btnUseBackup.setVisible(true);
        btnUseBackup.setManaged(true);
    }

    @FXML
    public void onCancel(ActionEvent event) {
        navigateToLogin();
    }

    private void handleSuccessfulLogin() {
        // Create session
        sessionManager.login(user);

        // Handle remember me functionality
        if (rememberMe) {
            rememberMeService.saveCredentials(user.getEmail(), "");
        } else {
            rememberMeService.clearCredentials();
        }

        showSuccess("Connexion réussie ! Redirection...");

        // Navigate to dashboard
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/main-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnCancel.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Tableau de Bord");
        } catch (Exception e) {
            showError("Erreur lors de la navigation vers le tableau de bord.");
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnCancel.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Connexion");
        } catch (Exception e) {
            showError("Erreur lors de la navigation vers la page de connexion.");
            e.printStackTrace();
        }
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showError(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("login-alert-success", "login-alert-info");
        lblMessage.getStyleClass().add("login-alert-error");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void showSuccess(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("login-alert-error", "login-alert-info");
        lblMessage.getStyleClass().add("login-alert-success");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void clearErrors() {
        errorTotp.setVisible(false);
        errorTotp.setManaged(false);
        errorBackup.setVisible(false);
        errorBackup.setManaged(false);
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
