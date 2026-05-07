package tn.formini.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.services.formations.FormationService;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormationListController {

    @FXML
    private TextField searchField;

    @FXML
    private Label resultCountLabel;

    @FXML
    private TilePane formationsGrid;

    private final FormationService formationService = new FormationService();
    private List<Formation> allFormations = new ArrayList<>();
    private User currentUser;

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        formationsGrid.setPrefColumns(3);
        formationsGrid.setHgap(16);
        formationsGrid.setVgap(16);
        loadFormations();
    }

    @FXML
    private void handleSearch() {
        String raw = searchField == null ? "" : searchField.getText();
        String query = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);

        List<Formation> filtered = allFormations.stream()
                .filter(f -> matchesQuery(f, query))
                .toList();
        renderGrid(filtered);
    }

    @FXML
    private void handleRefresh() {
        loadFormations();
    }

    private void loadFormations() {
        allFormations = formationService.findPublished();
        renderGrid(allFormations);
    }

    private void renderGrid(List<Formation> formations) {
        formationsGrid.getChildren().clear();
        for (Formation formation : formations) {
            formationsGrid.getChildren().add(createCard(formation));
        }
        resultCountLabel.setText(formations.size() + " formation(s)");
    }

    private VBox createCard(Formation formation) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setPrefWidth(260);
        card.setMinHeight(260);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dfe6e9; -fx-border-radius: 10; -fx-background-radius: 10;");

        ImageView cover = new ImageView(loadCoverImage(formation.getImage_couverture()));
        cover.setFitWidth(240);
        cover.setFitHeight(130);
        cover.setPreserveRatio(false);
        cover.setSmooth(true);

        Label title = new Label(safe(formation.getTitre(), "Sans titre"));
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label category = new Label("Categorie: " + safe(formation.getCategorie(), "Non definie"));
        Label level = new Label("Niveau: " + safe(formation.getNiveau(), "-"));
        Label language = new Label("Langue: " + safe(formation.getLangue(), "-"));
        Label price = new Label("Prix: " + (formation.getPrix() == null ? "Gratuit" : formation.getPrix().toPlainString() + " TND"));

        Label shortDesc = new Label(safe(formation.getDescription_courte(), ""));
        shortDesc.setWrapText(true);
        shortDesc.setStyle("-fx-text-fill: #636e72;");
        VBox.setVgrow(shortDesc, Priority.ALWAYS);

        Button detailsButton = new Button("Voir details");
        detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> {
            e.consume();
            openFormationDetail(formation);
        });

        card.setOnMouseClicked(e -> openFormationDetail(formation));
        card.setStyle(card.getStyle() + " -fx-cursor: hand;");

        card.getChildren().addAll(cover, title, category, level, language, price, shortDesc, detailsButton);
        return card;
    }

    private void openFormationDetail(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/formation-detail.fxml"));
            Parent root = loader.load();

            FormationDetailController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setFormation(formation);

            Stage stage = new Stage();
            stage.setTitle("Details - " + safe(formation.getTitre(), "Formation"));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean matchesQuery(Formation formation, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return safe(formation.getTitre(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(formation.getCategorie(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(formation.getDescription_courte(), "").toLowerCase(Locale.ROOT).contains(query);
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
                // Keep fallback if image path is invalid.
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
}





