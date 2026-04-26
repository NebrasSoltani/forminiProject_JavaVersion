package tn.formini.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.formini.controllers.frontend.FrontMainController;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.LoginService;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.services.auth.OAuthCallbackHandler;
import java.util.prefs.Preferences;


public class LoginController {

    @FXML
    private TextField fieldEmail;
    
    @FXML
    private PasswordField fieldPassword;
    
    @FXML
    private CheckBox cbRememberMe;
    
    @FXML
    private Button btnLogin;
    
    @FXML
    private Button btnForgotPassword;
    
    @FXML
    private Button btnSignup;
    
    @FXML
    private Button btnGoogleLogin;
    
    @FXML
    private Button btnGithubLogin;
    
    @FXML
    private Button btnCloudflareLogin;
    
    @FXML
    private Button btnTogglePassword;
    
    @FXML
    private Label eyeIcon;
    
    @FXML
    private Label eyeSlashIcon;
    
    @FXML
    private Label lblMessage;
    
    @FXML
    private Label errorEmail;
    
    @FXML
    private Label errorPassword;

    private LoginService loginService;
    private SessionManager sessionManager;
    private Runnable onBack;
    private Preferences prefs;

    @FXML
    public void initialize() {
        loginService = new LoginService();
        sessionManager = SessionManager.getInstance();
        prefs = Preferences.userNodeForPackage(LoginController.class);
        
        // Load saved email if remember me was checked
        loadRememberedCredentials();
        
        // Clear errors on input change
        fieldEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            errorEmail.setVisible(false);
            errorEmail.setManaged(false);
            lblMessage.setVisible(false);
            lblMessage.setManaged(false);
        });
        
        fieldPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            errorPassword.setVisible(false);
            errorPassword.setManaged(false);
            lblMessage.setVisible(false);
            lblMessage.setManaged(false);
        });
    }

    @FXML
    public void onLogin(ActionEvent event) {
        clearErrors();

        String email = fieldEmail.getText().trim();
        String password = getPasswordText();

        // Validate input
        if (!validateInput(email, password)) {
            return;
        }

        // Authenticate user
        User user = loginService.authenticate(email, password);

        if (user != null) {
            // Check if account is verified
            if (!loginService.isAccountVerified(user)) {
                // Create session temporarily for verification
                sessionManager.login(user);
                showInfo("Veuillez vérifier votre adresse email.");
                navigateToEmailVerification(user.getEmail());
                return;
            }

            // Check if account is active
            if (!loginService.isAccountActive(user)) {
                showError("Votre compte a été désactivé. Veuillez contacter l'administrateur.");
                return;
            }

            // Create session
            sessionManager.login(user);

            // Save remember me preference
            if (cbRememberMe.isSelected()) {
                saveRememberedCredentials(email, password);
            } else {
                clearRememberedCredentials();
            }

            // TODO: Update last login when database column is available
            // loginService.updateLastLogin(user.getId());

            showSuccess("Connexion réussie ! Redirection...");
            navigateToEditProfile();

        } else {
            showError("Email ou mot de passe incorrect.");
        }
    }

    @FXML
    public void onForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/PasswordResetRequest.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnForgotPassword.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Reset Password");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la page de réinitialisation.");
            e.printStackTrace();
        }
    }

    @FXML
    public void onTogglePassword(ActionEvent event) {
        HBox parent = (HBox) btnTogglePassword.getParent();
        
        // Find current password field (either PasswordField or TextField)
        javafx.scene.control.TextInputControl currentField = null;
        int fieldIndex = -1;
        
        for (int i = 0; i < parent.getChildren().size(); i++) {
            javafx.scene.Node node = parent.getChildren().get(i);
            if ((node instanceof PasswordField || node instanceof TextField) && !node.equals(btnTogglePassword)) {
                currentField = (javafx.scene.control.TextInputControl) node;
                fieldIndex = i;
                break;
            }
        }
        
        if (currentField == null) return;
        
        if (currentField instanceof PasswordField) {
            // Create TextField to show password
            TextField visiblePassword = new TextField();
            visiblePassword.setText(currentField.getText());
            visiblePassword.setPromptText(currentField.getPromptText());
            visiblePassword.getStyleClass().addAll(currentField.getStyleClass());
            visiblePassword.setStyle(currentField.getStyle());
            
            // Replace PasswordField with TextField
            parent.getChildren().set(fieldIndex, visiblePassword);
            
            // Update icons
            eyeIcon.setVisible(false);
            eyeIcon.setManaged(false);
            eyeSlashIcon.setVisible(true);
            eyeSlashIcon.setManaged(true);
            fieldPassword = null; // Clear reference
            
        } else {
            // Create PasswordField to hide password
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setText(currentField.getText());
            newPasswordField.setPromptText(currentField.getPromptText());
            newPasswordField.getStyleClass().addAll(currentField.getStyleClass());
            newPasswordField.setStyle(currentField.getStyle());
            
            // Replace TextField with PasswordField
            parent.getChildren().set(fieldIndex, newPasswordField);
            
            // Update icons and field reference
            eyeIcon.setVisible(true);
            eyeIcon.setManaged(true);
            eyeSlashIcon.setVisible(false);
            eyeSlashIcon.setManaged(false);
            fieldPassword = newPasswordField;
        }
    }

    @FXML
    public void onSignup(ActionEvent event) {
        // TODO: Navigate to signup page
        showInfo("Redirection vers la page d'inscription...");
        // You could open SignupApp here
        try {
            tn.formini.mains.SignupApp signupApp = new tn.formini.mains.SignupApp();
            Stage signupStage = new Stage();
            signupApp.start(signupStage);
            
            // Close current login window
            if (onBack != null) {
                onBack.run();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la page d'inscription: " + e.getMessage());
        }
    }

    @FXML
    public void onGoogleLogin(ActionEvent event) {
        handleOAuthLogin("google");
    }

    @FXML
    public void onGithubLogin(ActionEvent event) {
        handleOAuthLogin("github");
    }

    @FXML
    public void onCloudflareLogin(ActionEvent event) {
        handleOAuthLogin("cloudflare");
    }

    private void handleOAuthLogin(String provider) {
        // Run OAuth in a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                OAuthCallbackHandler handler = new OAuthCallbackHandler();
                User user;
                
                if (provider.equals("google")) {
                    user = handler.authenticateWithGoogle();
                } else if (provider.equals("github")) {
                    user = handler.authenticateWithGithub();
                } else {
                    user = handler.authenticateWithCloudflare();
                }
                
                if (user != null) {
                    // Update UI on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        sessionManager.login(user);
                        showSuccess("Connexion via " + provider + " réussie !");
                        navigateToEditProfile();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showError("Échec de l'authentification " + provider + ". Veuillez réessayer.");
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur lors de l'authentification: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    private String getPasswordText() {
        if (fieldPassword != null) {
            return fieldPassword.getText();
        } else {
            // Find the TextField in the password HBox
            HBox parent = (HBox) btnTogglePassword.getParent();
            for (javafx.scene.Node node : parent.getChildren()) {
                if (node instanceof TextField && !node.equals(btnTogglePassword)) {
                    return ((TextField) node).getText();
                }
            }
        }
        return "";
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;
        
        if (email.isEmpty()) {
            showFieldError(errorEmail, "L'email est obligatoire.");
            isValid = false;
        }
        
        if (password.isEmpty()) {
            showFieldError(errorPassword, "Le mot de passe est obligatoire.");
            isValid = false;
        }
        
        return isValid;
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

    private void showInfo(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("login-alert-error", "login-alert-success");
        lblMessage.getStyleClass().add("login-alert-info");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void clearErrors() {
        errorEmail.setVisible(false);
        errorEmail.setManaged(false);
        errorPassword.setVisible(false);
        errorPassword.setManaged(false);
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    private void navigateToEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/main-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Tableau de Bord");
        } catch (Exception e) {
            showError("Connexion OK, mais ouverture du tableau de bord impossible.");
            e.printStackTrace();
        }
    }

    private void navigateToEmailVerification(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/EmailVerification.fxml"));
            Parent root = loader.load();

            EmailVerificationController controller = loader.getController();
            controller.setUserEmail(email);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Vérification Email");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la page de vérification.");
            e.printStackTrace();
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    private void loadRememberedCredentials() {
        boolean rememberMe = prefs.getBoolean("rememberMe", false);
        if (rememberMe) {
            String savedEmail = prefs.get("email", "");
            String savedPassword = prefs.get("password", "");
            fieldEmail.setText(savedEmail);
            fieldPassword.setText(savedPassword);
            cbRememberMe.setSelected(true);
        }
    }

    private void saveRememberedCredentials(String email, String password) {
        prefs.putBoolean("rememberMe", true);
        prefs.put("email", email);
        prefs.put("password", password);
    }

    private void clearRememberedCredentials() {
        prefs.putBoolean("rememberMe", false);
        prefs.remove("email");
        prefs.remove("password");
    }
}
