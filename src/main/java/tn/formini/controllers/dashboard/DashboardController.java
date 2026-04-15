package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SessionManager;

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
            FXMLLoader loader;
            
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
                default:
                    redirectToLogin();
                    return;
            }
            
            if (loader != null) {
                Parent dashboardContent = loader.load();
                contentArea.getChildren().setAll(dashboardContent);
                
                // Initialize the specific dashboard controller
                Object controller = loader.getController();
                if (controller instanceof DashboardRoleController) {
                    ((DashboardRoleController) controller).initializeDashboard(currentUser);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            redirectToLogin();
        }
    }

    private String getRoleDisplayName(String role) {
        if (role == null) return "Inconnu";
        
        switch (role.toLowerCase()) {
            case "admin":
                return "Administrateur";
            case "formateur":
                return "Formateur";
            case "apprenant":
                return "Apprenant";
            case "societe":
                return "Société";
            default:
                return role;
        }
    }

    private void redirectToLogin() {
        try {
            // Clear session
            sessionManager.logout();
            
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/Login.fxml"));
            Parent root = loader.load();
            
            // Try to get the current stage, fallback to creating a new one
            Stage stage = null;
            if (welcomeLabel != null && welcomeLabel.getScene() != null) {
                stage = (Stage) welcomeLabel.getScene().getWindow();
            }
            
            if (stage == null) {
                // Create a new stage if we can't get the current one
                stage = new Stage();
            }
            
            stage.setScene(new Scene(root));
            stage.setTitle("Formini - Connexion");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error redirecting to login: " + e.getMessage());
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
            System.err.println("Error opening profile: " + e.getMessage());
        }
    }
}
