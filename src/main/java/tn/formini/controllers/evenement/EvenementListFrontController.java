package tn.formini.controllers.evenement;
import tn.formini.controllers.MainController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import tn.formini.utils.ListStyleManager;
import javafx.geometry.Pos;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import tn.formini.entities.evenements.Evenement;
import tn.formini.services.evenementsService.EvenementService;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
        filterType.setItems(FXCollections.observableArrayList(
                "Tous", "Conférence", "Atelier", "Webinaire", "Formation", "Autre"
        ));
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterType.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterLive.selectedProperty().addListener((obs, o, n) -> applyFilters());
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
            StackPane imgHeader = new StackPane();
            imgHeader.getStyleClass().add("card-image-cover");
            if (evt.getImage() != null && !evt.getImage().trim().isEmpty()) {
                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(evt.getImage());
                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
                    imageView.setFitWidth(320);
                    imageView.setFitHeight(150);
                    // Set preserveRatio to false to cover the box, or true. Usually cover is good.
                    imageView.setPreserveRatio(false);
                    imgHeader.getChildren().add(imageView);
                } catch (Exception ex) {
                    Label icon = new Label("📅");
                    icon.setStyle("-fx-font-size: 40px;");
                    imgHeader.getChildren().add(icon);
                }
            } else {
                Label icon = new Label("📅");
                icon.setStyle("-fx-font-size: 40px;");
                imgHeader.getChildren().add(icon);
            }

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
            VBox details = new VBox(5);
            Label dateLieu = new Label("📍 " + evt.getLieu() + " | 👥 " + evt.getNombre_places() + " places");
            dateLieu.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            details.getChildren().add(dateLieu);

            // Action
            Button btnParticiper = new Button("Participer maintenant");
            btnParticiper.getStyleClass().add("main-button-onix");
            btnParticiper.setMaxWidth(Double.MAX_VALUE);
            btnParticiper.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inscription");
                alert.setHeaderText("Confirmation d'inscription");
                alert.setContentText("Vous êtes maintenant inscrit à l'événement: " + evt.getTitre());
                alert.show();
            });

            body.getChildren().addAll(badge, title, details, btnParticiper);
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