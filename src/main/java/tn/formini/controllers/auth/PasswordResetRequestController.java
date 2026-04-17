package tn.formini.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.formini.services.UsersService.PasswordResetService;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordResetRequestController implements Initializable {

    @FXML private TextField fieldEmail;
    @FXML private Button btnSendReset;
    @FXML private Button btnBackToLogin;
    @FXML private Label errorEmail;
    @FXML private Label lblMessage;

    private final PasswordResetService passwordResetService = new PasswordResetService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideMessage();
    }

    @FXML
    private void onSendReset(ActionEvent event) {
        clearErrors();

        String email = fieldEmail.getText().trim();

        // Validate email
        if (email.isEmpty()) {
            showFieldError(errorEmail, "Email is required.");
            return;
        }

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showFieldError(errorEmail, "Invalid email format.");
            return;
        }

        // Initiate password reset
        boolean success = passwordResetService.initiatePasswordReset(email);

        if (success) {
            showSuccess("Password reset token sent to your email. Redirecting...");
            fieldEmail.clear();
            // Auto-redirect to NewPassword page after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/NewPassword.fxml"));
                            Parent root = loader.load();
                            Stage stage = (Stage) btnSendReset.getScene().getWindow();
                            if (stage.getScene() != null) {
                                stage.getScene().setRoot(root);
                            } else {
                                stage.setScene(new javafx.scene.Scene(root));
                            }
                            stage.setTitle("Formini - Set New Password");
                        } catch (Exception e) {
                            showError("Failed to navigate to new password page.");
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Failed to send reset token. Please check if the email is registered.");
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
        errorEmail.setVisible(false);
        errorEmail.setManaged(false);
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
