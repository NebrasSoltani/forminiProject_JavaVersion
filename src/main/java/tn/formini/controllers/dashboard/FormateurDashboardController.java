package tn.formini.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.controllers.formations.FormationCrudController;
import tn.formini.controllers.formations.FormationFormController;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.formations.FormationService;
import tn.formini.services.quizService.QuizService;

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
    private FormationService formationService;
    private QuizService quizService;

    @Override
    public void initializeDashboard(User user) {
        this.currentUser = user;
        formateurService = new FormateurService();
        formationService = new FormationService();
        quizService = new QuizService();

        currentFormateur = formateurService.findByUserId(user.getId());

        setupDashboard();
        loadFormateurInfo();
        loadStatistics();
    }

    private void setupDashboard() {
        titleLabel.setText("Tableau de Bord Formateur");
        welcomeLabel.setText("Bienvenue, " + currentUser.getPrenom() + " " + currentUser.getNom() + "!");

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
                            "Specialite: %s\n" +
                            "Experience: %s ans\n" +
                            "Note moyenne: %s",
                    currentFormateur.getSpecialite() != null ? currentFormateur.getSpecialite() : "Non definie",
                    currentFormateur.getExperience_annees() != null ? currentFormateur.getExperience_annees().toString() : "Non specifiee",
                    currentFormateur.getNote_moyenne() != null ? currentFormateur.getNote_moyenne().toString() : "Non note"
            );
            // You could display this info in a separate label or panel
            System.out.println(infoText);
        }
    }

    private void loadStatistics() {
        try {
            int totalFormations = 0;
            int activeFormations = 0;

            if (currentFormateur != null) {
                totalFormations = formationService.countByFormateurId(currentFormateur.getId());
                activeFormations = formationService
                        .findByFormateurId(currentFormateur.getId())
                        .stream()
                        .filter(f -> "publie".equalsIgnoreCase(f.getStatut()))
                        .toList()
                        .size();
            }

            String statsText = String.format(
                    "Mes Statistiques:\n" +
                            "Total formations: %d\n" +
                            "Formations publiees: %d\n" +
                            "Total quiz: en cours",
                    totalFormations,
                    activeFormations
            );

            statsLabel.setText(statsText);
        } catch (Exception e) {
            statsLabel.setText("Erreur lors du chargement des statistiques");
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    @FXML
    private void viewMyFormations() {
        if (!ensureFormateurReady()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/formation-crud.fxml"));
            Parent root = loader.load();

            FormationCrudController controller = loader.getController();
            controller.setFormateurId(currentFormateur.getId());

            openWindow("Mes formations", root, true);
            loadStatistics();
        } catch (Exception e) {
            showError("Erreur ouverture des formations: " + e.getMessage());
        }
    }

    @FXML
    private void createFormation() {
        if (!ensureFormateurReady()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/formation-form.fxml"));
            Parent root = loader.load();

            FormationFormController controller = loader.getController();
            controller.setFormateurId(currentFormateur.getId());
            controller.setOnSaved(this::loadStatistics);

            openWindow("Creer une formation", root, true);
        } catch (Exception e) {
            showError("Erreur ouverture du formulaire de formation: " + e.getMessage());
        }
    }

    @FXML
    private void manageLessons() {
        viewMyFormations();
    }

    @FXML
    private void manageQuizzes() {
        showFallbackMessage("Gestion des quiz - En cours de developpement");
    }

    @FXML
    private void viewStudents() {
        showFallbackMessage("Voir les etudiants - En cours de developpement");
    }

    @FXML
    private void viewMyProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/EditProfile.fxml"));
            Parent root = loader.load();
            openWindow("Mon Profil Formateur", root, false);
        } catch (Exception e) {
            System.err.println("Error opening profile: " + e.getMessage());
        }
    }

    @FXML
    private void viewReviews() {
        showFallbackMessage("Voir les avis - En cours de developpement");
    }

    private boolean ensureFormateurReady() {
        if (currentFormateur == null || currentFormateur.getId() <= 0) {
            showError("Votre profil formateur est introuvable. Contactez l'administrateur.");
            return false;
        }
        return true;
    }

    private void openWindow(String title, Parent root, boolean modal) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        if (modal) {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showFallbackMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
