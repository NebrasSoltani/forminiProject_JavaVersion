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
    private Button apprenantsManagementButton;
    
    @FXML
    private Button formateursManagementButton;
    
    @FXML
    private Button societesManagementButton;
    
    @FXML
    private Button formationsManagementButton;
    
    @FXML
    private Button eventsManagementButton;
    
    @FXML
    private Button reportsButton;

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
        apprenantsManagementButton.setOnAction(e -> openApprenantsManagement());
        formateursManagementButton.setOnAction(e -> openFormateursManagement());
        societesManagementButton.setOnAction(e -> openSocietesManagement());
        formationsManagementButton.setOnAction(e -> openFormationsManagement());
        eventsManagementButton.setOnAction(e -> openEventsManagement());
        reportsButton.setOnAction(e -> openReports());
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
                getClass().getResource("/fxml/crud/societe-crud.fxml")
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

    @Override
    public void refreshDashboard() {
        loadStatistics();
    }
}
