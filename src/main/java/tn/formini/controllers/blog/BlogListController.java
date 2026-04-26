package tn.formini.controllers.blog;

import tn.formini.controllers.MainController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Component;
import tn.formini.entities.evenements.Blog;
import tn.formini.services.evenementsService.BlogService;
import tn.formini.services.ToolsService;
import tn.formini.controllers.evenement.StatisticsController;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BlogListController implements Initializable {

    @FXML private FlowPane blogGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> sortOptions;
    @FXML private Label labelCount;
    @FXML private Pagination pagination;

    private MainController mainController;

    @org.springframework.beans.factory.annotation.Autowired
    private tn.formini.repositories.BlogRepository blogRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext springContext;

    private final ToolsService toolsService = new ToolsService();
    private List<Blog> allBlogs = new ArrayList<>();
    private List<Blog> filteredBlogs = new ArrayList<>();
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
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredBlogs.size());
        
        if (fromIndex >= filteredBlogs.size()) {
            renderCards(new ArrayList<>());
        } else {
            renderCards(filteredBlogs.subList(fromIndex, toIndex));
        }
        
        Region dummy = new Region();
        dummy.setPrefHeight(0);
        return dummy;
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
        loadBlogs();
        setupPagination();
    }

    private void setupFilters() {
        filterCategorie.setItems(FXCollections.observableArrayList(
                "Tous", "Technologie", "Formation", "Événement", "Actualité"
        ));
        filterStatus.setItems(FXCollections.observableArrayList(
                "Tous", "Publié", "Brouillon"
        ));
        sortOptions.setItems(FXCollections.observableArrayList(
                "Défaut", "Titre (A-Z)", "Titre (Z-A)", "Date (Récent)", "Date (Ancien)"
        ));

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterCategorie.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterStatus.valueProperty().addListener((obs, o, n) -> applyFilters());
        sortOptions.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void loadBlogs() {
        allBlogs = blogRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        applyFilters();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredBlogs.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
    }

    private void applyFilters() {
        if (allBlogs == null) return;
        String search = searchField.getText().toLowerCase();
        String cat = filterCategorie.getValue();
        String status = filterStatus.getValue();

        List<Blog> temp = allBlogs.stream().filter(b -> {
            boolean matchSearch = search.isEmpty() || b.getTitre().toLowerCase().contains(search);
            boolean matchCat = cat == null || cat.equals("Tous") || b.getCategorie().equalsIgnoreCase(cat);
            boolean matchStatus = status == null || status.equals("Tous")
                    || (status.equals("Publié") && b.isIs_publie())
                    || (status.equals("Brouillon") && !b.isIs_publie());
            return matchSearch && matchCat && matchStatus;
        }).collect(Collectors.toList());

        String sort = sortOptions.getValue();
        if (sort != null) {
            switch (sort) {
                case "Titre (A-Z)":
                    temp.sort(Comparator.comparing(Blog::getTitre, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "Titre (Z-A)":
                    temp.sort(Comparator.comparing(Blog::getTitre, String.CASE_INSENSITIVE_ORDER).reversed());
                    break;
                case "Date (Récent)":
                    temp.sort((b1, b2) -> {
                        if (b1.getDate_publication() == null) return 1;
                        if (b2.getDate_publication() == null) return -1;
                        return b2.getDate_publication().compareTo(b1.getDate_publication());
                    });
                    break;
                case "Date (Ancien)":
                    temp.sort((b1, b2) -> {
                        if (b1.getDate_publication() == null) return 1;
                        if (b2.getDate_publication() == null) return -1;
                        return b1.getDate_publication().compareTo(b2.getDate_publication());
                    });
                    break;
                default:
                    temp.sort(Comparator.comparing(Blog::getId).reversed());
                    break;
            }
        } else {
            temp.sort(Comparator.comparing(Blog::getId).reversed());
        }

        filteredBlogs = temp;
        updatePagination();
        createPage(0); // Force redraw
        labelCount.setText(filteredBlogs.size() + " trouvé(s)");
    }

    private void renderCards(List<Blog> list) {
        blogGrid.getChildren().clear();
        for (Blog b : list) {
            VBox card = new VBox();
            card.getStyleClass().add("event-card");
            card.setPrefWidth(320);

            StackPane imgHeader = new StackPane();
            imgHeader.getStyleClass().add("card-image-cover");
            imgHeader.setPrefHeight(180);

            boolean imageLoaded = false;
            if (b.getImage() != null && !b.getImage().trim().isEmpty()) {
                try {
                    String path = b.getImage();
                    if (!path.startsWith("http") && !path.startsWith("file:")) {
                        path = "file:" + path;
                    }
                    Image img = new Image(path, 320, 180, true, true);
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(320);
                        iv.setFitHeight(180);
                        iv.setPreserveRatio(true);
                        imgHeader.getChildren().add(iv);
                        imageLoaded = true;
                    }
                } catch (Exception ignored) {}
            }
            if (!imageLoaded) {
                Label icon = new Label("📝");
                icon.setStyle("-fx-font-size: 40px;");
                imgHeader.getChildren().add(icon);
            }

            VBox body = new VBox(15);
            body.setPadding(new javafx.geometry.Insets(25));

            HBox badges = new HBox(10);
            Label badgeType = new Label(b.getCategorie().toUpperCase());
            badgeType.getStyleClass().add("card-badge");
            
            Label statusBadge = new Label(b.isIs_publie() ? "PUBLIÉ" : "BROUILLON");
            statusBadge.setStyle(b.isIs_publie() ? "-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; -fx-padding: 5 15; -fx-background-radius: 5px; -fx-font-size: 11px; -fx-font-weight: bold;" : "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-padding: 5 15; -fx-background-radius: 5px; -fx-font-size: 11px; -fx-font-weight: bold;");
            badges.getChildren().addAll(badgeType, statusBadge);

            Label title = new Label(b.getTitre());
            title.getStyleClass().add("card-title-lg");
            title.setWrapText(true);
            title.setPrefHeight(45);

            VBox details = new VBox(5);
            Label auteur = new Label("👤 Auteur ID: " + b.getAuteur_id());
            auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            details.getChildren().add(auteur);
            
            HBox actions = new HBox(10);
            
            Button btnEdit = new Button("Modifier");
            btnEdit.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> editBlog(b));

            Button btnDelete = new Button("Supprimer");
            btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> deleteBlog(b));

            Button btnTools = new Button("⚙ Outils");
            btnTools.setStyle("-fx-background-color: #6974e8; -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
            btnTools.setOnAction(e -> showTools(b, btnTools));

            actions.getChildren().addAll(btnEdit, btnDelete, btnTools);

            body.getChildren().addAll(badges, title, details, actions);
            card.getChildren().addAll(imgHeader, body);
            blogGrid.getChildren().add(card);
        }
    }

    private void showTools(Blog b, Button source) {
        ContextMenu menu = new ContextMenu();
        MenuItem pdfItem = new MenuItem("📄 Exporter PDF (Détails)");
        pdfItem.setOnAction(e -> exportBlogPDF(b));
        MenuItem qrItem = new MenuItem("📱 Générer QR Code (ID)");
        qrItem.setOnAction(e -> showBlogQRCode(b));
        menu.getItems().addAll(pdfItem, qrItem);
        menu.show(source, Side.BOTTOM, 0, 0);
    }

    @FXML
    public void showBlogStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/evenement/Statistics.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Pane root = loader.load();
            StatisticsController controller = loader.getController();
            
            Map<String, Integer> stats = new HashMap<>();
            for (Blog b : allBlogs) {
                String cat = b.getCategorie();
                if (cat != null && !cat.trim().isEmpty()) {
                    String norm = cat.trim().toLowerCase();
                    stats.put(norm, stats.getOrDefault(norm, 0) + 1);
                }
            }
            controller.setBarData("Répartition des Blogs par Catégorie", stats);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Analytique - Blogs");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToAdd() {
        mainController.showBlogAdd();
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        filterCategorie.setValue("Tous");
        filterStatus.setValue("Tous");
        sortOptions.setValue("Défaut");
        applyFilters();
    }

    private void editBlog(Blog blog) {
        mainController.showBlogForm(blog);
    }

    private void deleteBlog(Blog blog) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le blog \"" + blog.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                blogRepository.delete(blog);
                loadBlogs();
            }
        });
    }

    private void exportBlogPDF(Blog b) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Enregistrer le PDF du blog");
        chooser.setInitialFileName("Blog_" + b.getId() + ".pdf");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        
        java.io.File file = chooser.showSaveDialog(blogGrid.getScene().getWindow());
        
        if (file != null) {
            try {
                toolsService.generateBlogPDF(b, file.getAbsolutePath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF du Blog enregistré !");
                alert.setTitle("Export PDF");
                alert.show();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement : " + ex.getMessage()).show();
            }
        }
    }

    private void showBlogQRCode(Blog b) {
        try {
            String content = "Blog: " + b.getTitre() + "\nCat: " + b.getCategorie() + "\nPublié: " + b.isIs_publie();
            Image qr = toolsService.generateAdvancedQRCode(content, 400);
            
            Stage stage = new Stage();
            stage.setTitle("Scanner - " + b.getTitre());
            
            VBox root = new VBox(20);
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.setPadding(new javafx.geometry.Insets(30));
            root.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            
            Label title = new Label(b.getTitre());
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            
            Label subtitle = new Label("Scanner pour les détails du blog");
            subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
            
            ImageView iv = new ImageView(qr);
            iv.setFitWidth(300);
            iv.setPreserveRatio(true);
            
            Button btnClose = new Button("Fermer");
            btnClose.setStyle("-fx-background-color: #6974e8; -fx-text-fill: white; -fx-padding: 10 30; -fx-background-radius: 10; -fx-cursor: hand;");
            btnClose.setOnAction(e -> stage.close());
            
            root.getChildren().addAll(title, subtitle, iv, btnClose);
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur QR Code : " + ex.getMessage()).show();
        }
    }
}