package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import tn.formini.controllers.blog.BlogFormController;
import tn.formini.controllers.blog.BlogListController;
import tn.formini.controllers.evenement.EvenementFormController;
import tn.formini.controllers.evenement.EvenementListController;
import tn.formini.controllers.order.OrderListController;
import tn.formini.controllers.produits.ProduitFormController;
import tn.formini.controllers.produit.ProduitListController;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label     labelPageTitle;
    @FXML private Label     labelDate;
    @FXML private Label     labelUserName;
    @FXML private Label     labelUserRole;

    @FXML private VBox sidebar;
    @FXML private Circle userAvatar;

    @FXML private Button btnDashboard;
    @FXML private Button btnBlogList;
    @FXML private Button btnBlogAdd;
    @FXML private Button btnEventList;
    @FXML private Button btnEventAdd;
    @FXML private Button btnProductList;
    @FXML private Button btnStageList;
    @FXML private Button btnProductAdd;
    @FXML private Button btnCandidatures;
    @FXML private Button btnProductManage;
    @FXML private Button btnOrderManage;

    private List<Button> navButtons;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        labelDate.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        );
        navButtons = Arrays.asList(btnDashboard, btnBlogList, btnBlogAdd, btnEventList, btnEventAdd, btnProductList, btnProductAdd, btnProductManage, btnOrderManage, btnStageList);
        showDashboard();
    }

    private void updateActiveButton(Button activeBtn) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-btn-active");
            if (btn == activeBtn) btn.getStyleClass().add("nav-btn-active");
        }
    }

    // ── Chargement dynamique ────────────────────────────────

    private Object loadPage(String fxmlPath) {
        try {
            System.out.println("DEBUG: Loading FXML: " + fxmlPath);
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("FXML introuvable : " + fxmlPath);
                return null;
            }
            System.out.println("DEBUG: FXML found at: " + resource);

            FXMLLoader loader = new FXMLLoader(resource);
            Node page = loader.load();
            System.out.println("DEBUG: FXML loaded successfully");
            Object controller = loader.getController();
            System.out.println("DEBUG: Controller: " + (controller != null ? controller.getClass().getSimpleName() : "null"));

            System.out.println("DEBUG: ContentArea before: " + contentArea.getChildren().size() + " children");
            contentArea.getChildren().setAll(page);
            System.out.println("DEBUG: ContentArea after: " + contentArea.getChildren().size() + " children");
            System.out.println("DEBUG: Page loaded: " + (page != null ? page.getClass().getSimpleName() : "null"));
            
            return controller;
        } catch (Exception e) {
            System.err.println("DEBUG: Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void showEventForm(tn.formini.entities.evenements.Evenement evt) {
        updateActiveButton(btnEventAdd);
        labelPageTitle.setText(evt == null ? "Nouvel Événement" : "Modifier l'Événement");
        EvenementFormController controller = (EvenementFormController) loadPage("/fxml/evenement/EvenementForm.fxml");
        if (controller != null) {
            controller.setMainController(this);
            if (evt != null) {
                controller.setEvenement(evt);
            }
        }
    }

    // ── Actions boutons sidebar ─────────────────────────────

    @FXML
    public void showDashboard() {
        labelPageTitle.setText("Tableau de bord");
        loadPage("/fxml/dashboard/Dashboard.fxml");
        updateActiveButton(btnDashboard);
    }

    @FXML
    public void showEventList() {
        labelPageTitle.setText("Liste des Événements");
        EvenementListController c = (EvenementListController) loadPage("/fxml/evenement/Evenementlist.fxml");
        if (c != null) c.setMainController(this);
        updateActiveButton(btnEventList);
    }

    @FXML
    public void showEventAdd() {
        showEventForm(null);
    }

    @FXML
    public void openSettings() {
        System.out.println("Settings cliqué");
    }

    @FXML
    public void switchToPublic() {
        try {
            URL resource = getClass().getResource("/fxml/frontend/FrontMain.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Label getLabelUserName() { return labelUserName; }
    public Label getLabelUserRole() { return labelUserRole; }

    public void showBlogList() {
        labelPageTitle.setText("Liste des Blogs");
        tn.formini.controllers.blog.BlogListController c = (tn.formini.controllers.blog.BlogListController) loadPage("/fxml/blog/Bloglist.fxml");
        if (c != null) c.setMainController(this);
        updateActiveButton(btnBlogList);
    }

    public void showBlogAdd() {
        showBlogForm(null);
    }

    public void showBlogForm(tn.formini.entities.evenements.Blog blog) {
        updateActiveButton(btnBlogAdd);
        labelPageTitle.setText(blog == null ? "Nouveau Blog" : "Modifier le Blog");
        tn.formini.controllers.blog.BlogFormController controller = (tn.formini.controllers.blog.BlogFormController) loadPage("/fxml/blog/BlogForm.fxml");
        if (controller != null) {
            controller.setMainController(this);
            if (blog != null) {
                controller.setBlog(blog);
            }
        }
    }

    // ── Product Management ─────────────────────────────────────

    @FXML
    public void showProductList() {
        labelPageTitle.setText("Liste des Produits");
        loadPage("/fxml/produits/ProduitList.fxml");
        updateActiveButton(btnProductList);
    }

    @FXML
    public void showProductManage() {
        System.out.println("=== DEBUG: showProductManage() method STARTED! ===");
        labelPageTitle.setText("Gérer les Produits");
        
        // First, create a simple visible test to verify contentArea works
        VBox testBox = new VBox();
        testBox.setStyle("-fx-background-color: #2196F3; -fx-padding: 20; -fx-alignment: center;");
        testBox.setPrefSize(400, 200);
        
        Label testLabel = new Label("LOADING PRODUCT LIST...");
        testLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        testBox.getChildren().add(testLabel);
        contentArea.getChildren().setAll(testBox);
        
        System.out.println("SUCCESS: Loading test content displayed");
        
        // Now try to load actual FXML
        try {
            System.out.println("DEBUG: Loading product list FXML...");
            
            // Check if resource exists first
            String fxmlPath = "/fxml/product/ProduitList.fxml";
            URL resource = getClass().getResource(fxmlPath);
            System.out.println("STEP 1 - Resource check: " + (resource != null ? "FOUND" : "NOT FOUND"));
            System.out.println("STEP 2 - Resource path: " + fxmlPath);
            
            if (resource == null) {
                System.err.println("ERROR: FXML resource not found at: " + fxmlPath);
                System.out.println("INFO: Keeping loading test content visible");
                return;
            }
            
            System.out.println("STEP 3 - Resource URL: " + resource.toExternalForm());
            
            // Try to load FXML
            try {
                FXMLLoader loader = new FXMLLoader(resource);
                System.out.println("STEP 4 - FXMLLoader created");
                
                Node page = loader.load();
                System.out.println("STEP 5 - FXML loaded successfully");
                
                Object controller = loader.getController();
                System.out.println("STEP 6 - Controller: " + (controller != null ? controller.getClass().getSimpleName() : "NULL"));
                
                if (controller != null) {
                    // Replace content
                    contentArea.getChildren().setAll(page);
                    System.out.println("STEP 7 - Content replaced successfully");
                    
                    try {
                        ProduitListController plc = (ProduitListController) controller;
                        plc.setMainController(this);
                        System.out.println("SUCCESS: MainController set for ProduitListController");
                        System.out.println("SUCCESS: Product management interface loaded!");
                    } catch (ClassCastException e) {
                        System.err.println("ERROR: Failed to cast controller: " + e.getMessage());
                    }
                } else {
                    System.err.println("ERROR: Controller was null - FXML loading failed");
                    System.out.println("INFO: Keeping loading test content visible");
                }
                
            } catch (Exception loadException) {
                System.err.println("ERROR: Exception during FXML loading: " + loadException.getMessage());
                loadException.printStackTrace();
                System.out.println("INFO: Keeping loading test content visible due to loading error");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception during FXML loading: " + e.getMessage());
            e.printStackTrace();
            System.out.println("INFO: Keeping loading test content visible due to loading error");
        }
        
        updateActiveButton(btnProductManage);
    }

    @FXML
    public void showProductAdd() {
        showProductForm(null);
    }

    @FXML
    public void showProductForm(tn.formini.entities.produits.Produit produit) {
        updateActiveButton(btnProductAdd);
        labelPageTitle.setText(produit == null ? "Nouveau Produit" : "Modifier le Produit");
        ProduitFormController controller = (ProduitFormController) loadPage("/fxml/produits/ProduitForm_Compact.fxml");
        if (controller != null) {
            controller.setMainController(this);
            if (produit != null) {
                controller.setProduit(produit);
            }
        }
    }

    // Order Management
    @FXML
    public void showOrderManage() {
        labelPageTitle.setText("Gérer les Commandes");
        OrderListController controller = (OrderListController) loadPage("/fxml/order/OrderList.fxml");
        if (controller != null) {
            controller.setMainController(this);
        }
        updateActiveButton(btnOrderManage);
    }

    @FXML
    public void showStageManagement() {
        labelPageTitle.setText("Gestion des Stages");
        loadPage("/fxml/stages/stage-management.fxml");
        updateActiveButton(btnStageList);
    }

    @FXML
    public void showSocieteOffres() {
        labelPageTitle.setText("Mes Offres de Stage");
        tn.formini.controllers.stages.StageManagementController controller = (tn.formini.controllers.stages.StageManagementController) loadPage("/fxml/stages/stage-management.fxml");
        if (controller != null) {
            controller.setSelectedTab(0);
        }
        updateActiveButton(btnStageList);
    }

    @FXML
    public void showSocieteCandidatures() {
        labelPageTitle.setText("Candidatures Reçues");
        tn.formini.controllers.stages.StageManagementController controller = (tn.formini.controllers.stages.StageManagementController) loadPage("/fxml/stages/stage-management.fxml");
        if (controller != null) {
            controller.setSelectedTab(1);
        }
        updateActiveButton(btnStageList);
    }
}