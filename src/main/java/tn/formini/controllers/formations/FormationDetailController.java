package tn.formini.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.services.formations.InscriptionService;

import java.io.File;
import java.net.URL;

public class FormationDetailController {

    @FXML
    private Label titreLabel;

    @FXML
    private Label categorieLabel;

    @FXML
    private Label niveauLabel;

    @FXML
    private Label langueLabel;

    @FXML
    private Label dureeLabel;

    @FXML
    private Label prixLabel;

    @FXML
    private Label statutInscriptionLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private ImageView coverImageView;

    @FXML
    private Button inscriptionButton;

    @FXML
    private Button commencerButton;

    private final InscriptionService inscriptionService = new InscriptionService();

    private Formation formation;
    private User currentUser;

    public void setFormation(Formation formation) {
        this.formation = formation;
        refreshView();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        refreshInscriptionState();
    }

    @FXML
    public void initialize() {
        refreshView();
    }

    @FXML
    private void handleInscription() {
        if (!isUserReady() || formation == null) {
            return;
        }

        boolean ok = inscriptionService.inscrire(currentUser.getId(), formation.getId());
        if (!ok) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Inscription impossible pour le moment.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Inscription", "Inscription effectuee avec succes.");
        refreshInscriptionState();
    }

    @FXML
    private void handleCommencer() {
        if (!isUserReady() || formation == null) {
            return;
        }

        if (!inscriptionService.isAlreadyInscrit(currentUser.getId(), formation.getId())) {
            showAlert(Alert.AlertType.WARNING, "Acces refuse", "Inscrivez-vous d'abord a cette formation.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/formation-learning.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            controller.getClass().getMethod("setCurrentUser", User.class).invoke(controller, currentUser);
            controller.getClass().getMethod("setFormation", Formation.class).invoke(controller, formation);

            Stage stage = new Stage();
            stage.setTitle("Apprentissage - " + formation.getTitre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le parcours de formation: " + ex.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        if (titreLabel.getScene() != null) {
            titreLabel.getScene().getWindow().hide();
        }
    }

    private void refreshView() {
        if (formation == null || titreLabel == null) {
            return;
        }

        titreLabel.setText(safe(formation.getTitre(), "Sans titre"));
        categorieLabel.setText("Categorie: " + safe(formation.getCategorie(), "Non definie"));
        niveauLabel.setText("Niveau: " + safe(formation.getNiveau(), "-"));
        langueLabel.setText("Langue: " + safe(formation.getLangue(), "-"));
        dureeLabel.setText("Duree: " + formation.getDuree() + " h");
        prixLabel.setText("Prix: " + (formation.getPrix() == null ? "Gratuit" : formation.getPrix().toPlainString() + " TND"));
        descriptionLabel.setText(safe(formation.getDescription_detaillee(), safe(formation.getDescription_courte(), "Aucune description.")));
        coverImageView.setImage(loadCoverImage(formation.getImage_couverture()));

        refreshInscriptionState();
    }

    private void refreshInscriptionState() {
        if (statutInscriptionLabel == null || inscriptionButton == null || commencerButton == null || formation == null) {
            return;
        }

        boolean inscrit = currentUser != null && inscriptionService.isAlreadyInscrit(currentUser.getId(), formation.getId());
        statutInscriptionLabel.setText(inscrit ? "Vous etes deja inscrit." : "Vous n'etes pas encore inscrit.");
        inscriptionButton.setDisable(inscrit);
        commencerButton.setDisable(!inscrit);
    }

    private boolean isUserReady() {
        if (currentUser == null || currentUser.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Session", "Utilisateur non connecte.");
            return false;
        }
        return true;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Image loadCoverImage(String imagePath) {
        String resolved = resolveImagePath(imagePath);
        if (resolved != null) {
            try {
                Image img = new Image(resolved, true);
                if (!img.isError()) {
                    return img;
                }
            } catch (Exception ignored) {
                // Use fallback image.
            }
        }

        URL fallbackUrl = getClass().getResource("/images/no-image-placeholder.png");
        if (fallbackUrl != null) {
            return new Image(fallbackUrl.toExternalForm(), true);
        }
        return new WritableImage(1, 1);
    }

    private String resolveImagePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return null;
        }
        String path = rawPath.trim();
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:")) {
            return path;
        }

        File file = new File(path);
        if (file.exists()) {
            return file.toURI().toString();
        }

        URL resource = path.startsWith("/") ? getClass().getResource(path) : getClass().getResource("/" + path);
        if (resource != null) {
            return resource.toExternalForm();
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}


