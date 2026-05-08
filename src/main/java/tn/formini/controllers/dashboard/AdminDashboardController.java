package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.SocieteService;

import java.util.List;

public class AdminDashboardController implements DashboardRoleController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label statsLabel;
    
    @FXML
    private VBox menuContainer;
    
    @FXML
    private Button manageUsersButton;
    
    @FXML
    private Button manageApprenantsButton;
    
    @FXML
    private Button manageFormateursButton;
    
    @FXML
    private Button manageSocietesButton;
    
    @FXML
    private Button manageFormationsButton;
    
    @FXML
    private Button manageCategoriesButton;

    private User currentUser;
    private UserService userService;
    private ApprenantService apprenantService;
    private FormateurService formateurService;
    private SocieteService societeService;

    @Override
    public void initializeDashboard(User user) {
        this.currentUser = user;
        userService = new UserService();
        apprenantService = new ApprenantService();
        formateurService = new FormateurService();
        societeService = new SocieteService();
        
        setupDashboard();
        loadStatistics();
    }

    private void setupDashboard() {
        titleLabel.setText("Tableau de Bord Administrateur");
        
        // Setup button actions
        if (manageApprenantsButton != null) manageApprenantsButton.setOnAction(e -> openApprenantsManagement());
        if (manageFormateursButton != null) manageFormateursButton.setOnAction(e -> openFormateursManagement());
        if (manageSocietesButton != null) manageSocietesButton.setOnAction(e -> openSocietesManagement());
        if (manageFormationsButton != null) manageFormationsButton.setOnAction(e -> openFormationsManagement());
        
        // Buttons specific to new FXML layout
        if (manageUsersButton != null) manageUsersButton.setOnAction(e -> System.out.println("Users management - To be implemented"));
        if (manageCategoriesButton != null) manageCategoriesButton.setOnAction(e -> System.out.println("Categories management - To be implemented"));
    }

    private void loadStatistics() {
        try {
            List<User> allUsers = userService.afficher();
            List<tn.formini.entities.Users.Apprenant> apprenants = apprenantService.afficher();
            List<tn.formini.entities.Users.Formateur> formateurs = formateurService.afficher();
            List<tn.formini.entities.Users.Societe> societes = societeService.afficher();
            
            String statsText = String.format(
                "Statistiques du système:\n" +
                "Total utilisateurs: %d\n" +
                "Apprenants: %d\n" +
                "Formateurs: %d\n" +
                "Sociétés: %d",
                allUsers.size(),
                apprenants.size(),
                formateurs.size(),
                societes.size()
            );
            
            statsLabel.setText(statsText);
        } catch (Exception e) {
            statsLabel.setText("Erreur lors du chargement des statistiques");
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    
    @FXML
    private void openApprenantsManagement() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/crud/apprenant-crud.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gestion des Apprenants");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening apprenants management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openFormateursManagement() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/crud/formateur-crud.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gestion des Formateurs");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening formateurs management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openSocietesManagement() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/crud/societe-crud-cards.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gestion des Sociétés");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening societes management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openFormationsManagement() {
        // TODO: Implement formations management
        System.out.println("Formations management - To be implemented");
    }

    @FXML
    private void openEventsManagement() {
        // TODO: Implement events management
        System.out.println("Events management - To be implemented");
    }

    @FXML
    private void openReports() {
        // TODO: Implement reports
        System.out.println("Reports - To be implemented");
    }
    
    @FXML
    private void openMainMenu() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/MainMenu.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = (javafx.stage.Stage) titleLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Formini - Menu Principal");
        } catch (Exception e) {
            System.err.println("Error opening main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void refreshDashboard() {
        loadStatistics();
    }
}
