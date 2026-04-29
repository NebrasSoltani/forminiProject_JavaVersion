package tn.formini.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.SessionManager;
import tn.formini.utils.TOTPService;

import java.io.File;
import java.util.List;

public class TwoFactorSetupController {

    @FXML
    private Label lblMessage;

    @FXML
    private ImageView qrCodeImage;

    @FXML
    private Label secretKeyLabel;

    @FXML
    private TextField fieldVerificationCode;

    @FXML
    private Label errorCode;

    @FXML
    private TextArea backupCodesArea;

    @FXML
    private VBox step1Box;

    @FXML
    private VBox step2Box;

    @FXML
    private VBox backupCodesBox;

    @FXML
    private Button btnShowSecret;

    @FXML
    private Button btnVerify;

    @FXML
    private Button btnCopyCodes;

    @FXML
    private Button btnFinish;

    @FXML
    private Button btnCancel;

    private UserService userService;
    private TOTPService totpService;
    private User currentUser;
    private String secretKey;
    private List<String> backupCodes;
    private String qrCodePath;

    @FXML
    public void initialize() {
        userService = new UserService();
        totpService = new TOTPService();
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Aucun utilisateur connecté. Veuillez vous connecter d'abord.");
            return;
        }

        // Check if 2FA is already enabled
        if (currentUser.isGoogle_auth_enabled()) {
            showError("L'authentification à deux facteurs est déjà activée pour ce compte.");
            return;
        }

        // Generate secret and QR code
        generateSecretAndQRCode();

        // Hide step 2 and backup codes initially
        step2Box.setVisible(false);
        step2Box.setManaged(false);
        backupCodesBox.setVisible(false);
        backupCodesBox.setManaged(false);
    }

    private void generateSecretAndQRCode() {
        try {
            // Generate TOTP secret
            com.warrenstrange.googleauth.GoogleAuthenticatorKey gAuthKey = totpService.generateSecret();
            secretKey = gAuthKey.getKey();

            // Generate QR code
            qrCodePath = System.getProperty("java.io.tmpdir") + "qrcode_" + currentUser.getId() + ".png";
            boolean qrGenerated = totpService.generateQRCodeImage(secretKey, currentUser.getEmail(), qrCodePath);

            if (qrGenerated) {
                File qrFile = new File(qrCodePath);
                Image qrImage = new Image(qrFile.toURI().toString());
                qrCodeImage.setImage(qrImage);
            } else {
                showError("Erreur lors de la génération du code QR. La clé secrète sera affichée.");
                secretKeyLabel.setText(secretKey);
                secretKeyLabel.setVisible(true);
                secretKeyLabel.setManaged(true);
                btnShowSecret.setVisible(false);
                btnShowSecret.setManaged(false);
            }

            // Generate backup codes
            backupCodes = totpService.generateBackupCodes();

        } catch (Exception e) {
            showError("Erreur lors de la génération du secret 2FA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onShowSecret(ActionEvent event) {
        secretKeyLabel.setText(secretKey);
        secretKeyLabel.setVisible(true);
        secretKeyLabel.setManaged(true);
        btnShowSecret.setVisible(false);
        btnShowSecret.setManaged(false);
    }

    @FXML
    public void onVerify(ActionEvent event) {
        clearErrors();

        String code = fieldVerificationCode.getText().trim();

        if (code.isEmpty()) {
            showFieldError(errorCode, "Le code de vérification est obligatoire.");
            return;
        }

        if (code.length() != 6) {
            showFieldError(errorCode, "Le code doit contenir 6 chiffres.");
            return;
        }

        // Verify the code
        boolean isValid = totpService.verifyCode(secretKey, code);

        if (isValid) {
            showSuccess("Code vérifié avec succès !");

            // Enable 2FA in database
            String backupCodesJson = totpService.backupCodesToJson(backupCodes);
            boolean enabled = userService.enableGoogleAuth(currentUser.getId(), secretKey, backupCodesJson);

            if (enabled) {
                // Update current user object
                currentUser.setGoogle_authenticator_secret(secretKey);
                currentUser.setBackup_codes(backupCodesJson);
                currentUser.setGoogle_auth_enabled(true);

                // Show backup codes
                showBackupCodes();
            } else {
                showError("Erreur lors de l'activation de la 2FA dans la base de données.");
            }
        } else {
            showFieldError(errorCode, "Code incorrect. Veuillez réessayer.");
        }
    }

    private void showBackupCodes() {
        // Hide step 1 and step 2
        step1Box.setVisible(false);
        step1Box.setManaged(false);
        step2Box.setVisible(false);
        step2Box.setManaged(false);

        // Show backup codes
        backupCodesBox.setVisible(true);
        backupCodesBox.setManaged(true);

        // Display backup codes
        StringBuilder codesText = new StringBuilder();
        for (int i = 0; i < backupCodes.size(); i++) {
            if (i > 0) codesText.append("\n");
            codesText.append(backupCodes.get(i));
        }
        backupCodesArea.setText(codesText.toString());
    }

    @FXML
    public void onCopyBackupCodes(ActionEvent event) {
        String codes = backupCodesArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(codes);
        clipboard.setContent(content);
        showSuccess("Codes copiés dans le presse-papiers !");
    }

    @FXML
    public void onFinish(ActionEvent event) {
        // Clean up QR code file
        if (qrCodePath != null) {
            File qrFile = new File(qrCodePath);
            if (qrFile.exists()) {
                qrFile.delete();
            }
        }

        showSuccess("Configuration 2FA terminée avec succès !");

        // Navigate back to dashboard
        navigateToDashboard();
    }

    @FXML
    public void onCancel(ActionEvent event) {
        // Clean up QR code file
        if (qrCodePath != null) {
            File qrFile = new File(qrCodePath);
            if (qrFile.exists()) {
                qrFile.delete();
            }
        }

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
        errorCode.setVisible(false);
        errorCode.setManaged(false);
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
