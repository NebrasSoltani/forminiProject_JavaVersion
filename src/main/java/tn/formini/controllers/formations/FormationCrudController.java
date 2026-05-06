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
import tn.formini.services.formations.FormationService;
import tn.formini.utils.StageWindowMode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FormationCrudController {

    @FXML private Label headerLabel;
    @FXML private TilePane formationsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private final FormationService formationService = new FormationService();
    private int formateurId;
    private final List<Formation> currentFormations = new ArrayList<>();
    private Formation selectedFormation;

    @FXML
    public void initialize() {
        formationsGrid.setPrefColumns(3);
        formationsGrid.setHgap(16);
        formationsGrid.setVgap(16);

        sortCombo.getItems().setAll(
                "Plus recentes",
                "Titre A-Z",
                "Titre Z-A",
                "Duree croissante",
                "Duree decroissante",
                "Prix croissant",
                "Prix decroissant"
        );
        sortCombo.setValue("Plus recentes");
    }

    public void setFormateurId(int formateurId) {
        this.formateurId = formateurId;
        refreshGrid();
    }

    @FXML
    private void handleCreate() {
        openFormationForm(null);
    }

    @FXML
    private void handleEdit() {
        if (selectedFormation == null) {
            showError("Selectionnez une formation a modifier.");
            return;
        }
        openFormationForm(selectedFormation);
    }

    @FXML
    private void handleDelete() {
        if (selectedFormation == null) {
            showError("Selectionnez une formation a supprimer.");
            return;
        }
        deleteFormation(selectedFormation);
    }

    private void deleteFormation(Formation selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la formation");
        confirm.setContentText("La formation '" + selected.getTitre() + "' et ses lecons seront supprimees.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = formationService.deleteById(selected.getId());
            if (deleted) {
                refreshGrid();
            } else {
                String details = safe(formationService.getLastDeleteError(), "Cause inconnue.");
                showError("Suppression impossible: " + details);
            }
        }
    }

    @FXML
    private void handleManageLessons() {
        if (selectedFormation == null) {
            showError("Selectionnez une formation pour gerer ses lecons.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/lecon-crud.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            controller.getClass().getMethod("setFormation", Formation.class).invoke(controller, selectedFormation);

            Stage stage = new Stage();
            stage.setTitle("Lecons - " + selectedFormation.getTitre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshGrid();
        } catch (Exception ex) {
            showError("Impossible d'ouvrir la gestion des lecons: " + ex.getMessage());
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
            sortCombo.setValue("Plus recentes");
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
        List<Formation> formations = formationService.findByFormateurId(formateurId);
        currentFormations.clear();
        currentFormations.addAll(formations);
        selectedFormation = null;
        applyFiltersAndSorting();
    }

    private void applyFiltersAndSorting() {
        String raw = searchField == null ? "" : searchField.getText();
        String query = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);

        List<Formation> filtered = currentFormations.stream()
                .filter(f -> matchesQuery(f, query))
                .sorted(buildComparator())
                .toList();

        formationsGrid.getChildren().clear();

        for (Formation formation : filtered) {
            formationsGrid.getChildren().add(createCard(formation));
        }

        headerLabel.setText("Mes formations (" + filtered.size() + "/" + currentFormations.size() + ")");
    }

    private boolean matchesQuery(Formation formation, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return safe(formation.getTitre(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(formation.getDescription_courte(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(formation.getCategorie(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(formation.getNiveau(), "").toLowerCase(Locale.ROOT).contains(query)
                || safe(formation.getLangue(), "").toLowerCase(Locale.ROOT).contains(query);
    }

    private Comparator<Formation> buildComparator() {
        String sortValue = sortCombo == null || sortCombo.getValue() == null ? "Plus recentes" : sortCombo.getValue();
        return switch (sortValue) {
            case "Titre A-Z" -> Comparator.comparing(f -> safe(f.getTitre(), "").toLowerCase(Locale.ROOT));
            case "Titre Z-A" -> Comparator.comparing((Formation f) -> safe(f.getTitre(), "").toLowerCase(Locale.ROOT)).reversed();
            case "Duree croissante" -> Comparator.comparingInt(Formation::getDuree);
            case "Duree decroissante" -> Comparator.comparingInt(Formation::getDuree).reversed();
            case "Prix croissant" -> Comparator.comparing(f -> f.getPrix() == null ? java.math.BigDecimal.ZERO : f.getPrix());
            case "Prix decroissant" -> Comparator.comparing((Formation f) -> f.getPrix() == null ? java.math.BigDecimal.ZERO : f.getPrix()).reversed();
            default -> Comparator.comparingInt(Formation::getId).reversed();
        };
    }

    private VBox createCard(Formation formation) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setPrefWidth(280);
        card.setMinHeight(320);
        card.setUserData(formation.getId());
        card.getStyleClass().add("formation-card");

        ImageView cover = new ImageView(loadCoverImage(formation.getImage_couverture()));
        cover.setFitWidth(256);
        cover.setFitHeight(140);
        cover.setPreserveRatio(false);
        cover.setSmooth(true);
        cover.getStyleClass().add("formation-card-image");

        Label title = new Label(safe(formation.getTitre(), "Sans titre"));
        title.setWrapText(true);
        title.getStyleClass().add("formation-card-title");

        Label shortDesc = new Label(truncate(safe(formation.getDescription_courte(), "Aucune description"), 110));
        shortDesc.setWrapText(true);
        shortDesc.getStyleClass().add("formation-card-desc");
        VBox.setVgrow(shortDesc, Priority.ALWAYS);

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("formation-card-edit");
        editButton.setOnAction(e -> {
            selectedFormation = formation;
            updateCardSelectionStyles();
            openFormationForm(formation);
        });

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("formation-card-delete");
        deleteButton.setOnAction(e -> {
            selectedFormation = formation;
            updateCardSelectionStyles();
            deleteFormation(formation);
        });

        HBox actions = new HBox(8, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(cover, title, shortDesc, actions);
        card.setOnMouseClicked(event -> {
            selectedFormation = formation;
            updateCardSelectionStyles();
        });

        return card;
    }

    private void updateCardSelectionStyles() {
        for (javafx.scene.Node node : formationsGrid.getChildren()) {
            if (!(node instanceof VBox card)) {
                continue;
            }
            card.getStyleClass().remove("formation-card-selected");

            if (selectedFormation != null
                    && card.getUserData() instanceof Integer cardId
                    && cardId == selectedFormation.getId()) {
                card.getStyleClass().add("formation-card-selected");
            }
        }
    }

    private void openFormationForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/formation-form.fxml"));
            Parent root = loader.load();

            FormationFormController controller = loader.getController();
            controller.setFormateurId(formateurId);
            controller.setOnSaved(this::refreshGrid);
            if (formation != null) {
                controller.setFormation(formation);
            }

            Stage stage = new Stage();
            stage.setTitle(formation == null ? "Nouvelle formation" : "Modifier formation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            StageWindowMode.skipAutoMaximize(stage);
            stage.showAndWait();
        } catch (IOException ex) {
            showError("Impossible d'ouvrir le formulaire formation: " + ex.getMessage());
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
