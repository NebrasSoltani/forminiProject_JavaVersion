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

    @FXML
    public void initialize() {
        loginService = new LoginService();
        sessionManager = SessionManager.getInstance();
        
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
                showError("Veuillez vérifier votre adresse email avant de vous connecter.");
                return;
            }
            
            // Check if account is active
            if (!loginService.isAccountActive(user)) {
                showError("Votre compte a été désactivé. Veuillez contacter l'administrateur.");
                return;
            }
            
            // Create session
            sessionManager.login(user);
            
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
        // TODO: Implement password recovery
        showInfo("Fonctionnalité de récupération de mot de passe bientôt disponible.");
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

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }
}
