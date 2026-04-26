package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SessionManager;

@Component
public class DashboardController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label userRoleLabel;
    
    @FXML
    private Label userInfoLabel;
    
    @FXML
    private VBox contentArea;

    private SessionManager sessionManager;
    private User currentUser;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        
        currentUser = sessionManager.getCurrentUser();
        loadUserDashboard();
    }

    private void loadUserDashboard() {
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Set user information
        welcomeLabel.setText("Bienvenue, " + currentUser.getPrenom() + " " + currentUser.getNom() + "!");
        userRoleLabel.setText("Rôle: " + getRoleDisplayName(currentUser.getRole_utilisateur()));
        userInfoLabel.setText("Email: " + currentUser.getEmail());

        // Redirect to appropriate dashboard based on role
        try {
            String role = currentUser.getRole_utilisateur().toLowerCase();
            FXMLLoader loader = null;
            
            switch (role) {
                case "admin":
                    loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/admin-dashboard.fxml"));
                    break;
                case "formateur":
                    loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/formateur-dashboard.fxml"));
                    break;
                case "apprenant":
                    loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/apprenant-dashboard.fxml"));
                    break;
                case "societe":
                    loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/societe-dashboard.fxml"));
                    break;
            }
            
            if (loader != null) {
                Parent dashboardContent = loader.load();
                contentArea.getChildren().setAll(dashboardContent);
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getRoleDisplayName(String role) {
        if (role == null) return "Inconnu";
        switch (role.toLowerCase()) {
            case "admin": return "Administrateur";
            case "formateur": return "Formateur";
            case "apprenant": return "Apprenant";
            case "societe": return "Société";
            default: return role;
        }
    }

    private void redirectToLogin() {
        try {
            sessionManager.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = null;
            if (welcomeLabel != null && welcomeLabel.getScene() != null) {
                stage = (Stage) welcomeLabel.getScene().getWindow();
            }
            if (stage == null) stage = new Stage();
            
            stage.setScene(new Scene(root));
            stage.setTitle("Formini - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        redirectToLogin();
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/EditProfile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Mon Profil");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
