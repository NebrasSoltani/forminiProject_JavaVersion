package tn.formini.controllers.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.FormateurService;
import tn.formini.services.UsersService.UserService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class FormateurCrudController {
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    private FlowPane cardsFlowPane;

    @FXML
    private ScrollPane cardsScrollPane;

    @FXML
    private Pagination pagination;
    
        
    @FXML
    private Button addButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;

    @FXML
    private Button searchButton;
    
    private tn.formini.controllers.MainController mainController;
    
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchScopeComboBox;

    @FXML
    private TextField specialiteFilterField;

    @FXML
    private ComboBox<String> minNoteComboBox;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortDirectionComboBox;

    @FXML
    private Button resetFiltersButton;

    @FXML
    private Button filterButton;

    @FXML
    private Button sortButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label lastUpdateLabel;
    
    private FormateurService formateurService;
    private UserService userService;
    private ObservableList<Formateur> formateurList;
    private ObservableList<Formateur> filteredFormateurList;
    private Formateur selectedFormateur;

    @FXML
    public void initialize() {
        formateurService = new FormateurService();
        userService = new UserService();
        formateurList = FXCollections.observableArrayList();
        filteredFormateurList = FXCollections.observableArrayList();
        
        setupAdvancedControls();
        setupPagination();
        loadFormateurs();
        updateUI();
    }

    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
    }

    private void setupAdvancedControls() {
        searchScopeComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "ID", "Nom complet", "Email", "Spécialité", "Bio", "LinkedIn", "Portfolio"
        ));
        minNoteComboBox.setItems(FXCollections.observableArrayList(
                "Toutes", "4+", "3+", "2+", "1+"
        ));
        sortByComboBox.setItems(FXCollections.observableArrayList(
                "ID", "Nom complet", "Email", "Spécialité", "Expérience", "Note"
        ));
        sortDirectionComboBox.setItems(FXCollections.observableArrayList(
                "Ascendant", "Descendant"
        ));

        searchScopeComboBox.setValue("Tous");
        minNoteComboBox.setValue("Toutes");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        specialiteFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        searchScopeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        minNoteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortByComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortDirectionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
    }

    
    private void loadFormateurs() {
        try {
            statusLabel.setText("Chargement...");
            List<Formateur> formateurs = formateurService.afficher();
            formateurList = FXCollections.observableArrayList(formateurs);
            applyFiltersAndSorting();
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les formateurs: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        // Update count label
        int count = filteredFormateurList.size();
        countLabel.setText("Total: " + count + " formateur" + (count > 1 ? "s" : ""));
        
        // Update last update time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }

    private VBox createPage(int pageIndex) {
        updateCardsPage(pageIndex);
        return new VBox(cardsScrollPane);
    }

    private void updateCardsPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        if (fromIndex >= filteredFormateurList.size()) {
            cardsFlowPane.getChildren().clear();
            return;
        }

        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredFormateurList.size());
        List<Formateur> pageFormateurs = filteredFormateurList.subList(fromIndex, toIndex);
        
        cardsFlowPane.getChildren().clear();
        for (Formateur formateur : pageFormateurs) {
            cardsFlowPane.getChildren().add(createFormateurCard(formateur));
        }
    }

    private VBox createFormateurCard(Formateur formateur) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12; -fx-border-color: #334155; -fx-border-width: 1; -fx-border-radius: 12; -fx-padding: 16; -fx-cursor: hand;");
        card.setPrefWidth(280);
        card.setPrefHeight(200);
        
        // Header with name and avatar
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(12);
        
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setStyle("-fx-background-color: #38bdf8; -fx-background-radius: 20; -fx-pref-width: 40; -fx-pref-height: 40;");
        
        // Load user photo with fallback to default avatar
        String photoUrl = getUserPhotoUrl(formateur);
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            try {
                javafx.scene.image.Image userImage = new javafx.scene.image.Image(photoUrl, true);
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(userImage);
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
                // Create circular clip
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
                imageView.setClip(clip);
                avatarBox.getChildren().add(imageView);
            } catch (Exception e) {
                // Fallback to emoji if image loading fails
                Label avatarLabel = new Label("👨‍🏫");
                avatarLabel.setStyle("-fx-font-size: 18px;");
                avatarBox.getChildren().add(avatarLabel);
            }
        } else {
            // Default avatar emoji
            Label avatarLabel = new Label("👨‍🏫");
            avatarLabel.setStyle("-fx-font-size: 18px;");
            avatarBox.getChildren().add(avatarLabel);
        }
        
        VBox nameBox = new VBox();
        nameBox.setSpacing(2);
        Label nameLabel = new Label(getFullName(formateur));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label emailLabel = new Label(getEmail(formateur));
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        nameBox.getChildren().addAll(nameLabel, emailLabel);
        
        headerBox.getChildren().addAll(avatarBox, nameBox);
        
        // Content with speciality and rating
        VBox contentBox = new VBox();
        contentBox.setSpacing(8);
        contentBox.setStyle("-fx-padding: 8 0;");
        
        HBox specialityBox = new HBox();
        specialityBox.setAlignment(Pos.CENTER_LEFT);
        specialityBox.setSpacing(6);
        Label specialityIcon = new Label("💼");
        specialityIcon.setStyle("-fx-font-size: 12px;");
        Label specialityLabel = new Label(nullSafe(formateur.getSpecialite()));
        specialityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e2e8f0;");
        specialityBox.getChildren().addAll(specialityIcon, specialityLabel);
        
        HBox experienceBox = new HBox();
        experienceBox.setAlignment(Pos.CENTER_LEFT);
        experienceBox.setSpacing(6);
        Label experienceIcon = new Label("📊");
        experienceIcon.setStyle("-fx-font-size: 12px;");
        Label experienceLabel = new Label(getSafeExperience(formateur) + " ans d'expérience");
        experienceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e2e8f0;");
        experienceBox.getChildren().addAll(experienceIcon, experienceLabel);
        
        HBox ratingBox = new HBox();
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.setSpacing(6);
        Label ratingIcon = new Label("⭐");
        ratingIcon.setStyle("-fx-font-size: 12px;");
        Label ratingLabel = new Label(String.format("%.1f", getSafeNote(formateur)));
        ratingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #fbbf24; -fx-font-weight: bold;");
        ratingBox.getChildren().addAll(ratingIcon, ratingLabel);
        
        contentBox.getChildren().addAll(specialityBox, experienceBox, ratingBox);
        
        // Footer with bio preview
        Label bioLabel = new Label();
        String bio = nullSafe(formateur.getBio());
        if (bio.length() > 60) {
            bio = bio.substring(0, 60) + "...";
        }
        bioLabel.setText(bio);
        bioLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        
        card.getChildren().addAll(headerBox, contentBox, bioLabel);
        
        // Add click handler
        card.setOnMouseClicked(event -> selectFormateur(formateur));
        
        return card;
    }

    private void selectFormateur(Formateur formateur) {
        selectedFormateur = formateur;
        updateButtonStates();
        updateSelectionStatus(formateur);
        
        // Highlight selected card
        cardsFlowPane.getChildren().forEach(node -> {
            node.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12; -fx-border-color: #334155; -fx-border-width: 1; -fx-border-radius: 12; -fx-padding: 16; -fx-cursor: hand;");
        });
        
        // Find and highlight the selected card
        for (javafx.scene.Node node : cardsFlowPane.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                // Check if this card contains the selected formateur's name
                for (javafx.scene.Node child : card.getChildren()) {
                    if (child instanceof HBox) {
                        HBox headerBox = (HBox) child;
                        for (javafx.scene.Node headerChild : headerBox.getChildren()) {
                            if (headerChild instanceof VBox) {
                                VBox nameBox = (VBox) headerChild;
                                if (!nameBox.getChildren().isEmpty() && nameBox.getChildren().get(0) instanceof Label) {
                                    Label nameLabel = (Label) nameBox.getChildren().get(0);
                                    if (nameLabel.getText().equals(getFullName(formateur))) {
                                        card.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 12; -fx-border-color: #38bdf8; -fx-border-width: 2; -fx-border-radius: 12; -fx-padding: 16; -fx-cursor: hand;");
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void refreshPagination() {
        int pageCount = Math.max(1, (int) Math.ceil((double) filteredFormateurList.size() / ROWS_PER_PAGE));
        pagination.setPageCount(pageCount);

        int currentPage = pagination.getCurrentPageIndex();
        if (currentPage >= pageCount) {
            currentPage = pageCount - 1;
            pagination.setCurrentPageIndex(currentPage);
        }

        updateCardsPage(currentPage);
    }

    private void setFilteredFormateurList(List<Formateur> formateurs) {
        filteredFormateurList = FXCollections.observableArrayList(formateurs != null ? formateurs : List.of());
        selectedFormateur = null;
        refreshPagination();
    }

    private void applyFiltersAndSorting() {
        String keyword = normalize(searchField.getText());
        String scope = valueOrDefault(searchScopeComboBox.getValue(), "Tous");
        String specialiteFilter = normalize(specialiteFilterField.getText());
        double minNote = parseMinNote(valueOrDefault(minNoteComboBox.getValue(), "Toutes"));

        List<Formateur> results = formateurList.stream()
                .filter(formateur -> matchesKeyword(formateur, keyword, scope))
                .filter(formateur -> specialiteFilter.isEmpty() || containsIgnoreCase(formateur.getSpecialite(), specialiteFilter))
                .filter(formateur -> getSafeNote(formateur) >= minNote)
                .sorted(buildComparator())
                .toList();

        setFilteredFormateurList(results);
        updateUI();
    }

    private Comparator<Formateur> buildComparator() {
        String sortBy = valueOrDefault(sortByComboBox.getValue(), "ID");
        String sortDirection = valueOrDefault(sortDirectionComboBox.getValue(), "Ascendant");

        Comparator<Formateur> comparator;
        switch (sortBy) {
            case "Nom complet":
                comparator = Comparator.comparing(this::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Email":
                comparator = Comparator.comparing(this::getEmail, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Spécialité":
                comparator = Comparator.comparing(f -> nullSafe(f.getSpecialite()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Expérience":
                comparator = Comparator.comparingInt(this::getSafeExperience);
                break;
            case "Note":
                comparator = Comparator.comparingDouble(this::getSafeNote);
                break;
            case "ID":
            default:
                comparator = Comparator.comparingInt(Formateur::getId);
                break;
        }

        return "Descendant".equalsIgnoreCase(sortDirection) ? comparator.reversed() : comparator;
    }

    private boolean matchesKeyword(Formateur formateur, String keyword, String scope) {
        if (keyword.isEmpty()) {
            return true;
        }

        switch (scope) {
            case "ID":
                return String.valueOf(formateur.getId()).contains(keyword);
            case "Nom complet":
                return containsIgnoreCase(getFullName(formateur), keyword);
            case "Email":
                return containsIgnoreCase(getEmail(formateur), keyword);
            case "Spécialité":
                return containsIgnoreCase(formateur.getSpecialite(), keyword);
            case "Bio":
                return containsIgnoreCase(formateur.getBio(), keyword);
            case "LinkedIn":
                return containsIgnoreCase(formateur.getLinkedin(), keyword);
            case "Portfolio":
                return containsIgnoreCase(formateur.getPortfolio(), keyword);
            case "Tous":
            default:
                return String.valueOf(formateur.getId()).contains(keyword)
                        || containsIgnoreCase(getFullName(formateur), keyword)
                        || containsIgnoreCase(getEmail(formateur), keyword)
                        || containsIgnoreCase(formateur.getSpecialite(), keyword)
                        || containsIgnoreCase(formateur.getBio(), keyword)
                        || containsIgnoreCase(formateur.getLinkedin(), keyword)
                        || containsIgnoreCase(formateur.getPortfolio(), keyword);
        }
    }

    private double parseMinNote(String minNoteValue) {
        if (minNoteValue == null || "Toutes".equalsIgnoreCase(minNoteValue)) {
            return 0d;
        }
        try {
            return Double.parseDouble(minNoteValue.replace("+", "").trim());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    private int getSafeExperience(Formateur formateur) {
        return formateur.getExperience_annees() != null ? formateur.getExperience_annees() : 0;
    }

    private double getSafeNote(Formateur formateur) {
        return formateur.getNote_moyenne() != null ? formateur.getNote_moyenne() : 0d;
    }

    private String getFullName(Formateur formateur) {
        if (formateur.getUser() == null) {
            return "";
        }
        return (nullSafe(formateur.getUser().getPrenom()) + " " + nullSafe(formateur.getUser().getNom())).trim();
    }

    private String getEmail(Formateur formateur) {
        return formateur.getUser() != null ? nullSafe(formateur.getUser().getEmail()) : "";
    }

    private String getUserPhotoUrl(Formateur formateur) {
        if (formateur.getUser() == null) {
            return null;
        }
        
        User user = formateur.getUser();
        
        // Try avatar_url first, then photo, then return null for fallback
        if (user.getAvatar_url() != null && !user.getAvatar_url().trim().isEmpty()) {
            return user.getAvatar_url().trim();
        }
        
        if (user.getPhoto() != null && !user.getPhoto().trim().isEmpty()) {
            return user.getPhoto().trim();
        }
        
        return null;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    private String normalize(String value) {
        return nullSafe(value).toLowerCase(Locale.ROOT).trim();
    }

    private String nullSafe(String value) {
        return Objects.toString(value, "");
    }

    private String valueOrDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
    
    private void updateSelectionStatus(Formateur selected) {
        if (selected != null) {
            statusLabel.setText("Sélectionné: " + selected.getUser().getPrenom() + " " + selected.getUser().getNom());
        } else {
            updateUI();
        }
    }
    
    @FXML
    private void handleAddButton(ActionEvent event) {
        if (mainController != null) {
            mainController.showFormateurForm(null);
        } else {
            showAlert("Erreur", "MainController non disponible", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewDetailsButton(ActionEvent event) {
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur pour voir les détails", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/formateur-details.fxml"));
            Parent root = loader.load();

            FormateurDetailsController controller = loader.getController();
            controller.setFormateur(selectedFormateur);

            Stage stage = new Stage();
            stage.setTitle("Détails du Formateur");
            Scene scene = new Scene(root, 760, 720);
            URL css = getClass().getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(640);
            stage.setMinHeight(560);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur à modifier", Alert.AlertType.WARNING);
            return;
        }

        if (mainController != null) {
            mainController.showFormateurForm(selectedFormateur);
        } else {
            showAlert("Erreur", "MainController non disponible", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedFormateur == null) {
            showAlert("Avertissement", "Veuillez sélectionner un formateur à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer ce formateur ?");
        confirmDialog.setContentText("Formateur ID: " + selectedFormateur.getId());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                formateurService.supprimer(selectedFormateur.getId());
                loadFormateurs();
                showAlert("Succès", "Formateur supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadFormateurs();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSorting();
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        specialiteFilterField.clear();
        searchScopeComboBox.setValue("Tous");
        minNoteComboBox.setValue("Toutes");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");
        applyFiltersAndSorting();
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        applyFiltersAndSorting();
        statusLabel.setText("Filtres appliqués");
    }

    @FXML
    private void handleSort(ActionEvent event) {
        applyFiltersAndSorting();
        statusLabel.setText("Tri appliqué");
    }

    private void updateButtonStates() {
        boolean isSelected = selectedFormateur != null;
        viewDetailsButton.setDisable(!isSelected);
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainController(tn.formini.controllers.MainController mainController) {
        this.mainController = mainController;
    }
}
