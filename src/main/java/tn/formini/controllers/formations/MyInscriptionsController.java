package tn.formini.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.controllers.MainController;
import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.services.formations.InscriptionService;

import java.io.File;
import java.net.URL;
import java.util.List;

public class MyInscriptionsController {

    @FXML private TilePane formationsGrid;
    @FXML private Label resultCountLabel;
    @FXML private VBox emptyState;

    private final InscriptionService inscriptionService = new InscriptionService();
    private MainController mainController;
    private User currentUser;

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadMyInscriptions();
    }

    @FXML
    public void initialize() {
        formationsGrid.setPrefColumns(3);
        formationsGrid.setHgap(20);
        formationsGrid.setVgap(20);
    }

    @FXML
    private void handleRefresh() {
        loadMyInscriptions();
    }

    @FXML
    private void goToCatalogue() {
        if (mainController != null) {
            // Logic to go to catalogue if needed
        }
    }

    private void loadMyInscriptions() {
        if (currentUser == null) return;
        
        List<Formation> myFormations = inscriptionService.findFormationsByApprenant(currentUser.getId());
        renderGrid(myFormations);
        
        if (myFormations.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            formationsGrid.setVisible(false);
            formationsGrid.setManaged(false);
            resultCountLabel.setText("Aucune inscription");
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            formationsGrid.setVisible(true);
            formationsGrid.setManaged(true);
            resultCountLabel.setText(myFormations.size() + " formation(s) en cours");
        }
    }

    private void renderGrid(List<Formation> formations) {
        formationsGrid.getChildren().clear();
        for (Formation formation : formations) {
            formationsGrid.getChildren().add(createCard(formation));
        }
    }

    private VBox createCard(Formation formation) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5); -fx-cursor: hand;");

        ImageView cover = new ImageView(loadCoverImage(formation.getImage_couverture()));
        cover.setFitWidth(270);
        cover.setFitHeight(150);
        cover.setPreserveRatio(false);
        
        Label title = new Label(formation.getTitre());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPrefHeight(50);

        Label category = new Label(formation.getCategorie().toUpperCase());
        category.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-padding: 5 10; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;");

        Button studyButton = new Button("🚀 Continuer l'apprentissage");
        studyButton.setMaxWidth(Double.MAX_VALUE);
        studyButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        studyButton.setOnAction(e -> startLearning(formation));

        card.getChildren().addAll(cover, category, title, studyButton);
        card.setOnMouseClicked(e -> startLearning(formation));
        
        return card;
    }

    private void startLearning(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/formation-learning.fxml"));
            Parent root = loader.load();

            FormationLearningController controller = loader.getController();
            controller.setFormation(formation);
            controller.setCurrentUser(currentUser); // FIXED METHOD NAME HERE

            Stage stage = new Stage();
            stage.setTitle("Étude - " + formation.getTitre());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Image loadCoverImage(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                return new Image(imagePath, true);
            } catch (Exception ignored) {}
        }
        URL fallbackUrl = getClass().getResource("/images/no-image-placeholder.png");
        return fallbackUrl != null ? new Image(fallbackUrl.toExternalForm(), true) : new WritableImage(1, 1);
    }
}
