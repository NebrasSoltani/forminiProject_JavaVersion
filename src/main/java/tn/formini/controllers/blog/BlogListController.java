package tn.formini.controllers.blog;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.formini.utils.ListStyleManager;
import javafx.geometry.Pos;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import tn.formini.entities.evenements.Blog;
import tn.formini.services.evenementsService.BlogService;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BlogListController implements Initializable {

    @FXML private FlowPane blogGrid;

    @FXML private TextField            searchField;
    @FXML private ComboBox<String>     filterCategorie;
    @FXML private ComboBox<String>     filterStatus;
    @FXML private Label                labelCount;
    @FXML private Pagination           pagination;

    private MainController mainController;
    private final BlogService blogService = new BlogService();
    private List<Blog> allBlogs = new ArrayList<>();
    private List<Blog> filteredBlogs = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 6;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilters();
        loadBlogs();
        setupPagination();
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
    }

    private void setupFilters() {
        filterCategorie.setItems(FXCollections.observableArrayList(
                "Tous", "Technologie", "Formation", "Événement", "Actualité"
        ));
        filterStatus.setItems(FXCollections.observableArrayList(
                "Tous", "Publié", "Brouillon"
        ));
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterCategorie.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterStatus.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void loadBlogs() {
        allBlogs = blogService.afficher();
        // SORT BY ID DESCENDING (Most recent first)
        allBlogs.sort(Comparator.comparing(Blog::getId).reversed());
        filteredBlogs = new ArrayList<>(allBlogs);
        updatePagination();
        labelCount.setText(allBlogs.size() + " blog(s)");
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredBlogs.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        renderCards(filteredBlogs.subList(0, Math.min(ITEMS_PER_PAGE, filteredBlogs.size())));
    }

    private void renderCards(List<Blog> list) {
        blogGrid.getChildren().clear();
        for (Blog b : list) {
            VBox card = new VBox();
            card.getStyleClass().add("event-card");
            card.setPrefWidth(320);

            StackPane imgHeader = new StackPane();
            imgHeader.getStyleClass().add("card-image-cover");
            Label icon = new Label("📝");
            icon.setStyle("-fx-font-size: 40px;");
            imgHeader.getChildren().add(icon);

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

            actions.getChildren().addAll(btnEdit, btnDelete);

            body.getChildren().addAll(badges, title, details, actions);
            card.getChildren().addAll(imgHeader, body);
            blogGrid.getChildren().add(card);
        }
    }


    private void applyFilters() {
        if (allBlogs == null) return;
        String search = searchField.getText().toLowerCase();
        String cat    = filterCategorie.getValue();
        String status = filterStatus.getValue();

        filteredBlogs = allBlogs.stream().filter(b -> {
            boolean matchSearch = search.isEmpty() || b.getTitre().toLowerCase().contains(search);
            boolean matchCat = cat == null || cat.equals("Tous") || b.getCategorie().equalsIgnoreCase(cat);
            boolean matchStatus = status == null || status.equals("Tous")
                    || (status.equals("Publié") && b.isIs_publie())
                    || (status.equals("Brouillon") && !b.isIs_publie());
            return matchSearch && matchCat && matchStatus;
        }).sorted(Comparator.comparing(Blog::getId).reversed())
          .collect(Collectors.toList());

        updatePagination();
        labelCount.setText(filteredBlogs.size() + " trouvé(s)");
    }

    @FXML
    public void goToAdd() {
        mainController.showBlogAdd();
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        filterCategorie.setValue(null);
        filterStatus.setValue(null);
        updatePagination();
        labelCount.setText(allBlogs.size() + " blog(s)");
    }

    private void editBlog(Blog blog) {
        mainController.showBlogForm(blog);
    }

    private void deleteBlog(Blog blog) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le blog \"" + blog.getTitre() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                blogService.supprimer(blog.getId());
                loadBlogs();
            }
        });
    }
}