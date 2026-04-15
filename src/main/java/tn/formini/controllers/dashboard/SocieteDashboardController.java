package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SocieteService;

import java.util.List;

public class SocieteDashboardController implements DashboardRoleController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label statsLabel;
    
    @FXML
    private VBox menuContainer;
    
    @FXML
    private Button myOffersButton;
    
    @FXML
    private Button createOfferButton;
    
    @FXML
    private Button viewCandidatesButton;
    
    @FXML
    private Button myProfileButton;
    
    @FXML
    private Button searchStudentsButton;
    
    @FXML
    private Button messagesButton;

    private User currentUser;
    private Societe currentSociete;
    private SocieteService societeService;

    @Override
    public void initializeDashboard(User user) {
        this.currentUser = user;
        societeService = new SocieteService();
        
        // Get societe data
        currentSociete = societeService.findByUserId(user.getId());
        
        setupDashboard();
        loadSocieteInfo();
        loadStatistics();
    }

    private void setupDashboard() {
        titleLabel.setText("Tableau de Bord Société");
        welcomeLabel.setText("Bienvenue, " + currentSociete.getNom_societe() + "!");
        
        // Setup button actions
        myOffersButton.setOnAction(e -> viewMyOffers());
        createOfferButton.setOnAction(e -> createOffer());
        viewCandidatesButton.setOnAction(e -> viewCandidates());
        myProfileButton.setOnAction(e -> viewMyProfile());
        searchStudentsButton.setOnAction(e -> searchStudents());
        messagesButton.setOnAction(e -> viewMessages());
    }

    private void loadSocieteInfo() {
        if (currentSociete != null) {
            String infoText = String.format(
                "Informations Société:\n" +
                "Secteur: %s\n" +
                "Adresse: %s\n" +
                "Site web: %s",
                currentSociete.getSecteur() != null ? currentSociete.getSecteur() : "Non spécifié",
                currentSociete.getAdresse() != null ? currentSociete.getAdresse() : "Non spécifiée",
                currentSociete.getSite_web() != null ? currentSociete.getSite_web() : "Non spécifié"
            );
            
            // You could display this info in a separate label or panel
            System.out.println(infoText);
        }
    }

    private void loadStatistics() {
        try {
            // Load societe statistics
            int totalOffers = 0;
            int activeOffers = 0;
            int totalCandidates = 0;
            int acceptedCandidates = 0;
            
            if (currentSociete != null) {
                // TODO: Load actual statistics from services
                // totalOffers = offreStageService.getSocieteOffersCount(currentSociete.getId());
                // activeOffers = offreStageService.getActiveOffersCount(currentSociete.getId());
                // totalCandidates = candidatureService.getSocieteCandidatesCount(currentSociete.getId());
                // acceptedCandidates = candidatureService.getAcceptedCandidatesCount(currentSociete.getId());
            }
            
            String statsText = String.format(
                "Mes Statistiques:\n" +
                "Total offres: %d\n" +
                "Offres actives: %d\n" +
                "Total candidats: %d\n" +
                "Candidats acceptés: %d",
                totalOffers,
                activeOffers,
                totalCandidates,
                acceptedCandidates
            );
            
            statsLabel.setText(statsText);
        } catch (Exception e) {
            statsLabel.setText("Erreur lors du chargement des statistiques");
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    @FXML
    private void viewMyOffers() {
        try {
            // TODO: Create societe offers view
            showFallbackMessage("Mes offres de stage - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening offers: " + e.getMessage());
        }
    }

    @FXML
    private void createOffer() {
        try {
            // TODO: Create offer creation form
            showFallbackMessage("Créer une offre de stage - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening offer creation: " + e.getMessage());
        }
    }

    @FXML
    private void viewCandidates() {
        try {
            // TODO: Create candidates view
            showFallbackMessage("Voir les candidats - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening candidates view: " + e.getMessage());
        }
    }

    @FXML
    private void viewMyProfile() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/auth/EditProfile.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Profil de la Société");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening profile: " + e.getMessage());
        }
    }

    @FXML
    private void searchStudents() {
        try {
            // TODO: Create student search interface
            showFallbackMessage("Rechercher des étudiants - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening student search: " + e.getMessage());
        }
    }

    @FXML
    private void viewMessages() {
        try {
            // TODO: Create messages interface
            showFallbackMessage("Messages - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening messages: " + e.getMessage());
        }
    }

    private void showFallbackMessage(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void refreshDashboard() {
        loadStatistics();
        loadSocieteInfo();
    }
}
