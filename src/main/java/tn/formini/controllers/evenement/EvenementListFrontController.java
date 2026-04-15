package tn.formini.controllers.evenement;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.formini.entities.Evenement;
import tn.formini.services.EvenementService;
import tn.formini.utils.EventUiUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EvenementListFrontController implements Initializable {

    @FXML private FlowPane eventGrid;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private CheckBox         filterLive;
    @FXML private Label            labelCount;
    @FXML private Pagination       pagination;

    private final EvenementService service = new EvenementService();
    private List<Evenement> allEvenements = new ArrayList<>();
    private List<Evenement> filteredEvenements = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 6;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilters();
        loadEvenements();
        setupPagination();
    }

    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
    }

    private javafx.scene.Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredEvenements.size());

        if (fromIndex >= filteredEvenements.size()) {
            renderCards(new ArrayList<>());
        } else {
            renderCards(filteredEvenements.subList(fromIndex, toIndex));
        }

        Region dummy = new Region();
        dummy.setPrefHeight(0);
        return dummy;
    }

    private void setupFilters() {
        filterType.setItems(FXCollections.observableArrayList(Evenement.DISPLAY_TYPES));
        searchField.textProperty().addListener((_, _, _) -> applyFilters());
        filterType.valueProperty().addListener((_, _, _) -> applyFilters());
        filterLive.selectedProperty().addListener((_, _, _) -> applyFilters());
    }

    private void loadEvenements() {
        allEvenements = service.afficher();
        // SORT BY ID DESCENDING (Most recent first)
        allEvenements.sort(Comparator.comparing(Evenement::getId).reversed());
        filteredEvenements = new ArrayList<>(allEvenements);
        updatePagination();
        labelCount.setText(allEvenements.size() + " événement(s)");
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredEvenements.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        renderCards(filteredEvenements.subList(0, Math.min(ITEMS_PER_PAGE, filteredEvenements.size())));
    }

    private void renderCards(List<Evenement> list) {
        eventGrid.getChildren().clear();
        for (Evenement evt : list) {
            VBox card = new VBox();
            card.getStyleClass().add("event-card");
            card.setPrefWidth(320);

            // Header image/icon
            StackPane imgHeader = EventUiUtils.createEventImageHeader(evt);

            VBox body = new VBox(15);
            body.setPadding(new javafx.geometry.Insets(25));

            // Type Badge
            Label badge = new Label(evt.getType().toUpperCase());
            badge.getStyleClass().add("card-badge");

            // Title
            Label title = new Label(evt.getTitre());
            title.getStyleClass().add("card-title-lg");
            title.setWrapText(true);
            title.setPrefHeight(45);

            // Details
            VBox details = EventUiUtils.createEventDetails(evt);

            // Live and 360 Buttons
            HBox features = new HBox(10);
            if (evt.isLive()) {
                Button btnLive = new Button("🔴 LIVE");
                btnLive.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
                btnLive.setOnAction(_ -> {
                    try { java.awt.Desktop.getDesktop().browse(new java.net.URI(evt.getStream_url())); }
                    catch (Exception ex) { System.err.println(ex.getMessage()); }
                });
                features.getChildren().add(btnLive);
            }
            if (evt.getImage360() != null && !evt.getImage360().isEmpty()) {
                Button btn360 = new Button("📸 360°");
                btn360.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
                btn360.setOnAction(_ -> {
                    try { java.awt.Desktop.getDesktop().browse(new java.net.URI(evt.getImage360())); }
                    catch (Exception ex) { System.err.println(ex.getMessage()); }
                });
                features.getChildren().add(btn360);
            }
            
            // Action
            Button btnParticiper = new Button("Participer maintenant");
            btnParticiper.getStyleClass().add("main-button-onix");
            btnParticiper.setMaxWidth(Double.MAX_VALUE);
            btnParticiper.setOnAction(_ -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inscription");
                alert.setHeaderText("Confirmation d'inscription");
                alert.setContentText("Vous êtes maintenant inscrit à l'événement: " + evt.getTitre());
                alert.show();
            });

            body.getChildren().addAll(badge, title, details, features, btnParticiper);

            card.getChildren().addAll(imgHeader, body);
            eventGrid.getChildren().add(card);
        }
    }


    private void applyFilters() {
        if (allEvenements == null) return;
        String search = searchField.getText().toLowerCase();
        String type   = filterType.getValue();

        filteredEvenements = allEvenements.stream().filter(e -> {
                    boolean matchSearch = search.isEmpty() || e.getTitre().toLowerCase().contains(search);
                    boolean matchType  = type == null || type.equals("Tous") || e.getType().equalsIgnoreCase(type);
                    boolean matchLive  = !filterLive.isSelected()  || e.isLive();
                    boolean matchActif = e.isIs_actif(); // Only show active events publicly
                    return matchSearch && matchType && matchLive && matchActif;
                }).sorted(Comparator.comparing(Evenement::getId).reversed())
                .collect(Collectors.toList());

        updatePagination();
        labelCount.setText(filteredEvenements.size() + " trouvé(s)");
    }


    @FXML
    public void resetFilters() {
        searchField.clear();
        filterType.setValue(null);
        filterLive.setSelected(false);
        renderCards(allEvenements);
        labelCount.setText(allEvenements.size() + " événement(s)");
    }
}