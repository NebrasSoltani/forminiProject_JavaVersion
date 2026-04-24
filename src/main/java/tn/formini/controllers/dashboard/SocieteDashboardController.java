package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.entities.stages.Candidature;
import tn.formini.entities.stages.OffreStage;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.stageService.CandidatureService;
import tn.formini.services.stageService.OffreStageService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SocieteDashboardController implements DashboardRoleController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label statsLabel;
    
    @FXML
    private Label lblTotalOffres;
    
    @FXML
    private Label lblOffresActives;
    
    @FXML
    private Label lblTotalCandidatures;
    
    @FXML
    private Label lblCandidaturesEnAttente;
    
    @FXML
    private PieChart offersPieChart;
    
    @FXML
    private PieChart candidatesPieChart;
    
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
    private OffreStageService offreService = new OffreStageService();
    private CandidatureService candService = new CandidatureService();

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
        String displayedName = (currentSociete != null) ? currentSociete.getNom_societe() : currentUser.getNom();
        welcomeLabel.setText("Bienvenue, " + displayedName + "!");
        
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
            List<OffreStage> allOffres = offreService.afficher();
            List<Candidature> allCands = candService.afficher();
            
            int totalOffres = allOffres.size();
            long offresActives = allOffres.stream().filter(o -> "ouvert".equals(o.getStatut())).count();
            int totalCands = allCands.size();
            long candsEnAttente = allCands.stream().filter(c -> "en_attente".equals(c.getStatut())).count();
            
            if (lblTotalOffres != null) lblTotalOffres.setText(String.valueOf(totalOffres));
            if (lblOffresActives != null) lblOffresActives.setText(String.valueOf(offresActives));
            if (lblTotalCandidatures != null) lblTotalCandidatures.setText(String.valueOf(totalCands));
            if (lblCandidaturesEnAttente != null) lblCandidaturesEnAttente.setText(String.valueOf(candsEnAttente));
            
            offersPieChart.getData().clear();
            if (allOffres.isEmpty()) {
                offersPieChart.getData().add(new PieChart.Data("Aucune offre", 1));
            } else {
                Map<String, Long> offerStats = allOffres.stream()
                    .collect(Collectors.groupingBy(o -> o.getStatut() != null ? o.getStatut() : "inconnu", Collectors.counting()));
                offerStats.forEach((statut, count) -> 
                    offersPieChart.getData().add(new PieChart.Data(statut + " (" + count + ")", count)));
            }

            candidatesPieChart.getData().clear();
            if (allCands.isEmpty()) {
                candidatesPieChart.getData().add(new PieChart.Data("Aucune candidature", 1));
            } else {
                Map<String, Long> candStats = allCands.stream()
                    .collect(Collectors.groupingBy(c -> c.getStatut() != null ? c.getStatut() : "en_attente", Collectors.counting()));
                candStats.forEach((statut, count) -> 
                    candidatesPieChart.getData().add(new PieChart.Data(statut + " (" + count + ")", count)));
            }

            statsLabel.setText(String.format("Total: %d offres et %d candidatures", totalOffres, totalCands));
                
        } catch (Exception e) {
            e.printStackTrace();
            statsLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private TabPane mainTabPane;
    
    @FXML
    private Tab stageManagementTab;
    
    @FXML
    private tn.formini.controllers.stages.StageManagementController stageManagementController;

    @FXML
    private void viewMyOffers() {
        mainTabPane.getSelectionModel().select(stageManagementTab);
        if (stageManagementController != null) {
            stageManagementController.setSelectedTab(0);
        }
    }

    @FXML
    private void createOffer() {
        mainTabPane.getSelectionModel().select(stageManagementTab);
        if (stageManagementController != null) {
            stageManagementController.setSelectedTab(0);
        }
    }

    @FXML
    private void viewCandidates() {
        mainTabPane.getSelectionModel().select(stageManagementTab);
        if (stageManagementController != null) {
            stageManagementController.setSelectedTab(1);
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
