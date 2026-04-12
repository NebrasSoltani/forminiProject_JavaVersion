package tn.formini.controllers.blog;
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
import tn.formini.entities.Blog;
import tn.formini.services.BlogService;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BlogListController implements Initializable {

    @FXML private TableView<Blog> tableBlogs;
    @FXML private TableColumn<Blog, String> colCategorie;
    @FXML private TableColumn<Blog, String> colTitre;
    @FXML private TableColumn<Blog, String> colAuteur;
    @FXML private TableColumn<Blog, Boolean> colStatus;
    @FXML private TableColumn<Blog, Void> colActions;

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
        setupTable();
        setupFilters();
        loadBlogs();
        setupPagination();
    }

    private void setupTable() {
        ListStyleManager.applyStandardStyle(tableBlogs);
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colAuteur.setCellValueFactory(new PropertyValueFactory<>("auteur_id")); // Showing ID for now, could be name

        // Install tooltips for truncated columns
        ListStyleManager.installTooltip(colTitre);

        // Set default sort visually
        tableBlogs.getSortOrder().add(colTitre);

        colStatus.setCellValueFactory(new PropertyValueFactory<>("is_publie"));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            private final Label label = new Label();
            @Override
            protected void updateItem(Boolean published, boolean empty) {
                super.updateItem(published, empty);
                if (empty || published == null) {
                    setGraphic(null);
                } else {
                    label.setText(published ? "● Publié" : "○ Brouillon");
                    label.getStyleClass().setAll("status-badge-" + (published ? "active" : "inactive"));
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
                    Blog b = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_RIGHT);
                    Button btnE = new Button("✏️"); btnE.getStyleClass().add("btn-edit");
                    btnE.setOnAction(event -> editBlog(b));
                    Button btnD = new Button("🗑️"); btnD.getStyleClass().add("btn-delete");
                    btnD.setOnAction(event -> deleteBlog(b));
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
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredBlogs.size());
        
        if (fromIndex >= filteredBlogs.size()) {
            tableBlogs.setItems(FXCollections.emptyObservableList());
        } else {
            tableBlogs.setItems(FXCollections.observableArrayList(filteredBlogs.subList(fromIndex, toIndex)));
        }
        return tableBlogs;
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
        tableBlogs.setItems(FXCollections.observableArrayList(list));
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