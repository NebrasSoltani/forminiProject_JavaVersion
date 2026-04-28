package tn.formini.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.PasswordResetService;

import java.net.URL;
import java.util.ResourceBundle;

public class NewPasswordController implements Initializable {

    @FXML private TextField fieldToken;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldPasswordConfirm;
    @FXML private Button btnResetPassword;
    @FXML private Button btnBackToLogin;
    @FXML private Label errorToken;
    @FXML private Label errorPassword;
    @FXML private Label errorPasswordConfirm;
    @FXML private Label lblMessage;

    private final PasswordResetService passwordResetService = new PasswordResetService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideMessage();
    }

    @FXML
    private void onResetPassword(ActionEvent event) {
        clearErrors();

        String token = fieldToken.getText().trim();
        String password = fieldPassword.getText();
        String passwordConfirm = fieldPasswordConfirm.getText();

        // Validate token
        if (token.isEmpty()) {
            showFieldError(errorToken, "Token is required.");
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            showFieldError(errorPassword, "Password is required.");
            return;
        }

        if (password.length() < 8) {
            showFieldError(errorPassword, "Password must be at least 8 characters.");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            showFieldError(errorPassword, "Password must contain at least one uppercase letter.");
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            showFieldError(errorPassword, "Password must contain at least one lowercase letter.");
            return;
        }

        if (!password.matches(".*[0-9].*")) {
            showFieldError(errorPassword, "Password must contain at least one digit.");
            return;
        }

        // Validate password confirmation
        if (!password.equals(passwordConfirm)) {
            showFieldError(errorPasswordConfirm, "Passwords do not match.");
            return;
        }

        // Validate token
        User user = passwordResetService.validateResetToken(token);
        if (user == null) {
            showError("Invalid or expired token. Please request a new password reset.");
            return;
        }

        // Reset password
        boolean success = passwordResetService.resetPassword(user, password);
        if (success) {
            showSuccess("Password reset successfully. Redirecting to login...");
            fieldToken.clear();
            fieldPassword.clear();
            fieldPasswordConfirm.clear();
            // Auto-redirect to Login page after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> onBackToLogin(null));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Failed to reset password. Please try again.");
        }
    }

    @FXML
    private void onBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnBackToLogin.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Login");
        } catch (Exception e) {
            showError("Failed to navigate to login page.");
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

    private void hideMessage() {
        lblMessage.setText("");
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    private void clearErrors() {
        errorToken.setVisible(false);
        errorToken.setManaged(false);
        errorPassword.setVisible(false);
        errorPassword.setManaged(false);
        errorPasswordConfirm.setVisible(false);
        errorPasswordConfirm.setManaged(false);
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
