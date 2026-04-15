package tn.formini.controllers.evenement;
import tn.formini.controllers.MainController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.formini.utils.ListStyleManager;
import javafx.geometry.Pos;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import tn.formini.entities.Evenement;
import tn.formini.services.EvenementService;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EvenementListController implements Initializable {

    @FXML private FlowPane eventGrid;
    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, String> colType;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, Integer> colPlaces;
    @FXML private TableColumn<Evenement, Boolean> colStatus;
    @FXML private TableColumn<Evenement, Void> colActions;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private CheckBox         filterLive;
    @FXML private CheckBox         filterActif;
    @FXML private Label            labelCount;
    @FXML private Pagination       pagination;

    private MainController mainController;
    private final EvenementService service = new EvenementService();
    private List<Evenement> allEvenements = new ArrayList<>();
    private List<Evenement> filteredEvenements = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 6;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();
        loadEvenements();
        setupPagination();
    }

    private void setupTable() {
        ListStyleManager.applyStandardStyle(tableEvenements);
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nombre_places"));

        // Install tooltips for truncated columns
        ListStyleManager.installTooltip(colTitre);
        ListStyleManager.installTooltip(colLieu);

        // Set default sort visually
        tableEvenements.getSortOrder().add(colTitre);

        colStatus.setCellValueFactory(new PropertyValueFactory<>("is_actif"));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            private final Label label = new Label();
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                } else {
                    label.setText(active ? "● Actif" : "○ Inactif");
                    label.getStyleClass().setAll("status-badge-" + (active ? "active" : "inactive"));
                    setGraphic(label);
                }
            }
        });

        colActions.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Evenement e = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_RIGHT);
                    Button btnE = new Button("✏️"); btnE.getStyleClass().add("btn-edit");
                    btnE.setOnAction(event -> editEvenement(e));
                    Button btnD = new Button("🗑️"); btnD.getStyleClass().add("btn-delete");
                    btnD.setOnAction(event -> deleteEvenement(e));
                    box.getChildren().addAll(btnE, btnD);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
    }

    private javafx.scene.Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredEvenements.size());

        if (fromIndex >= filteredEvenements.size()) {
            tableEvenements.setItems(FXCollections.emptyObservableList());
        } else {
            tableEvenements.setItems(FXCollections.observableArrayList(filteredEvenements.subList(fromIndex, toIndex)));
        }
        return tableEvenements;
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    private void setupFilters() {
        filterType.setItems(FXCollections.observableArrayList(
                "Tous", "Conférence", "Atelier", "Webinaire", "Formation", "Autre"
        ));
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterType.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterLive.selectedProperty().addListener((obs, o, n) -> applyFilters());
        filterActif.selectedProperty().addListener((obs, o, n) -> applyFilters());
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
        if (eventGrid == null) return;
        eventGrid.getChildren().clear();
        for (Evenement evt : list) {
            VBox card = new VBox();
            card.getStyleClass().add("event-card");
            card.setPrefWidth(320);

            javafx.scene.layout.StackPane imgHeader = new javafx.scene.layout.StackPane();
            imgHeader.getStyleClass().add("card-image-cover");
            if (evt.getImage() != null && !evt.getImage().trim().isEmpty()) {
                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(evt.getImage());
                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
                    imageView.setFitWidth(320);
                    imageView.setFitHeight(150);
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

            HBox badges = new HBox(10);
            Label badgeType = new Label(evt.getType().toUpperCase());
            badgeType.getStyleClass().add("card-badge");

            Label statusBadge = new Label(evt.isIs_actif() ? "ACTIF" : "INACTIF");
            statusBadge.setStyle(evt.isIs_actif() ? "-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; -fx-padding: 5 15; -fx-background-radius: 5px; -fx-font-size: 11px; -fx-font-weight: bold;" : "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-padding: 5 15; -fx-background-radius: 5px; -fx-font-size: 11px; -fx-font-weight: bold;");
            badges.getChildren().addAll(badgeType, statusBadge);

            Label title = new Label(evt.getTitre());
            title.getStyleClass().add("card-title-lg");
            title.setWrapText(true);
            title.setPrefHeight(45);

            VBox details = new VBox(5);
            Label dateLieu = new Label("📍 " + evt.getLieu() + " | 👥 " + evt.getNombre_places() + " places");
            dateLieu.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            details.getChildren().add(dateLieu);

            HBox actions = new HBox(10);

            Button btnEdit = new Button("Modifier");
            btnEdit.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> editEvenement(evt));

            Button btnDelete = new Button("Supprimer");
            btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> deleteEvenement(evt));

            actions.getChildren().addAll(btnEdit, btnDelete);

            body.getChildren().addAll(badges, title, details, actions);
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
                    boolean matchActif = !filterActif.isSelected() || e.isIs_actif();
                    return matchSearch && matchType && matchLive && matchActif;
                }).sorted(Comparator.comparing(Evenement::getId).reversed())
                .collect(Collectors.toList());

        updatePagination();
        labelCount.setText(filteredEvenements.size() + " trouvé(s)");
    }

    @FXML
    public void goToAdd() {
        mainController.showEventAdd();
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        filterType.setValue(null);
        filterLive.setSelected(false);
        filterActif.setSelected(false);
        renderCards(allEvenements);
        labelCount.setText(allEvenements.size() + " événement(s)");
    }

    private void editEvenement(Evenement evt) {
        mainController.showEventForm(evt);
    }

    private void deleteEvenement(Evenement evt) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + evt.getTitre() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                service.supprimer(evt.getId());
                loadEvenements();
            }
        });
    }
}