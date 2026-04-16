package tn.formini.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.formini.services.UsersService.EmailVerificationService;
import tn.formini.services.UsersService.LoginService;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.services.UsersService.EmailService;

public class EmailVerificationController {

    @FXML
    private TextField fieldToken;

    @FXML
    private Button btnVerify;

    @FXML
    private Button btnResend;

    @FXML
    private Button btnBack;

    @FXML
    private Label lblMessage;

    @FXML
    private Label errorToken;

    private EmailVerificationService emailVerificationService;
    private EmailService emailService;
    private LoginService loginService;
    private String userEmail;
    private Runnable onBack;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        emailVerificationService = new EmailVerificationService();
        emailService = new EmailService();
        loginService = new LoginService();
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    public void onVerify(ActionEvent event) {
        clearErrors();

        String token = fieldToken.getText().trim();

        if (token.isEmpty()) {
            showFieldError(errorToken, "Le code de vérification est obligatoire.");
            return;
        }

        // Verify the token
        boolean verified = emailVerificationService.verifyEmail(token);

        if (verified) {
            showSuccess("Email vérifié avec succès !");

            // Update session if user is logged in
            SessionManager sessionManager = SessionManager.getInstance();
            if (sessionManager.getCurrentUser() != null) {
                tn.formini.entities.Users.User user = loginService.getUserByEmail(userEmail);
                if (user != null) {
                    sessionManager.login(user);
                }
            }

            // Navigate to success or dashboard
            if (onSuccess != null) {
                onSuccess.run();
            } else {
                navigateToDashboard();
            }
        } else {
            showError("Code de vérification invalide ou expiré.");
        }
    }

    @FXML
    public void onResend(ActionEvent event) {
        if (userEmail == null || userEmail.isEmpty()) {
            showError("Email non disponible pour renvoyer le code.");
            return;
        }

        String token = emailVerificationService.resendVerificationToken(userEmail);
        if (token != null) {
            emailService.sendVerificationEmail(userEmail, "Utilisateur", token);
            showSuccess("Nouveau code envoyé avec succès !");
        } else {
            showError("Impossible de renvoyer le code. Vérifiez votre email.");
        }
    }

    @FXML
    public void onBack(ActionEvent event) {
        if (onBack != null) {
            onBack.run();
        } else {
            closeWindow();
        }
    }

    private void clearErrors() {
        errorToken.setVisible(false);
        errorToken.setManaged(false);
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
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

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/main-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnVerify.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Tableau de Bord");
        } catch (Exception e) {
            showError("Vérification réussie, mais ouverture du tableau de bord impossible.");
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }
}
