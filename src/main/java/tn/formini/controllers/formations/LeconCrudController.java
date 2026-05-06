package tn.formini.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Lecon;
import tn.formini.services.formations.LeconService;
import tn.formini.utils.StageWindowMode;

import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeconCrudController {

    @FXML private Label headerLabel;
    @FXML private TilePane leconsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private final LeconService leconService = new LeconService();
    private Formation formation;
    private Lecon selectedLecon;
    private final List<Lecon> currentLecons = new ArrayList<>();
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/)|youtu\\.be/)([A-Za-z0-9_-]{11})"
    );

    @FXML
    public void initialize() {
        leconsGrid.setPrefColumns(3);
        leconsGrid.setHgap(16);
        leconsGrid.setVgap(16);

        sortCombo.getItems().setAll(
                "Ordre croissant",
                "Ordre decroissant",
                "Titre A-Z",
                "Titre Z-A",
                "Duree croissante",
                "Duree decroissante"
        );
        sortCombo.setValue("Ordre croissant");
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        refreshGrid();
    }

    @FXML
    private void handleAdd() {
        openLeconForm(null);
    }

    @FXML
    private void handleEdit() {
        if (selectedLecon == null) {
            showError("Selectionnez une lecon a modifier.");
            return;
        }
        openLeconForm(selectedLecon);
    }

    @FXML
    private void handleDelete() {
        if (selectedLecon == null) {
            showError("Selectionnez une lecon a supprimer.");
            return;
        }
        deleteLecon(selectedLecon);
    }

    private void deleteLecon(Lecon selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la lecon");
        confirm.setContentText("Confirmer la suppression de '" + selected.getTitre() + "' ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = leconService.deleteById(selected.getId());
            if (deleted) {
                refreshGrid();
            } else {
                String details = safe(leconService.getLastDeleteError(), "Cause inconnue.");
                showError("Suppression impossible: " + details);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        refreshGrid();
    }

    @FXML
    private void handleSearch() {
        applyFiltersAndSorting();
    }

    @FXML
    private void handleSortChange() {
        applyFiltersAndSorting();
    }

    @FXML
    private void handleResetFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (sortCombo != null) {
            sortCombo.setValue("Ordre croissant");
        }
        applyFiltersAndSorting();
    }

    @FXML
    private void handleBack() {
        Window window = headerLabel != null && headerLabel.getScene() != null ? headerLabel.getScene().getWindow() : null;
        if (window instanceof Stage stage) {
            stage.close();
        }
    }

    private void refreshGrid() {
        if (formation == null) {
            headerLabel.setText("Lecons");
            selectedLecon = null;
            leconsGrid.getChildren().clear();
            return;
        }

        List<Lecon> lecons = leconService.findByFormationId(formation.getId());
        currentLecons.clear();
        currentLecons.addAll(lecons);
        selectedLecon = null;
        applyFiltersAndSorting();
    }

    private void applyFiltersAndSorting() {
        String raw = searchField == null ? "" : searchField.getText();
        String query = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);

        List<Lecon> filtered = currentLecons.stream()
                .filter(l -> matchesQuery(l, query))
                .sorted(buildComparator())
                .toList();

        leconsGrid.getChildren().clear();

        for (Lecon lecon : filtered) {
            leconsGrid.getChildren().add(createCard(lecon));
        }

        headerLabel.setText("Lecons de '" + formation.getTitre() + "' (" + filtered.size() + "/" + currentLecons.size() + ")");
    }

    private boolean matchesQuery(Lecon lecon, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return safe(lecon.getTitre(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(lecon.getDescription(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(lecon.getVideo_url(), "").toLowerCase(Locale.ROOT).contains(query);
    }

    private Comparator<Lecon> buildComparator() {
        String sortValue = sortCombo == null || sortCombo.getValue() == null ? "Ordre croissant" : sortCombo.getValue();
        return switch (sortValue) {
            case "Ordre decroissant" -> Comparator.comparingInt(Lecon::getOrdre).reversed();
            case "Titre A-Z" -> Comparator.comparing(l -> safe(l.getTitre(), "").toLowerCase(Locale.ROOT));
            case "Titre Z-A" -> Comparator.comparing((Lecon l) -> safe(l.getTitre(), "").toLowerCase(Locale.ROOT)).reversed();
            case "Duree croissante" -> Comparator.comparingInt(l -> l.getDuree() == null ? 0 : l.getDuree());
            case "Duree decroissante" -> Comparator.comparingInt((Lecon l) -> l.getDuree() == null ? 0 : l.getDuree()).reversed();
            default -> Comparator.comparingInt(Lecon::getOrdre);
        };
    }

    private VBox createCard(Lecon lecon) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setPrefWidth(280);
        card.setMinHeight(260);
        card.setUserData(lecon.getId());
        card.getStyleClass().add("lecon-card");

        ImageView videoPreview = new ImageView(loadVideoPreview(lecon.getVideo_url()));
        videoPreview.setFitWidth(256);
        videoPreview.setFitHeight(140);
        videoPreview.setPreserveRatio(false);
        videoPreview.setSmooth(true);
        videoPreview.getStyleClass().add("lecon-card-video");

        Label title = new Label(safe(lecon.getTitre(), "Sans titre"));
        title.setWrapText(true);
        title.getStyleClass().add("lecon-card-title");
        VBox.setVgrow(title, Priority.ALWAYS);

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("lecon-card-edit");
        editButton.setOnAction(e -> {
            selectedLecon = lecon;
            updateCardSelectionStyles();
            openLeconForm(lecon);
        });

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("lecon-card-delete");
        deleteButton.setOnAction(e -> {
            selectedLecon = lecon;
            updateCardSelectionStyles();
            deleteLecon(lecon);
        });

        Button openVideoButton = new Button("Ouvrir video");
        openVideoButton.getStyleClass().add("lecon-card-open");
        openVideoButton.setOnAction(e -> openVideo(lecon.getVideo_url()));

        HBox actions = new HBox(8, editButton, deleteButton, openVideoButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(videoPreview, title, actions);
        card.setOnMouseClicked(event -> {
            selectedLecon = lecon;
            updateCardSelectionStyles();
        });
        return card;
    }

    private void updateCardSelectionStyles() {
        for (javafx.scene.Node node : leconsGrid.getChildren()) {
            if (!(node instanceof VBox card)) {
                continue;
            }
            card.getStyleClass().remove("lecon-card-selected");
            if (selectedLecon != null
                    && card.getUserData() instanceof Integer cardId
                    && cardId == selectedLecon.getId()) {
                card.getStyleClass().add("lecon-card-selected");
            }
        }
    }

    private void openLeconForm(Lecon lecon) {
        if (formation == null) {
            showError("Formation introuvable.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/lecon-form.fxml"));
            Parent root = loader.load();

            LeconFormController controller = loader.getController();
            controller.setFormation(formation);
            controller.setOnSaved(this::refreshGrid);
            if (lecon != null) {
                controller.setLecon(lecon);
            }

            Stage stage = new Stage();
            stage.setTitle(lecon == null ? "Nouvelle lecon" : "Modifier lecon");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            StageWindowMode.skipAutoMaximize(stage);
            stage.showAndWait();
        } catch (IOException ex) {
            showError("Impossible d'ouvrir le formulaire lecon: " + ex.getMessage());
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Image loadVideoPreview(String videoUrl) {
        String youtubeId = extractYoutubeId(videoUrl);
        if (youtubeId != null) {
            String thumbnailUrl = "https://img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg";
            try {
                Image img = new Image(thumbnailUrl, true);
                if (!img.isError()) {
                    return img;
                }
            } catch (Exception ignored) {
                // Use fallback if thumbnail cannot be loaded.
            }
        }

        URL fallbackUrl = getClass().getResource("/images/no-image-placeholder.png");
        if (fallbackUrl != null) {
            return new Image(fallbackUrl.toExternalForm(), true);
        }
        return new WritableImage(1, 1);
    }

    private String extractYoutubeId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void openVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) {
            showError("Aucune URL video disponible pour cette lecon.");
            return;
        }

        String normalized = videoUrl.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                showError("Ouverture du navigateur non supportee sur cette machine.");
                return;
            }
            Desktop.getDesktop().browse(new URI(normalized));
        } catch (Exception ex) {
            showError("Impossible d'ouvrir la video: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

