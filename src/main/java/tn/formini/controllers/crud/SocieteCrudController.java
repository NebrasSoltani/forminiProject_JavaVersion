package tn.formini.controllers.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.formini.controllers.MainController;
import tn.formini.controllers.crud.SocieteFormController;
import tn.formini.controllers.crud.SocieteDetailsController;
import tn.formini.entities.Users.Societe;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SocieteCrudController {

    private static final int ROWS_PER_PAGE = 6;
    private MainController mainController;

    // ── FXML ────────────────────────────────────────────────────────────────
    @FXML private FlowPane     cardsFlowPane;
    @FXML private ScrollPane   cardsScrollPane;
    @FXML private Pagination   pagination;
    @FXML private Button       addButton, viewDetailsButton, editButton, deleteButton;
    @FXML private Button       refreshButton, backButton, searchButton;
    @FXML private Button       filterButton, sortButton, resetFiltersButton;
    @FXML private TextField    searchField, secteurFilterField;
    @FXML private ComboBox<String> searchScopeComboBox, sortByComboBox, sortDirectionComboBox;
    @FXML private CheckBox     hasWebsiteCheckBox;
    @FXML private Label        countLabel, statusLabel, lastUpdateLabel;

    // ── State ────────────────────────────────────────────────────────────────
    private SocieteService societeService;
    private UserService    userService;
    private ObservableList<Societe> societeList         = FXCollections.observableArrayList();
    private ObservableList<Societe> filteredSocieteList = FXCollections.observableArrayList();
    private Societe selectedSociete = null;

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

    // ════════════════════════════════════════════════════════════════════════
    // INIT
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        if (cardsFlowPane == null || pagination == null) {
            System.err.println("FXML injection failed – check fx:id attributes.");
            return;
        }
        societeService = new SocieteService();
        userService    = new UserService();
        setupAdvancedControls();
        setupPagination();
        loadSocietes();
        updateButtonStates();
    }

    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
    }

    private void setupAdvancedControls() {
        searchScopeComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "ID", "Nom société", "Secteur", "Description", "Adresse", "Site web", "Email"));
        sortByComboBox.setItems(FXCollections.observableArrayList(
                "ID", "Nom société", "Secteur", "Email"));
        sortDirectionComboBox.setItems(FXCollections.observableArrayList(
                "Ascendant", "Descendant"));

        searchScopeComboBox.setValue("Tous");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");

        // Live listeners
        searchField.textProperty().addListener((o, a, b)            -> applyFiltersAndSorting());
        secteurFilterField.textProperty().addListener((o, a, b)     -> applyFiltersAndSorting());
        hasWebsiteCheckBox.selectedProperty().addListener((o, a, b) -> applyFiltersAndSorting());
        searchScopeComboBox.valueProperty().addListener((o, a, b)   -> applyFiltersAndSorting());
        sortByComboBox.valueProperty().addListener((o, a, b)        -> applyFiltersAndSorting());
        sortDirectionComboBox.valueProperty().addListener((o, a, b) -> applyFiltersAndSorting());
    }

    // ════════════════════════════════════════════════════════════════════════
    // DATA
    // ════════════════════════════════════════════════════════════════════════

    private void loadSocietes() {
        try {
            if (statusLabel != null) statusLabel.setText("Chargement...");
            societeList = FXCollections.observableArrayList(societeService.afficher());
            applyFiltersAndSorting();
            updateUI();
            if (statusLabel != null) statusLabel.setText("");
        } catch (Exception e) {
            if (statusLabel != null) statusLabel.setText("Erreur de chargement");
            showAlert("Erreur", "Impossible de charger les sociétés: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateUI() {
        int count = filteredSocieteList.size();
        if (countLabel != null)
            countLabel.setText("Total: " + count + " société" + (count > 1 ? "s" : ""));
        if (lastUpdateLabel != null)
            lastUpdateLabel.setText("Dernière mise à jour: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    // ════════════════════════════════════════════════════════════════════════
    // PAGINATION
    // ════════════════════════════════════════════════════════════════════════

    private VBox createPage(int pageIndex) {
        if (cardsFlowPane != null) updateCardsPage(pageIndex);
        return new VBox();
    }

    private void updateCardsPage(int pageIndex) {
        cardsFlowPane.getChildren().clear();
        int from = pageIndex * ROWS_PER_PAGE;
        if (from >= filteredSocieteList.size()) return;
        int to = Math.min(from + ROWS_PER_PAGE, filteredSocieteList.size());
        for (Societe s : filteredSocieteList.subList(from, to))
            cardsFlowPane.getChildren().add(createSocieteCard(s));
    }

    private void refreshPagination() {
        int pages = Math.max(1, (int) Math.ceil((double) filteredSocieteList.size() / ROWS_PER_PAGE));
        pagination.setPageCount(pages);
        int cur = Math.min(pagination.getCurrentPageIndex(), pages - 1);
        pagination.setCurrentPageIndex(cur);
        updateCardsPage(cur);
    }

    private void setFilteredSocieteList(List<Societe> list) {
        filteredSocieteList = FXCollections.observableArrayList(list != null ? list : List.of());
        selectedSociete = null;
        refreshPagination();
    }

    // ════════════════════════════════════════════════════════════════════════
    // CARD BUILDER  — dark theme, Mentalthy-inspired layout
    // ════════════════════════════════════════════════════════════════════════

    private VBox createSocieteCard(Societe societe) {
        // Pick accent color deterministically
        int idx = Math.abs(Objects.hashCode(societe.getNom_societe())) % AVATAR_PALETTE.length;
        String accent  = AVATAR_PALETTE[idx][0];
        String accentBg = AVATAR_PALETTE[idx][1];

        // ── Card root ────────────────────────────────────────────────────────
        VBox card = new VBox(14);
        card.setPrefWidth(348);
        card.setStyle(cardStyle(false, accent));
        DropShadow shadow = new DropShadow(16, 0, 6, Color.rgb(0, 0, 0, 0.35));
        card.setEffect(shadow);

        // ── Avatar + Info ────────────────────────────────────────────────────
        HBox header = new HBox(14);
        header.setAlignment(Pos.TOP_LEFT);

        // Circular avatar
        String initial = (societe.getNom_societe() != null && !societe.getNom_societe().isEmpty())
                ? String.valueOf(societe.getNom_societe().charAt(0)).toUpperCase() : "S";

        StackPane avatar = new StackPane();
        avatar.setPrefSize(62, 62);
        avatar.setMinSize(62, 62);
        avatar.setStyle("-fx-background-color: " + accentBg + "; -fx-background-radius: 31; " +
                        "-fx-border-color: " + accent + "44; -fx-border-radius: 31; -fx-border-width: 2;");
        Label initLbl = new Label(initial);
        initLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: " + accent + ";");
        avatar.getChildren().add(initLbl);

        // Right-side info
        VBox info = new VBox(5);
        info.setAlignment(Pos.TOP_LEFT);

        Label nameLbl = new Label(nullSafe(societe.getNom_societe()));
        nameLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: white; -fx-wrap-text: true;");
        nameLbl.setMaxWidth(230);

        Label sectorBadge = new Label(nullSafe(societe.getSecteur()));
        sectorBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: " + accent + "; " +
                             "-fx-background-color: " + accentBg + "; -fx-background-radius: 20; -fx-padding: 3 10; " +
                             "-fx-border-color: " + accent + "44; -fx-border-radius: 20; -fx-border-width: 1;");

        // Rating
        HBox ratingRow = buildRatingBadge(societe, accent);

        info.getChildren().addAll(nameLbl, sectorBadge, ratingRow);
        header.getChildren().addAll(avatar, info);

        // ── Address ──────────────────────────────────────────────────────────
        Label adresse = new Label("📍  " + nullSafe(societe.getAdresse()));
        adresse.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        adresse.setMaxWidth(316);

        // ── Stats row ────────────────────────────────────────────────────────
        HBox stats = buildStatsRow(societe, accent);

        // ── Description ──────────────────────────────────────────────────────
        String desc = nullSafe(societe.getDescription());
        if (desc.length() > 90) desc = desc.substring(0, 90) + "…";
        Label descLbl = new Label(desc.isEmpty() ? "Aucune description disponible." : desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-wrap-text: true;");
        descLbl.setMaxWidth(316);

        // ── Tags ─────────────────────────────────────────────────────────────
        FlowPane tags = buildTagsPane(societe, accent, accentBg);

        // ── Divider ──────────────────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155; -fx-opacity: 0.6;");

        // ── Footer ───────────────────────────────────────────────────────────
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox contact = new VBox(3);
        String email = getEmail(societe);
        String web   = nullSafe(societe.getSite_web());

        Label emailLbl = new Label("✉  " + (email.isEmpty() ? "—" : email));
        emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        Label webLbl = new Label(web.isEmpty() ? "🌐  Pas de site web"
                : "🌐  " + (web.length() > 30 ? web.substring(0, 30) + "…" : web));
        webLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (web.isEmpty() ? "#334155" : accent) + ";");
        contact.getChildren().addAll(emailLbl, webLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Edit icon button
        Button editBtn = new Button("✏");
        editBtn.setStyle("-fx-background-color: rgba(244,180,0,0.1); -fx-text-fill: #f4b400; " +
                         "-fx-font-size: 14px; -fx-background-radius: 50; -fx-padding: 8 10; " +
                         "-fx-cursor: hand; -fx-border-width: 1; -fx-border-color: rgba(244,180,0,0.3); -fx-border-radius: 50;");
        editBtn.setOnAction(e -> { selectSociete(societe); handleEditButton(null); });

        // Details button
        Button detailsBtn = new Button("Voir Détails");
        detailsBtn.setStyle("-fx-background-color: " + accent + "; -fx-text-fill: #0f172a; " +
                            "-fx-font-size: 12px; -fx-font-weight: 900; -fx-background-radius: 22; " +
                            "-fx-padding: 9 20; -fx-cursor: hand; -fx-border-width: 0;");
        detailsBtn.setOnAction(e -> { selectSociete(societe); handleViewDetailsButton(null); });

        footer.getChildren().addAll(contact, spacer, editBtn, detailsBtn);

        // ── Assemble ─────────────────────────────────────────────────────────
        card.getChildren().addAll(header, adresse, stats, descLbl, tags, sep, footer);
        card.setOnMouseClicked(e -> { selectSociete(societe); updateCardStyles(); });
        return card;
    }

    private HBox buildRatingBadge(Societe s, String accent) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        double rating = 3.5 + (s.getId() % 30) / 20.0;
        String ratingStr = String.format("%.1f", Math.min(rating, 5.0));
        String bg = rating >= 4.5 ? "#0f9d58" : rating >= 3.5 ? "#f4b400" : "#ef4444";
        Label badge = new Label("★  " + ratingStr);
        badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; " +
                       "-fx-background-color: " + bg + "; -fx-background-radius: 20; -fx-padding: 3 10;");
        box.getChildren().add(badge);
        return box;
    }

    private HBox buildStatsRow(Societe s, String accent) {
        boolean hasWeb   = !nullSafe(s.getSite_web()).isBlank();
        boolean hasEmail = s.getUser() != null && !nullSafe(s.getUser().getEmail()).isBlank();
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
            buildStat("🌐", hasWeb ? "Site web" : "Pas de site", hasWeb, accent),
            buildStat("✉", hasEmail ? "Email" : "Pas d'email", hasEmail, accent),
            buildStat("📋", nullSafe(s.getDescription()).length() + " car.", true, accent)
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

    private FlowPane buildTagsPane(Societe s, String accent, String accentBg) {
        FlowPane pane = new FlowPane(8, 6);
        for (String tag : getTagsForSociete(s)) {
            Label lbl = new Label(tag);
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; " +
                         "-fx-background-color: #0f172a; -fx-background-radius: 20; -fx-padding: 4 12; " +
                         "-fx-border-color: #334155; -fx-border-radius: 20; -fx-border-width: 1;");
            pane.getChildren().add(lbl);
        }
        return pane;
    }

    // ════════════════════════════════════════════════════════════════════════
    // SELECTION
    // ════════════════════════════════════════════════════════════════════════

    private void selectSociete(Societe s) {
        selectedSociete = s;
        updateButtonStates();
        if (statusLabel != null && s != null)
            statusLabel.setText("Sélectionné: " + s.getNom_societe());
    }

    private void updateCardStyles() {
        if (cardsFlowPane == null) return;
        for (javafx.scene.Node child : cardsFlowPane.getChildren()) {
            if (!(child instanceof VBox)) continue;
            VBox card = (VBox) child;
            // reset
            card.setStyle(cardStyle(false, "#38bdf8"));
            if (selectedSociete == null) continue;
            try {
                HBox header = (HBox) card.getChildren().get(0);
                VBox info   = (VBox) header.getChildren().get(1);
                Label name  = (Label) info.getChildren().get(0);
                if (name.getText().equals(selectedSociete.getNom_societe())) {
                    int idx = Math.abs(Objects.hashCode(selectedSociete.getNom_societe())) % AVATAR_PALETTE.length;
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

    // ════════════════════════════════════════════════════════════════════════
    // FILTERING & SORTING
    // ════════════════════════════════════════════════════════════════════════

    private void applyFiltersAndSorting() {
        String keyword  = normalize(searchField.getText());
        String scope    = valueOrDefault(searchScopeComboBox.getValue(), "Tous");
        String secteurF = normalize(secteurFilterField.getText());
        boolean onlyWeb = hasWebsiteCheckBox.isSelected();

        List<Societe> results = societeList.stream()
                .filter(s -> matchesKeyword(s, keyword, scope))
                .filter(s -> secteurF.isEmpty() || containsIgnoreCase(s.getSecteur(), secteurF))
                .filter(s -> !onlyWeb || !nullSafe(s.getSite_web()).isBlank())
                .sorted(buildComparator())
                .toList();

        setFilteredSocieteList(results);
        updateUI();
    }

    private Comparator<Societe> buildComparator() {
        String by  = valueOrDefault(sortByComboBox.getValue(), "ID");
        String dir = valueOrDefault(sortDirectionComboBox.getValue(), "Ascendant");
        Comparator<Societe> cmp = switch (by) {
            case "Nom société" -> Comparator.comparing(s -> nullSafe(s.getNom_societe()), String.CASE_INSENSITIVE_ORDER);
            case "Secteur"     -> Comparator.comparing(s -> nullSafe(s.getSecteur()), String.CASE_INSENSITIVE_ORDER);
            case "Email"       -> Comparator.comparing(this::getEmail, String.CASE_INSENSITIVE_ORDER);
            default            -> Comparator.comparingInt(Societe::getId);
        };
        return "Descendant".equalsIgnoreCase(dir) ? cmp.reversed() : cmp;
    }

    private boolean matchesKeyword(Societe s, String kw, String scope) {
        if (kw.isEmpty()) return true;
        return switch (scope) {
            case "ID"          -> String.valueOf(s.getId()).contains(kw);
            case "Nom société" -> containsIgnoreCase(s.getNom_societe(), kw);
            case "Secteur"     -> containsIgnoreCase(s.getSecteur(), kw);
            case "Description" -> containsIgnoreCase(s.getDescription(), kw);
            case "Adresse"     -> containsIgnoreCase(s.getAdresse(), kw);
            case "Site web"    -> containsIgnoreCase(s.getSite_web(), kw);
            case "Email"       -> containsIgnoreCase(getEmail(s), kw);
            default            -> String.valueOf(s.getId()).contains(kw)
                                  || containsIgnoreCase(s.getNom_societe(), kw)
                                  || containsIgnoreCase(s.getSecteur(), kw)
                                  || containsIgnoreCase(s.getDescription(), kw)
                                  || containsIgnoreCase(s.getAdresse(), kw)
                                  || containsIgnoreCase(s.getSite_web(), kw)
                                  || containsIgnoreCase(getEmail(s), kw);
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    // CRUD HANDLERS
    // ════════════════════════════════════════════════════════════════════════

    @FXML private void handleAddButton(ActionEvent e) {
        if (mainController != null) {
            openFormInMainContent(SocieteFormController.Mode.ADD, null, "Ajouter une Société");
        } else {
            // Fallback to modal if no main controller available
            openForm(SocieteFormController.Mode.ADD, null, "Ajouter une Société", false);
        }
    }

    @FXML private void handleViewDetailsButton(ActionEvent e) {
        if (selectedSociete == null) { warn("Veuillez sélectionner une société."); return; }
        if (mainController != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-details.fxml"));
                Parent root = loader.load();
                SocieteDetailsController ctrl = loader.getController();
                ctrl.setSociete(selectedSociete);
                ctrl.setMainController(mainController);
                mainController.getContentArea().getChildren().setAll(root);
                mainController.getLabelPageTitle().setText("Détails de la Société");
            } catch (IOException ex) { showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR); }
        } else {
            // Fallback to modal if no main controller available
            openDetailsModal();
        }
    }

    @FXML private void handleEditButton(ActionEvent e) {
        if (selectedSociete == null) { warn("Veuillez sélectionner une société."); return; }
        if (mainController != null) {
            openFormInMainContent(SocieteFormController.Mode.EDIT, selectedSociete, "Modifier une Société");
        } else {
            // Fallback to modal if no main controller available
            openForm(SocieteFormController.Mode.EDIT, selectedSociete, "Modifier une Société", false);
        }
    }

    @FXML private void handleDeleteButton(ActionEvent e) {
        if (selectedSociete == null) { warn("Veuillez sélectionner une société."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer : " + selectedSociete.getNom_societe() + " ?");
        confirm.setTitle("Confirmation"); confirm.setHeaderText(null);
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                societeService.supprimer(selectedSociete.getId());
                selectedSociete = null;
                loadSocietes();
                showAlert("Succès", "Société supprimée avec succès.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) { showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR); }
        });
    }

    @FXML private void handleRefreshButton(ActionEvent e) { loadSocietes(); }
    @FXML private void handleSearch(ActionEvent e)        { applyFiltersAndSorting(); }
    @FXML private void handleFilter(ActionEvent e)        { applyFiltersAndSorting(); if (statusLabel!=null) statusLabel.setText("Filtres appliqués"); }
    @FXML private void handleSort(ActionEvent e)          { applyFiltersAndSorting(); if (statusLabel!=null) statusLabel.setText("Tri appliqué"); }

    @FXML private void handleResetFilters(ActionEvent e) {
        searchField.clear(); secteurFilterField.clear();
        hasWebsiteCheckBox.setSelected(false);
        searchScopeComboBox.setValue("Tous");
        sortByComboBox.setValue("ID");
        sortDirectionComboBox.setValue("Ascendant");
        if (statusLabel != null) statusLabel.setText("");
        applyFiltersAndSorting();
    }

    @FXML private void handleBackButton(ActionEvent e) {
        ((Stage) backButton.getScene().getWindow()).close();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private void openFormInMainContent(SocieteFormController.Mode mode, Societe s, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-form.fxml"));
            Parent root = loader.load();
            SocieteFormController ctrl = loader.getController();
            ctrl.setMode(mode);
            if (s != null) ctrl.setSociete(s);
            ctrl.setMainController(mainController); // Pass main controller for navigation
            mainController.getContentArea().getChildren().setAll(root);
            mainController.getLabelPageTitle().setText(title);
        } catch (IOException ex) { showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR); }
    }

    private void openDetailsModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-details.fxml"));
            Parent root = loader.load();
            SocieteDetailsController ctrl = loader.getController();
            ctrl.setSociete(selectedSociete);
            openStage("Détails de la Société", root);
        } catch (IOException ex) { showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR); }
    }

    private void openForm(SocieteFormController.Mode mode, Societe s, String title, boolean reload) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crud/societe-form.fxml"));
            Parent root = loader.load();
            SocieteFormController ctrl = loader.getController();
            ctrl.setMode(mode);
            if (s != null) ctrl.setSociete(s);
            openStage(title, root);
            if (reload) loadSocietes();
        } catch (IOException ex) { showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR); }
    }

    private void openStage(String title, Parent root) {
        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(root, 760, 720);
        URL css = getClass().getResource("/css/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(640); stage.setMinHeight(560);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void updateButtonStates() {
        boolean sel = selectedSociete != null;
        viewDetailsButton.setDisable(!sel);
        editButton.setDisable(!sel);
        deleteButton.setDisable(!sel);
    }

    private String getEmail(Societe s) {
        return s.getUser() != null ? nullSafe(s.getUser().getEmail()) : "";
    }

    private boolean containsIgnoreCase(String src, String kw) { return normalize(src).contains(kw); }
    private String  normalize(String v)   { return nullSafe(v).toLowerCase(Locale.ROOT).trim(); }
    private String  nullSafe(String v)    { return Objects.toString(v, ""); }
    private String  valueOrDefault(String v, String fb) { return (v == null || v.isBlank()) ? fb : v; }
    private void    warn(String msg)      { showAlert("Attention", msg, Alert.AlertType.WARNING); }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private String[] getTagsForSociete(Societe s) {
        String sec = nullSafe(s.getSecteur()).toLowerCase();
        if (sec.contains("tech") || sec.contains("it"))        return new String[]{"Technologie", "Innovation", "Digital"};
        if (sec.contains("santé") || sec.contains("med"))      return new String[]{"Santé", "Médical", "Bien-être"};
        if (sec.contains("éducation") || sec.contains("form")) return new String[]{"Éducation", "Formation", "Apprentissage"};
        if (sec.contains("finance") || sec.contains("banque")) return new String[]{"Finance", "Banque", "Services"};
        return new String[]{nullSafe(s.getSecteur()), "Services", "Professionnel"};
    }
}
