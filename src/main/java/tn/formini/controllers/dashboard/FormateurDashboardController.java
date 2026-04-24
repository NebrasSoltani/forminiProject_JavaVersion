package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.quizService.QuizService;

import java.util.List;

public class FormateurDashboardController implements DashboardRoleController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label statsLabel;
    
    @FXML
    private VBox menuContainer;
    
    @FXML
    private Button myFormationsButton;
    
    @FXML
    private Button createFormationButton;
    
    @FXML
    private Button manageLessonsButton;
    
    @FXML
    private Button manageQuizzesButton;
    
    @FXML
    private Button viewStudentsButton;
    
    @FXML
    private Button myProfileButton;
    
    @FXML
    private Button reviewsButton;

    private User currentUser;
    private Formateur currentFormateur;
    private FormateurService formateurService;
    private QuizService quizService;

    @Override
    public void initializeDashboard(User user) {
        this.currentUser = user;
        formateurService = new FormateurService();
        quizService = new QuizService();
        
        // Get formateur data
        currentFormateur = formateurService.findByUserId(user.getId());
        
        setupDashboard();
        loadFormateurInfo();
        loadStatistics();
    }

    private void setupDashboard() {
        titleLabel.setText("Tableau de Bord Formateur");
        welcomeLabel.setText("Bienvenue, " + currentUser.getPrenom() + " " + currentUser.getNom() + "!");
        
        // Setup button actions
        myFormationsButton.setOnAction(e -> viewMyFormations());
        createFormationButton.setOnAction(e -> createFormation());
        manageLessonsButton.setOnAction(e -> manageLessons());
        manageQuizzesButton.setOnAction(e -> manageQuizzes());
        viewStudentsButton.setOnAction(e -> viewStudents());
        myProfileButton.setOnAction(e -> viewMyProfile());
        reviewsButton.setOnAction(e -> viewReviews());
    }

    private void loadFormateurInfo() {
        if (currentFormateur != null) {
            String infoText = String.format(
                "Informations Formateur:\n" +
                "Spécialité: %s\n" +
                "Expérience: %s ans\n" +
                "Note moyenne: %s",
                currentFormateur.getSpecialite() != null ? currentFormateur.getSpecialite() : "Non définie",
                currentFormateur.getExperience_annees() != null ? currentFormateur.getExperience_annees().toString() : "Non spécifiée",
                currentFormateur.getNote_moyenne() != null ? currentFormateur.getNote_moyenne().toString() : "Non noté"
            );
            
            // You could display this info in a separate label or panel
            System.out.println(infoText);
        }
    }

    private void loadStatistics() {
        try {
            // Load formateur statistics
            int totalFormations = 0;
            int activeFormations = 0;
            int totalStudents = 0;
            int totalQuizzes = 0;
            
            if (currentFormateur != null) {
                // TODO: Load actual statistics from services
                // totalFormations = formationService.getFormateurFormationsCount(currentFormateur.getId());
                // activeFormations = formationService.getActiveFormationsCount(currentFormateur.getId());
                // totalStudents = formationService.getTotalStudentsCount(currentFormateur.getId());
                // totalQuizzes = quizService.getFormateurQuizzesCount(currentFormateur.getId());
            }
            
            String statsText = String.format(
                "Mes Statistiques:\n" +
                "Total formations: %d\n" +
                "Formations actives: %d\n" +
                "Total étudiants: %d\n" +
                "Total quiz: %d",
                totalFormations,
                activeFormations,
                totalStudents,
                totalQuizzes
            );
            
            statsLabel.setText(statsText);
        } catch (Exception e) {
            statsLabel.setText("Erreur lors du chargement des statistiques");
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    @FXML
    private void viewMyFormations() {
        try {
            // TODO: Create formateur formations view
            showFallbackMessage("Mes formations - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening formations: " + e.getMessage());
        }
    }

    @FXML
    private void createFormation() {
        try {
            // TODO: Create formation creation form
            showFallbackMessage("Créer une formation - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening formation creation: " + e.getMessage());
        }
    }

    @FXML
    private void manageLessons() {
        try {
            // TODO: Create lessons management view
            showFallbackMessage("Gérer les leçons - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening lessons management: " + e.getMessage());
        }
    }

    @FXML
    private void manageQuizzes() {
        try {
            // TODO: Create quiz management view
            showFallbackMessage("Gérer les quiz - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening quiz management: " + e.getMessage());
        }
    }

    @FXML
    private void viewStudents() {
        try {
            // TODO: Create students view for formateur
            showFallbackMessage("Voir les étudiants - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening students view: " + e.getMessage());
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
            stage.setTitle("Mon Profil Formateur");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening profile: " + e.getMessage());
        }
    }

    @FXML
    private void viewReviews() {
        try {
            // TODO: Create reviews view
            showFallbackMessage("Voir les avis - En cours de développement");
        } catch (Exception e) {
            System.err.println("Error opening reviews: " + e.getMessage());
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
        loadFormateurInfo();
    }
}
