package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.quizService.QuizService;

import java.util.List;

public class ApprenantDashboardController implements DashboardRoleController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label statsLabel;
    
    @FXML
    private Label lblTotalFormations;
    
    @FXML
    private Label lblQuizCompletes;
    
    @FXML
    private Label lblOffresStage;
    
    @FXML
    private Label lblCertificats;
    
    @FXML
    private VBox menuContainer;
    
    @FXML
    private Button viewFormationsButton;
    
    @FXML
    private Button myInscriptionsButton;
    
    @FXML
    private Button myProgressButton;
    
    @FXML
    private Button takeQuizButton;
    
    @FXML
    private Button myCertificatesButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private Button viewOffersButton;

    private User currentUser;
    private Apprenant currentApprenant;
    private ApprenantService apprenantService;
    private QuizService quizService;

    @Override
    public void initializeDashboard(User user) {
        this.currentUser = user;
        apprenantService = new ApprenantService();
        quizService = new QuizService();
        
        // Get apprenant data
        currentApprenant = apprenantService.findByUserId(user.getId());
        
        setupDashboard();
        loadStudentInfo();
        loadStatistics();
    }

    private void setupDashboard() {
        titleLabel.setText("Tableau de Bord Apprenant");
        welcomeLabel.setText("Bienvenue, " + currentUser.getPrenom() + "!");
        
        // Setup button actions
        viewFormationsButton.setOnAction(e -> viewFormations());
        myInscriptionsButton.setOnAction(e -> viewMyInscriptions());
        myProgressButton.setOnAction(e -> viewMyProgress());
        takeQuizButton.setOnAction(e -> takeQuiz());
        myCertificatesButton.setOnAction(e -> viewMyCertificates());
        profileButton.setOnAction(e -> viewProfile());
        if (viewOffersButton != null) {
            viewOffersButton.setOnAction(e -> viewOffers());
        }
    }

    private void loadStudentInfo() {
        if (currentApprenant != null) {
            String infoText = String.format(
                "Informations:\n" +
                "Objectif: %s\n" +
                "Domaine: %s",
                currentApprenant.getObjectif() != null ? currentApprenant.getObjectif() : "Non défini",
                currentApprenant.getDomaine() != null ? currentApprenant.getDomaine().getNom() : "Non défini"
            );
            
            // You could display this info in a separate label or panel
            System.out.println(infoText);
        }
    }

    private void loadStatistics() {
        try {
            int totalFormations = 0;
            int completedQuiz = 0;
            int offresStage = 0;
            int certificats = 0;
            
            if (lblTotalFormations != null) lblTotalFormations.setText(String.valueOf(totalFormations));
            if (lblQuizCompletes != null) lblQuizCompletes.setText(String.valueOf(completedQuiz));
            if (lblOffresStage != null) lblOffresStage.setText(String.valueOf(offresStage));
            if (lblCertificats != null) lblCertificats.setText(String.valueOf(certificats));
            
            statsLabel.setText("Derniere mise a jour: " + java.time.LocalDateTime.now().toLocalTime());
        } catch (Exception e) {
            statsLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void viewFormations() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/formations/formation-list.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Catalogue des Formations");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening formations: " + e.getMessage());
            showFallbackMessage("Catalogue des formations - En cours de développement");
        }
    }

    @FXML
    private void viewMyInscriptions() {
        try {
            // TODO: Create inscriptions view
            showFallbackMessage("Mes inscriptions - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening inscriptions: " + e.getMessage());
        }
    }

    @FXML
    private void viewMyProgress() {
        try {
            // TODO: Create progress view
            showFallbackMessage("Ma progression - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening progress: " + e.getMessage());
        }
    }

    @FXML
    private void takeQuiz() {
        try {
            // TODO: Create quiz interface
            showFallbackMessage("Quiz - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening quiz: " + e.getMessage());
        }
    }

    @FXML
    private void viewMyCertificates() {
        try {
            // TODO: Create certificates view
            showFallbackMessage("Mes certificats - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening certificates: " + e.getMessage());
        }
    }

    @FXML
    private void viewProfile() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/auth/EditProfile.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Mon Profil");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening profile: " + e.getMessage());
        }
    }

    @FXML
    private void viewOffers() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/stages/offre-stage-view.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            tn.formini.controllers.stages.OffreStageViewController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Offres de Stage");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening offers: " + e.getMessage());
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
        loadStudentInfo();
    }
}
