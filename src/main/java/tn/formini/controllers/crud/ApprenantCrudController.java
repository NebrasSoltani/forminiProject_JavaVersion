package tn.formini.controllers.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Domaine;
import tn.formini.services.UsersService.ApprenantService;
import tn.formini.services.UsersService.UserService;
import tn.formini.services.UsersService.DomaineService;
import tn.formini.controllers.MainController;
import tn.formini.controllers.crud.ApprenantFormController;
import tn.formini.controllers.crud.ApprenantDetailsController;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;

public class ApprenantCrudController {
    private static final int ROWS_PER_PAGE = 6;

    // ── Color palette for avatars ─────────────────────────────────────────────
    private static final String[][] AVATAR_PALETTE = {
        {"#38bdf8", "rgba(56,189,248,0.15)"},   // cyan
        {"#818cf8", "rgba(129,140,248,0.15)"},   // indigo
        {"#34d399", "rgba(52,211,153,0.15)"},    // emerald
        {"#f472b6", "rgba(244,114,182,0.15)"},   // pink
        {"#fb923c", "rgba(251,146,60,0.15)"},    // orange
        {"#a78bfa", "rgba(167,139,250,0.15)"},   // violet
        {"#4ade80", "rgba(74,222,128,0.15)"},    // green
        {"#f87171", "rgba(248,113,113,0.15)"},   // red
    };

    @FXML
    private FlowPane cardsFlowPane;

    @FXML
    private ScrollPane cardsScrollPane;

    @FXML
    private Pagination pagination;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Label countLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label lastUpdateLabel;
    
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
    private Button backButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchScopeComboBox;

    @FXML
    private ComboBox<String> genreFilterComboBox;

    @FXML
    private ComboBox<String> etatCivilFilterComboBox;

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
    
    private ApprenantService apprenantService;
    private UserService userService;
    private DomaineService domaineService;
    private MainController mainController;
    private ObservableList<Apprenant> apprenantList;
    private ObservableList<Apprenant> filteredApprenantList;
    private Apprenant selectedApprenant;

    @FXML
    public void initialize() {
        apprenantService = new ApprenantService();
        userService = new UserService();
        domaineService = new DomaineService();
        
        apprenantList = FXCollections.observableArrayList();
        filteredApprenantList = FXCollections.observableArrayList();
        
        setupSearchAndFilters();
        setupPagination();
        loadApprenants();
        updateUI();
        updateButtonStates();
    }

    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
    }

    private void setupAdvancedControls() {
        searchScopeComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "ID", "Nom complet", "Email", "Genre", "Etat civil", "Objectif", "Domaines"
        ));
        genreFilterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "homme", "femme", "autre"
        ));
        etatCivilFilterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "celibataire", "marie", "divorce", "veuf"
        ));
        sortByComboBox.setItems(FXCollections.observableArrayList(
                "ID", "Nom complet", "Email", "Genre", "Etat civil"
        ));
        sortDirectionComboBox.setItems(FXCollections.observableArrayList(
                "Ascendant", "Descendant"
        ));

        searchScopeComboBox.setValue("Tous");
        genreFilterComboBox.setValue("Tous");
        etatCivilFilterComboBox.setValue("Tous");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        searchScopeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        genreFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        etatCivilFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortByComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
        sortDirectionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());
    }

    
    private void loadApprenants() {
        try {
            statusLabel.setText("Chargement...");
            List<Apprenant> apprenants = apprenantService.afficher();
            apprenantList = FXCollections.observableArrayList(apprenants);
            applyFiltersAndSorting();
            updateUI();
            statusLabel.setText("Prêt");
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du chargement");
            showAlert("Erreur", "Impossible de charger les apprenants: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateUI() {
        // Update count label
        int count = filteredApprenantList.size();
        countLabel.setText("Total: " + count + " apprenant" + (count > 1 ? "s" : ""));
        
        // Update last update time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
    }

    private VBox createPage(int pageIndex) {
        updateCardsPage(pageIndex);
        return new VBox(); // Return empty VBox as page factory expects a node
    }

    private void updateCardsPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        if (fromIndex >= filteredApprenantList.size()) {
            cardsFlowPane.getChildren().clear();
            return;
        }

        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredApprenantList.size());
        List<Apprenant> pageApprenants = filteredApprenantList.subList(fromIndex, toIndex);
        
        cardsFlowPane.getChildren().clear();
        for (Apprenant apprenant : pageApprenants) {
            cardsFlowPane.getChildren().add(createApprenantCard(apprenant));
        }
    }

    private void refreshPagination() {
        int pageCount = Math.max(1, (int) Math.ceil((double) filteredApprenantList.size() / ROWS_PER_PAGE));
        pagination.setPageCount(pageCount);

        int currentPage = pagination.getCurrentPageIndex();
        if (currentPage >= pageCount) {
            currentPage = pageCount - 1;
            pagination.setCurrentPageIndex(currentPage);
        }

        updateCardsPage(currentPage);
    }

    private void setFilteredApprenantList(List<Apprenant> apprenants) {
        filteredApprenantList = FXCollections.observableArrayList(apprenants != null ? apprenants : List.of());
        refreshPagination();
    }

    private void applyFiltersAndSorting() {
        String keyword = normalize(searchField.getText());
        String scope = valueOrDefault(searchScopeComboBox.getValue(), "Tous");
        String genreFilter = valueOrDefault(genreFilterComboBox.getValue(), "Tous");
        String etatCivilFilter = valueOrDefault(etatCivilFilterComboBox.getValue(), "Tous");

        List<Apprenant> results = apprenantList.stream()
                .filter(apprenant -> matchesKeyword(apprenant, keyword, scope))
                .filter(apprenant -> "Tous".equalsIgnoreCase(genreFilter) || genreFilter.equalsIgnoreCase(nullSafe(apprenant.getGenre())))
                .filter(apprenant -> "Tous".equalsIgnoreCase(etatCivilFilter) || etatCivilFilter.equalsIgnoreCase(nullSafe(apprenant.getEtat_civil())))
                .sorted(buildComparator())
                .toList();

        setFilteredApprenantList(results);
        updateUI();
    }

    private Comparator<Apprenant> buildComparator() {
        String sortBy = valueOrDefault(sortByComboBox.getValue(), "ID");
        String sortDirection = valueOrDefault(sortDirectionComboBox.getValue(), "Ascendant");

        Comparator<Apprenant> comparator;
        switch (sortBy) {
            case "Nom complet":
                comparator = Comparator.comparing(this::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Email":
                comparator = Comparator.comparing(this::getEmail, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Genre":
                comparator = Comparator.comparing(a -> nullSafe(a.getGenre()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "Etat civil":
                comparator = Comparator.comparing(a -> nullSafe(a.getEtat_civil()), String.CASE_INSENSITIVE_ORDER);
                break;
            case "ID":
            default:
                comparator = Comparator.comparingInt(Apprenant::getId);
                break;
        }

        return "Descendant".equalsIgnoreCase(sortDirection) ? comparator.reversed() : comparator;
    }

    private boolean matchesKeyword(Apprenant apprenant, String keyword, String scope) {
        if (keyword.isEmpty()) {
            return true;
        }

        switch (scope) {
            case "ID":
                return String.valueOf(apprenant.getId()).contains(keyword);
            case "Nom complet":
                return containsIgnoreCase(getFullName(apprenant), keyword);
            case "Email":
                return containsIgnoreCase(getEmail(apprenant), keyword);
            case "Genre":
                return containsIgnoreCase(apprenant.getGenre(), keyword);
            case "Etat civil":
                return containsIgnoreCase(apprenant.getEtat_civil(), keyword);
            case "Objectif":
                return containsIgnoreCase(apprenant.getObjectif(), keyword);
            case "Domaines":
                return containsIgnoreCase(formatDomainesInteret(apprenant), keyword);
            case "Tous":
            default:
                return String.valueOf(apprenant.getId()).contains(keyword)
                        || containsIgnoreCase(getFullName(apprenant), keyword)
                        || containsIgnoreCase(getEmail(apprenant), keyword)
                        || containsIgnoreCase(apprenant.getGenre(), keyword)
                        || containsIgnoreCase(apprenant.getEtat_civil(), keyword)
                        || containsIgnoreCase(apprenant.getObjectif(), keyword)
                        || containsIgnoreCase(formatDomainesInteret(apprenant), keyword);
        }
    }

    private String getFullName(Apprenant apprenant) {
        if (apprenant.getUser() == null) {
            return "";
        }
        return (nullSafe(apprenant.getUser().getPrenom()) + " " + nullSafe(apprenant.getUser().getNom())).trim();
    }

    private String getEmail(Apprenant apprenant) {
        return apprenant.getUser() != null ? nullSafe(apprenant.getUser().getEmail()) : "";
    }

    private String formatDomainesInteret(Apprenant apprenant) {
        if (apprenant == null) {
            return "N/A";
        }

        String domainesRaw = nullSafe(apprenant.getDomaines_interet()).trim();
        if (!domainesRaw.isEmpty() && !"[]".equals(domainesRaw)) {
            // Most records are stored as a JSON-like array string: ["IA","Web"].
            String cleaned = domainesRaw
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .trim();

            if (!cleaned.isEmpty()) {
                return cleaned.contains(",")
                        ? List.of(cleaned.split(","))
                        .stream()
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .collect(Collectors.joining(", "))
                        : cleaned;
            }
        }
        return "N/A";
    }

    private VBox createApprenantCard(Apprenant apprenant) {
        String fullName = getFullName(apprenant);
        int idx = Math.abs(Objects.hashCode(fullName)) % AVATAR_PALETTE.length;
        String accent  = AVATAR_PALETTE[idx][0];
        String accentBg = AVATAR_PALETTE[idx][1];

        // Card root
        VBox card = new VBox(14);
        card.setPrefWidth(348);
        card.setStyle(cardStyle(false, accent));
        DropShadow shadow = new DropShadow(16, 0, 6, Color.rgb(0, 0, 0, 0.35));
        card.setEffect(shadow);

        // Avatar + Info
        HBox header = new HBox(14);
        header.setAlignment(Pos.TOP_LEFT);

        String initial = (fullName != null && !fullName.isEmpty()) ? String.valueOf(fullName.charAt(0)).toUpperCase() : "A";

        StackPane avatar = new StackPane();
        avatar.setPrefSize(62, 62);
        avatar.setMinSize(62, 62);
        avatar.setStyle("-fx-background-color: " + accentBg + "; -fx-background-radius: 31; " +
                        "-fx-border-color: " + accent + "44; -fx-border-radius: 31; -fx-border-width: 2;");
        Label initLbl = new Label(initial);
        initLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: " + accent + ";");
        avatar.getChildren().add(initLbl);

        VBox info = new VBox(5);
        info.setAlignment(Pos.TOP_LEFT);

        Label nameLbl = new Label(fullName);
        nameLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: white; -fx-wrap-text: true;");
        nameLbl.setMaxWidth(230);

        Label roleBadge = new Label(nullSafe(apprenant.getGenre()));
        roleBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: " + accent + "; " +
                             "-fx-background-color: " + accentBg + "; -fx-background-radius: 20; -fx-padding: 3 10; " +
                             "-fx-border-color: " + accent + "44; -fx-border-radius: 20; -fx-border-width: 1;");

        HBox ratingRow = buildRatingBadge(apprenant, accent);

        info.getChildren().addAll(nameLbl, roleBadge, ratingRow);
        header.getChildren().addAll(avatar, info);

        Label etat = new Label("📋  État civil: " + nullSafe(apprenant.getEtat_civil()));
        etat.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        etat.setMaxWidth(316);

        HBox stats = buildStatsRow(apprenant, accent);

        String desc = nullSafe(apprenant.getObjectif());
        if (desc.length() > 90) desc = desc.substring(0, 90) + "…";
        Label descLbl = new Label(desc.isEmpty() ? "Aucun objectif spécifié." : desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-wrap-text: true;");
        descLbl.setMaxWidth(316);

        FlowPane tags = buildTagsPane(apprenant, accent, accentBg);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155; -fx-opacity: 0.6;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox contact = new VBox(3);
        String email = getEmail(apprenant);

        Label emailLbl = new Label("✉  " + (email.isEmpty() ? "—" : email));
        emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        contact.getChildren().add(emailLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✏");
        editBtn.setStyle("-fx-background-color: rgba(244,180,0,0.1); -fx-text-fill: #f4b400; " +
                         "-fx-font-size: 14px; -fx-background-radius: 50; -fx-padding: 8 10; " +
                         "-fx-cursor: hand; -fx-border-width: 1; -fx-border-color: rgba(244,180,0,0.3); -fx-border-radius: 50;");
        editBtn.setOnAction(e -> { selectApprenant(apprenant); handleEditButton(null); });

        Button detailsBtn = new Button("Voir Détails");
        detailsBtn.setStyle("-fx-background-color: " + accent + "; -fx-text-fill: #0f172a; " +
                            "-fx-font-size: 12px; -fx-font-weight: 900; -fx-background-radius: 22; " +
                            "-fx-padding: 9 20; -fx-cursor: hand; -fx-border-width: 0;");
        detailsBtn.setOnAction(e -> { selectApprenant(apprenant); handleViewDetailsButton(null); });

        footer.getChildren().addAll(contact, spacer, editBtn, detailsBtn);

        card.getChildren().addAll(header, etat, stats, descLbl, tags, sep, footer);
        card.setOnMouseClicked(e -> { selectApprenant(apprenant); updateCardStyles(); });
        return card;
    }

    private HBox buildRatingBadge(Apprenant a, String accent) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        double rating = 3.5 + (a.getId() % 30) / 20.0;
        String ratingStr = String.format("%.1f", Math.min(rating, 5.0));
        String bg = rating >= 4.5 ? "#0f9d58" : rating >= 3.5 ? "#f4b400" : "#ef4444";
        Label badge = new Label("★  " + ratingStr);
        badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; " +
                       "-fx-background-color: " + bg + "; -fx-background-radius: 20; -fx-padding: 3 10;");
        box.getChildren().add(badge);
        return box;
    }

    private HBox buildStatsRow(Apprenant a, String accent) {
        boolean hasEmail = getEmail(a) != null && !getEmail(a).isBlank();
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
            buildStat("✉", hasEmail ? "Email" : "Pas d'email", hasEmail, accent),
            buildStat("📋", nullSafe(a.getObjectif()).length() + " car.", true, accent)
        );
        return row;
    }

    private VBox buildStat(String icon, String label, boolean active, String accent) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 14px;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (active ? accent : "#475569") +
                     "; -fx-font-weight: bold;");
        box.getChildren().addAll(ico, lbl);
        return box;
    }

    private FlowPane buildTagsPane(Apprenant a, String accent, String accentBg) {
        FlowPane pane = new FlowPane(8, 6);
        String formatted = formatDomainesInteret(a);
        if (!"N/A".equals(formatted)) {
            String[] tags = formatted.split(",");
            for (String t : tags) {
                String tag = t.trim();
                if (tag.isEmpty()) continue;
                Label lbl = new Label(tag);
                lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; " +
                             "-fx-background-color: #0f172a; -fx-background-radius: 20; -fx-padding: 4 12; " +
                             "-fx-border-color: #334155; -fx-border-radius: 20; -fx-border-width: 1;");
                pane.getChildren().add(lbl);
            }
        }
        return pane;
    }

    private void selectApprenant(Apprenant a) {
        selectedApprenant = a;
        updateButtonStates();
        updateSelectionStatus(a);
    }

    private void updateCardStyles() {
        if (cardsFlowPane == null) return;
        for (javafx.scene.Node child : cardsFlowPane.getChildren()) {
            if (!(child instanceof VBox)) continue;
            VBox card = (VBox) child;
            card.setStyle(cardStyle(false, "#38bdf8"));
            if (selectedApprenant == null) continue;
            try {
                HBox header = (HBox) card.getChildren().get(0);
                VBox info   = (VBox) header.getChildren().get(1);
                Label name  = (Label) info.getChildren().get(0);
                if (name.getText().equals(getFullName(selectedApprenant))) {
                    int idx = Math.abs(Objects.hashCode(getFullName(selectedApprenant))) % AVATAR_PALETTE.length;
                    card.setStyle(cardStyle(true, AVATAR_PALETTE[idx][0]));
                }
            } catch (Exception ignored) {}
        }
    }

    private String cardStyle(boolean selected, String accent) {
        if (selected) {
            return "-fx-background-color: #1e293b; -fx-background-radius: 18; -fx-border-radius: 18; " +
                   "-fx-border-color: " + accent + "; -fx-border-width: 2; -fx-padding: 20; -fx-cursor: hand;";
        }
        return "-fx-background-color: #1e293b; -fx-background-radius: 18; -fx-border-radius: 18; " +
               "-fx-border-color: #334155; -fx-border-width: 1; -fx-padding: 20; -fx-cursor: hand;";
    }

    @FXML
    private void handleAddButton(ActionEvent event) {
        if (mainController != null) {
            openFormInMainContent(ApprenantFormController.Mode.ADD, null, "Ajouter un Apprenant");
        } else {
            openForm(ApprenantFormController.Mode.ADD, null, "Ajouter un Apprenant", false);
        }
    }

    @FXML
    private void handleViewDetailsButton(ActionEvent event) {
        if (selectedApprenant == null) {
            showAlert("Avertissement", "Veuillez sélectionner un apprenant pour voir les détails", Alert.AlertType.WARNING);
            return;
        }

        if (mainController != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-details.fxml"));
                Parent root = loader.load();
                ApprenantDetailsController ctrl = loader.getController();
                ctrl.setApprenant(selectedApprenant);
                ctrl.setMainController(mainController);
                mainController.getContentArea().getChildren().setAll(root);
                mainController.getLabelPageTitle().setText("Détails de l'Apprenant");
            } catch (IOException ex) {
                showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            openDetailsModal();
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        if (selectedApprenant == null) {
            showAlert("Avertissement", "Veuillez sélectionner un apprenant à modifier", Alert.AlertType.WARNING);
            return;
        }

        if (mainController != null) {
            openFormInMainContent(ApprenantFormController.Mode.EDIT, selectedApprenant, "Modifier un Apprenant");
        } else {
            openForm(ApprenantFormController.Mode.EDIT, selectedApprenant, "Modifier un Apprenant", false);
        }
    }

    private void openFormInMainContent(ApprenantFormController.Mode mode, Apprenant a, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-form.fxml"));
            Parent root = loader.load();
            ApprenantFormController ctrl = loader.getController();
            ctrl.setMode(mode);
            if (a != null) ctrl.setApprenant(a);
            ctrl.setMainController(mainController);
            mainController.getContentArea().getChildren().setAll(root);
            mainController.getLabelPageTitle().setText(title);
        } catch (IOException ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openDetailsModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-details.fxml"));
            Parent root = loader.load();
            ApprenantDetailsController ctrl = loader.getController();
            ctrl.setApprenant(selectedApprenant);
            openStage("Détails de l'Apprenant", root);
        } catch (IOException ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openForm(ApprenantFormController.Mode mode, Apprenant a, String title, boolean reload) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/apprenant-form.fxml"));
            Parent root = loader.load();
            ApprenantFormController ctrl = loader.getController();
            ctrl.setMode(mode);
            if (a != null) ctrl.setApprenant(a);
            openStage(title, root);
            if (reload) loadApprenants();
        } catch (IOException ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openStage(String title, Parent root) {
        Stage stage = new Stage();
        stage.setTitle(title);
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
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedApprenant == null) {
            showAlert("Avertissement", "Veuillez sélectionner un apprenant à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cet apprenant ?");
        confirmDialog.setContentText("Apprenant ID: " + selectedApprenant.getId());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                apprenantService.supprimer(selectedApprenant.getId());
                loadApprenants();
                showAlert("Succès", "Apprenant supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadApprenants();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSorting();
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        searchScopeComboBox.setValue("Tous");
        genreFilterComboBox.setValue("Tous");
        etatCivilFilterComboBox.setValue("Tous");
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
        boolean isSelected = selectedApprenant != null;
        viewDetailsButton.setDisable(!isSelected);
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
    }

    private void updateSelectionStatus(Apprenant apprenant) {
        if (apprenant != null) {
            statusLabel.setText("Sélectionné: " + getFullName(apprenant));
        } else {
            statusLabel.setText("Prêt");
        }
    }

    private void setupSearchAndFilters() {
        setupAdvancedControls();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    private boolean containsIgnoreCase(String src, String kw) { return normalize(src).contains(kw); }
    private String  normalize(String v)   { return nullSafe(v).toLowerCase(Locale.ROOT).trim(); }
    private String  nullSafe(String v)    { return Objects.toString(v, ""); }
    private String  valueOrDefault(String v, String fb) { return (v == null || v.isBlank()) ? fb : v; }
}
