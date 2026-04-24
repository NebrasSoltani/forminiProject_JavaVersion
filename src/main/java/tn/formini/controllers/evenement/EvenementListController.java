package tn.formini.controllers.evenement;

import tn.formini.controllers.MainController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.formini.entities.evenements.Evenement;
import tn.formini.services.evenementsService.EvenementService;
import tn.formini.services.ToolsService;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class EvenementListController implements Initializable {

    @FXML private FlowPane eventGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> sortOptions;
    @FXML private CheckBox filterLive;
    @FXML private CheckBox filterActif;
    @FXML private Label labelCount;
    @FXML private Pagination pagination;

    private MainController mainController;
    private final EvenementService service = new EvenementService();
    private final ToolsService toolsService = new ToolsService();
    private List<Evenement> allEvenements = new ArrayList<>();
    private List<Evenement> filteredEvenements = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 6;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilters();
        // Contenu chargé via setMainController
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

    public void setMainController(MainController mc) {
        this.mainController = mc;
        loadEvenements();
        setupPagination();
    }

    private void setupFilters() {
        filterType.setItems(FXCollections.observableArrayList(
                "Tous", "Conférence", "Atelier", "Webinaire", "Formation", "Autre"
        ));
        sortOptions.setItems(FXCollections.observableArrayList(
                "Défaut", "Titre (A-Z)", "Titre (Z-A)", "Date (Récent)", "Date (Ancien)"
        ));
        
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterType.valueProperty().addListener((obs, o, n) -> applyFilters());
        sortOptions.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterLive.selectedProperty().addListener((obs, o, n) -> applyFilters());
        filterActif.selectedProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void loadEvenements() {
        allEvenements = service.afficher();
        applyFilters();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredEvenements.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
    }

    private void applyFilters() {
        if (allEvenements == null) return;
        String search = searchField.getText().toLowerCase();
        String type = filterType.getValue();

        List<Evenement> temp = allEvenements.stream().filter(e -> {
            boolean matchSearch = search.isEmpty() || e.getTitre().toLowerCase().contains(search);
            boolean matchType = type == null || type.equals("Tous") || e.getType().equalsIgnoreCase(type);
            boolean matchLive = !filterLive.isSelected() || e.isLive();
            boolean matchActif = !filterActif.isSelected() || e.isIs_actif();
            return matchSearch && matchType && matchLive && matchActif;
        }).collect(Collectors.toList());

        String sort = sortOptions.getValue();
        if (sort != null) {
            switch (sort) {
                case "Titre (A-Z)":
                    temp.sort(Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "Titre (Z-A)":
                    temp.sort(Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER).reversed());
                    break;
                case "Date (Récent)":
                    temp.sort((e1, e2) -> {
                        if (e1.getDate_debut() == null) return 1;
                        if (e2.getDate_debut() == null) return -1;
                        return e2.getDate_debut().compareTo(e1.getDate_debut());
                    });
                    break;
                case "Date (Ancien)":
                    temp.sort((e1, e2) -> {
                        if (e1.getDate_debut() == null) return 1;
                        if (e2.getDate_debut() == null) return -1;
                        return e1.getDate_debut().compareTo(e2.getDate_debut());
                    });
                    break;
                default:
                    temp.sort(Comparator.comparing(Evenement::getId).reversed());
                    break;
            }
        } else {
            temp.sort(Comparator.comparing(Evenement::getId).reversed());
        }

        filteredEvenements = temp;
        updatePagination();
        createPage(0); // Force initial render with sorted data
        labelCount.setText(filteredEvenements.size() + " trouvé(s)");
    }

    private void renderCards(List<Evenement> list) {
        eventGrid.getChildren().clear();
        for (Evenement evt : list) {
            VBox card = new VBox();
            card.getStyleClass().add("event-card");
            card.setPrefWidth(320);

            StackPane imgHeader = new StackPane();
            imgHeader.getStyleClass().add("card-image-cover");
            imgHeader.setPrefHeight(180);
            
            boolean imageLoaded = false;
            if (evt.getImage() != null && !evt.getImage().trim().isEmpty()) {
                try {
                    String path = evt.getImage();
                    if (!path.startsWith("http") && !path.startsWith("file:")) {
                        path = "file:" + path;
                    }
                    Image img = new Image(path, 320, 180, true, true);
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        imgHeader.getChildren().add(iv);
                        imageLoaded = true;
                    }
                } catch (Exception e) {}
            }
            if (!imageLoaded) {
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

            Button btnTools = new Button("⚙ Outils");
            btnTools.setStyle("-fx-background-color: #6974e8; -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            btnTools.setOnAction(e -> showTools(evt, btnTools));

            actions.getChildren().addAll(btnEdit, btnDelete, btnTools);

            body.getChildren().addAll(badges, title, details, actions);
            card.getChildren().addAll(imgHeader, body);
            eventGrid.getChildren().add(card);
        }
    }

    private void showTools(Evenement evt, Button source) {
        ContextMenu menu = new ContextMenu();
        MenuItem pdfItem = new MenuItem("📄 Exporter PDF (Détails)");
        pdfItem.setOnAction(e -> exportEventPDF(evt));
        MenuItem qrItem = new MenuItem("📱 Générer QR Code (ID)");
        qrItem.setOnAction(e -> showEventQRCode(evt));
        menu.getItems().addAll(pdfItem, qrItem);
        menu.show(source, Side.BOTTOM, 0, 0);
    }

    @FXML
    public void showEventStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/evenement/Statistics.fxml"));
            Pane root = loader.load();
            StatisticsController controller = loader.getController();
            
            Map<String, Integer> stats = new HashMap<>();
            for (Evenement e : allEvenements) {
                String type = e.getType();
                if (type != null && !type.trim().isEmpty()) {
                    String norm = type.trim().toLowerCase();
                    stats.put(norm, stats.getOrDefault(norm, 0) + 1);
                }
            }
            controller.setPieData("Répartition des Événements par Type", stats);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Tableau de bord - Statistiques");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToAdd() {
        mainController.showEventAdd();
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        filterType.setValue("Tous");
        sortOptions.setValue("Défaut");
        filterLive.setSelected(false);
        filterActif.setSelected(false);
        applyFilters();
    }

    private void editEvenement(Evenement evt) {
        mainController.showEventForm(evt);
    }

    private void deleteEvenement(Evenement evt) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer \"" + evt.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                service.supprimer(evt.getId());
                loadEvenements();
            }
        });
    }

    private void exportEventPDF(Evenement evt) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Enregistrer le PDF de l'événement");
        chooser.setInitialFileName("Evenement_" + evt.getId() + ".pdf");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        
        java.io.File file = chooser.showSaveDialog(eventGrid.getScene().getWindow());
        
        if (file != null) {
            try {
                toolsService.generateEvenementPDF(evt, file.getAbsolutePath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF enregistré avec succès !");
                alert.setTitle("Export PDF");
                alert.show();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement : " + e.getMessage()).show();
            }
        }
    }

    private void showEventQRCode(Evenement evt) {
        try {
            // Générer URL Google Maps
            String location = evt.getLieu() != null ? evt.getLieu() : "";
            String encodedLocation = java.net.URLEncoder.encode(location, "UTF-8");
            String googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=" + encodedLocation;
            
            Image qr = toolsService.generateAdvancedQRCode(googleMapsUrl, 400);
            
            Stage stage = new Stage();
            stage.setTitle("Scanner - " + evt.getTitre());
            
            VBox root = new VBox(20);
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.setPadding(new javafx.geometry.Insets(30));
            root.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            
            Label title = new Label(evt.getTitre());
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            
            Label subtitle = new Label("Scannez pour voir sur Google Maps");
            subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            
            ImageView iv = new ImageView(qr);
            iv.setFitWidth(300);
            iv.setPreserveRatio(true);
            
            Button btnClose = new Button("Fermer");
            btnClose.setStyle("-fx-background-color: #6974e8; -fx-text-fill: white; -fx-padding: 10 30; -fx-background-radius: 10; -fx-cursor: hand;");
            btnClose.setOnAction(e -> stage.close());
            
            root.getChildren().addAll(title, subtitle, iv, btnClose);
            
            Scene scene = new Scene(root);
            scene.setFill(null); // Transparent background for the scene
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur QR Code : " + e.getMessage()).show();
        }
    }
}