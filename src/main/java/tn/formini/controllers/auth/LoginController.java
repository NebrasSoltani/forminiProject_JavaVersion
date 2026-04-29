package tn.formini.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import tn.formini.controllers.frontend.FrontMainController;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.LoginService;
import tn.formini.services.UsersService.RememberMeService;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.services.auth.OAuthCallbackHandler;
import tn.formini.services.auth.TurnstileService;
import tn.formini.services.face.CameraCaptureService;
import tn.formini.services.face.FaceRecognitionService;
import tn.formini.utils.OAuthConfig;
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
    
    @FXML
    private Button btnFaceLogin;
    
    @FXML
    private ImageView cameraView;
    
    @FXML
    private VBox cameraPanel;
    
    @FXML
    private Button btnStartCamera;
    
    @FXML
    private Button btnStopCamera;
    
    @FXML
    private Button btnCaptureFace;

    @FXML
    private WebView turnstileWebView;

    @FXML
    private VBox turnstileContainer;

    @FXML
    private Label turnstileError;

    private LoginService loginService;
    private SessionManager sessionManager;
    private RememberMeService rememberMeService;
    private Runnable onBack;
    private Preferences prefs;
    private CameraCaptureService cameraService;
    private FaceRecognitionService faceService;
    private boolean cameraActive = false;
    private String turnstileToken = null;
    private boolean turnstileVerified = false;

    @FXML
    public void initialize() {
        loginService = new LoginService();
        sessionManager = SessionManager.getInstance();
        rememberMeService = new RememberMeService();
        prefs = Preferences.userNodeForPackage(LoginController.class);
        cameraService = CameraCaptureService.getInstance();
        faceService = FaceRecognitionService.getInstance();
        
        // Load saved credentials if remember me was checked
        loadSavedCredentials();
        
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
        
        // Handle remember me checkbox changes
        cbRememberMe.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                rememberMeService.clearCredentials();
            }
        });
        
        // Hide camera panel initially
        if (cameraPanel != null) {
            cameraPanel.setVisible(false);
            cameraPanel.setManaged(false);
        }

        // Initialize Turnstile widget
        initializeTurnstile();
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

        // Verify Turnstile before authentication
        if (!turnstileVerified || turnstileToken == null) {
            showTurnstileError("Veuillez compléter la vérification de sécurité Cloudflare.");
            return;
        }

        // Verify Turnstile token with Cloudflare
        if (!TurnstileService.verifyToken(turnstileToken)) {
            showTurnstileError("La vérification de sécurité a échoué. Veuillez réessayer.");
            turnstileToken = null;
            turnstileVerified = false;
            initializeTurnstile(); // Reload the widget
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

            // Check if 2FA is enabled
            if (user.isGoogle_auth_enabled()) {
                // Navigate to 2FA verification
                navigateToTwoFactorVerification(user, cbRememberMe.isSelected());
                return;
            }

            // Create session
            sessionManager.login(user);
            // Handle remember me functionality
            if (cbRememberMe.isSelected()) {
                rememberMeService.saveCredentials(user.getEmail(), "");
            } else {
                rememberMeService.clearCredentials();
            }
            
            // Save remember me preference
            if (cbRememberMe.isSelected()) {
                prefs.putBoolean("rememberMe", true);
                prefs.put("rememberedEmail", user.getEmail());
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

    private void navigateToTwoFactorVerification(User user, boolean rememberMe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/TwoFactorVerification.fxml"));
            Parent root = loader.load();

            TwoFactorVerificationController controller = loader.getController();
            controller.setUser(user);
            controller.setRememberMe(rememberMe);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.setTitle("Formini - Vérification 2FA");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la page de vérification 2FA.");
            e.printStackTrace();
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }
    
    /**
     * Load saved credentials if remember me was previously checked
     */
    private void loadSavedCredentials() {
        // Load from RememberMeService
        if (rememberMeService.hasSavedCredentials()) {
            String savedEmail = rememberMeService.getSavedEmail();
            if (savedEmail != null && !savedEmail.isEmpty()) {
                fieldEmail.setText(savedEmail);
                cbRememberMe.setSelected(true);
                System.out.println("Identifiants sauvegardés chargés pour la connexion automatique");
            }
        }
        
        // Also load from preferences
        boolean rememberMe = prefs.getBoolean("rememberMe", false);
        if (rememberMe) {
            String rememberedEmail = prefs.get("rememberedEmail", "");
            if (!rememberedEmail.isEmpty()) {
                fieldEmail.setText(rememberedEmail);
                cbRememberMe.setSelected(true);
            }
        }
    }

    private void saveRememberedCredentials(String email, String password) {
        prefs.putBoolean("rememberMe", true);
        prefs.put("rememberedEmail", email);
        prefs.put("password", password);
    }

    private void clearRememberedCredentials() {
        prefs.putBoolean("rememberMe", false);
        prefs.remove("email");
        prefs.remove("password");
    }

    private void initializeTurnstile() {
        if (turnstileWebView == null) {
            return;
        }

        String siteKey = OAuthConfig.getTurnstileSiteKey();
        if (siteKey == null || siteKey.isEmpty() || siteKey.equals("YOUR_TURNSTILE_SITE_KEY")) {
            System.err.println("Turnstile site key not configured");
            return;
        }

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Turnstile Verification</title>\n" +
                "    <script src='https://challenges.cloudflare.com/turnstile/v0/api.js' async defer></script>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; overflow: hidden; }\n" +
                "        .cf-turnstile { margin: 0 auto; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='cf-turnstile' data-sitekey='" + siteKey + "' data-callback='turnstileCallback' data-theme='light'></div>\n" +
                "    <script>\n" +
                "        function turnstileCallback(token) {\n" +
                "            // Send token to JavaFX application\n" +
                "            window.javaBridge.setTurnstileToken(token);\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        turnstileWebView.getEngine().loadContent(htmlContent);

        // Set up Java-JavaScript bridge
        turnstileWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject jsObject = (JSObject) turnstileWebView.getEngine().executeScript("window");
                jsObject.setMember("javaBridge", new TurnstileBridge());
            }
        });
    }

    private void showTurnstileError(String message) {
        turnstileError.setText(message);
        turnstileError.setVisible(true);
        turnstileError.setManaged(true);
    }

    // Bridge class to communicate between JavaScript and Java
    public class TurnstileBridge {
        public void setTurnstileToken(String token) {
            turnstileToken = token;
            turnstileVerified = true;
            javafx.application.Platform.runLater(() -> {
                turnstileError.setVisible(false);
                turnstileError.setManaged(false);
                System.out.println("Turnstile verification completed successfully");
            });
        }
    }
    
    @FXML
    public void onFaceLogin(ActionEvent event) {
        if (!faceService.isInitialized()) {
            showError("Service de reconnaissance faciale non initialisé. Veuillez vérifier l'installation d'OpenCV.");
            return;
        }
        
        // Show camera panel
        if (cameraPanel != null) {
            cameraPanel.setVisible(true);
            cameraPanel.setManaged(true);
        }
        
        showInfo("Cliquez sur 'Démarrer la caméra' pour commencer la reconnaissance faciale.");
    }
    
    @FXML
    public void onStartCamera(ActionEvent event) {
        if (cameraActive) {
            showInfo("La caméra est déjà active.");
            return;
        }
        
        if (!cameraService.isCameraAvailable()) {
            showError("Aucune caméra détectée.");
            return;
        }
        
        boolean started = cameraService.startCamera(0, cameraView);
        if (started) {
            cameraActive = true;
            showInfo("Caméra démarrée. Positionnez votre visage devant la caméra.");
        } else {
            showError("Impossible de démarrer la caméra.");
        }
    }
    
    @FXML
    public void onStopCamera(ActionEvent event) {
        if (!cameraActive) {
            return;
        }
        
        cameraService.stopCamera();
        cameraActive = false;
        showInfo("Caméra arrêtée.");
    }
    
    @FXML
    public void onCaptureFace(ActionEvent event) {
        if (!cameraActive) {
            showError("Veuillez d'abord démarrer la caméra.");
            return;
        }
        
        // Capture frame
        java.io.File capturedImage = cameraService.captureFrame();
        if (capturedImage == null) {
            showError("Échec de la capture de l'image.");
            return;
        }
        
        // Extract face encoding
        byte[] faceEncoding = faceService.extractFaceEncoding(capturedImage.getAbsolutePath());
        if (faceEncoding == null) {
            showError("Aucun visage détecté dans l'image. Veuillez réessayer.");
            capturedImage.delete();
            return;
        }
        
        // Try to find matching user
        User matchedUser = findUserByFaceEncoding(faceEncoding);
        
        if (matchedUser != null) {
            // Check if face auth is enabled for this user
            if (!matchedUser.isFace_auth_enabled()) {
                showError("L'authentification faciale n'est pas activée pour ce compte.");
                capturedImage.delete();
                return;
            }
            
            // Check if account is verified and active
            if (!loginService.isAccountVerified(matchedUser)) {
                showError("Veuillez vérifier votre adresse email.");
                capturedImage.delete();
                return;
            }
            
            if (!loginService.isAccountActive(matchedUser)) {
                showError("Votre compte a été désactivé.");
                capturedImage.delete();
                return;
            }
            
            // Create session
            sessionManager.login(matchedUser);
            
            // Stop camera
            cameraService.stopCamera();
            cameraActive = false;
            
            // Hide camera panel
            if (cameraPanel != null) {
                cameraPanel.setVisible(false);
                cameraPanel.setManaged(false);
            }
            
            showSuccess("Connexion par reconnaissance faciale réussie !");
            navigateToEditProfile();
            
        } else {
            showError("Aucun compte correspondant trouvé. Veuillez vous inscrire ou utiliser votre email/mot de passe.");
        }
        
        capturedImage.delete();
    }
    
    private User findUserByFaceEncoding(byte[] encoding) {
        // This is a simplified implementation
        // In a real application, you would query the database for all users with face encodings
        // and compare them using the face service
        
        // For now, return null - this needs to be implemented with database integration
        // You would need to:
        // 1. Query database for users with face_auth_enabled = true
        // 2. For each user, compare their face_encoding with the captured encoding
        // 3. Return the user if similarity threshold is met
        
        return null;
    }
}
