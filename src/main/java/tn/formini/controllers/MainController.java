package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import tn.formini.controllers.blog.Blogformcontroller;
import tn.formini.controllers.blog.BlogListController;
import tn.formini.controllers.evenement.EvenementformController;
import tn.formini.controllers.evenement.EvenementListController;
import tn.formini.controllers.produits.ProduitFormController;

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

    @FXML private Button btnDashboard;
    @FXML private Button btnBlogList;
    @FXML private Button btnBlogAdd;
    @FXML private Button btnEventList;
    @FXML private Button btnEventAdd;
    @FXML private Button btnProductList;
    @FXML private Button btnQuiz;
    @FXML private Button btnProductAdd;

    private List<Button> navButtons;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        labelDate.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        );
        navButtons = Arrays.asList(btnDashboard, btnBlogList, btnBlogAdd, btnEventList, btnEventAdd, btnProductList, btnQuiz, btnProductAdd);
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
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("FXML introuvable : " + fxmlPath);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node page = loader.load();
            Object controller = loader.getController();

            contentArea.getChildren().setAll(page);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void showEventForm(tn.formini.entities.evenements.Evenement evt) {
        updateActiveButton(btnEventAdd);
        labelPageTitle.setText(evt == null ? "Nouvel Événement" : "Modifier l'Événement");
        EvenementformController controller = (EvenementformController) loadPage("/fxml/evenement/Evenementform.fxml");
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
        tn.formini.controllers.blog.Blogformcontroller controller = (tn.formini.controllers.blog.Blogformcontroller) loadPage("/fxml/blog/Blogform.fxml");
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
    public void showQuizDashboard() {
        labelPageTitle.setText("Gestion des Quiz");
        loadPage("/fxml/quiz/Dashboard.fxml");
        updateActiveButton(btnQuiz);
    }

    @FXML
    public void showProductAdd() {
        showProductForm(null);
    }

    public void showProductForm(tn.formini.entities.produits.Produit produit) {
        updateActiveButton(btnProductAdd);
        labelPageTitle.setText(produit == null ? "Nouveau Produit" : "Modifier le Produit");
        ProduitFormController controller = (ProduitFormController) loadPage("/fxml/produits/ProduitForm.fxml");
        if (controller != null) {
            controller.setMainController(this);
            if (produit != null) {
                controller.setProduit(produit);
            }
        }
    }
}