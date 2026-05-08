package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import tn.formini.controllers.blog.BlogFormController;
import tn.formini.controllers.blog.BlogListController;
import tn.formini.controllers.crud.SocieteCrudController;
import tn.formini.controllers.crud.FormateurCrudController;
import tn.formini.controllers.evenement.EvenementFormController;
import tn.formini.controllers.evenement.EvenementListController;
import tn.formini.controllers.order.OrderListController;
import tn.formini.controllers.produit.ProduitListController;
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
    @FXML private Button btnStageList;
    @FXML private Button btnProductAdd;
    @FXML private Button btnProductManage;
    @FXML private Button btnOrderManage;
    @FXML private Button btnSocieteManage;
    @FXML private Button btnFormateurManage;
    @FXML private Button btnApprenantManage;
    @FXML private Label labelAdminSection;

    private List<Button> navButtons;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        labelDate.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        );
        navButtons = Arrays.asList(
                btnDashboard,
                btnBlogList,
                btnBlogAdd,
                btnEventList,
                btnEventAdd,
                btnProductList,
                btnProductAdd,
                btnProductManage,
                btnOrderManage,
                btnSocieteManage,
                btnFormateurManage,
                btnApprenantManage,
                btnQuiz,
                btnStageList
        );
        
        configurerInterfaceSelonRole();
        showDashboard();
    }

    private void configurerInterfaceSelonRole() {
        tn.formini.services.UsersService.SessionManager session = tn.formini.services.UsersService.SessionManager.getInstance();
        if (session.isLoggedIn()) {
            tn.formini.entities.Users.User user = session.getCurrentUser();
            String role = user.getRole_utilisateur();
            if (role == null || role.trim().isEmpty()) {
                // Fallback to roles JSON if role_utilisateur is missing
                String rolesJson = user.getRoles();
                if (rolesJson != null) {
                    if (rolesJson.contains("ROLE_ADMIN")) role = "admin";
                    else if (rolesJson.contains("ROLE_FORMATEUR")) role = "formateur";
                    else if (rolesJson.contains("ROLE_APPRENANT")) role = "apprenant";
                    else if (rolesJson.contains("ROLE_SOCIETE")) role = "societe";
                }
                if (role == null) role = "apprenant";
                user.setRole_utilisateur(role);
            }
            
            labelUserName.setText(user.getNom() + " " + user.getPrenom());
            labelUserRole.setText(role.toUpperCase());

            boolean isApprenant = session.isApprenant();
            boolean isSociete = session.isSociete();

            if (isApprenant) {
                // Un apprenant ne peut pas ajouter de contenu ni gérer les produits/commandes
                cacherBouton(btnBlogAdd);
                cacherBouton(btnEventAdd);
                cacherBouton(btnProductAdd);
                cacherBouton(btnProductManage);
                cacherBouton(btnOrderManage);
                cacherBouton(btnSocieteManage);
                cacherBouton(btnFormateurManage);
                cacherBouton(btnApprenantManage);
                if (labelAdminSection != null) {
                    labelAdminSection.setVisible(false);
                    labelAdminSection.setManaged(false);
                }
            } else if (isSociete) {
                // Une société se concentre sur les stages
                cacherBouton(btnBlogAdd);
                cacherBouton(btnEventAdd);
                cacherBouton(btnProductAdd);
                cacherBouton(btnProductManage);
                cacherBouton(btnOrderManage);
                cacherBouton(btnSocieteManage);
                cacherBouton(btnFormateurManage);
                cacherBouton(btnApprenantManage);
                cacherBouton(btnQuiz);
                if (labelAdminSection != null) {
                    labelAdminSection.setVisible(false);
                    labelAdminSection.setManaged(false);
                }
            } else if (!session.isAdmin()) {
                // Pour les formateurs et autres rôles non-admin
                cacherBouton(btnBlogAdd);
                cacherBouton(btnEventAdd);
                cacherBouton(btnProductAdd);
                cacherBouton(btnProductManage);
                cacherBouton(btnOrderManage);
                cacherBouton(btnFormateurManage);
                cacherBouton(btnApprenantManage);
                if (labelAdminSection != null) {
                    labelAdminSection.setVisible(false);
                    labelAdminSection.setManaged(false);
                }
            }
        }
    }

    private void cacherBouton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    @FXML
    public void showDashboard() {
        labelPageTitle.setText("Tableau de bord");
        
        tn.formini.services.UsersService.SessionManager session = tn.formini.services.UsersService.SessionManager.getInstance();
        String fxmlPath = "/fxml/dashboard/Dashboard.fxml"; // Fallback
        
        if (session.isApprenant()) {
            fxmlPath = "/fxml/dashboard/apprenant-dashboard.fxml";
        } else if (session.isFormateur()) {
            fxmlPath = "/fxml/dashboard/formateur-dashboard.fxml";
        } else if (session.isAdmin()) {
            fxmlPath = "/fxml/dashboard/admin-dashboard.fxml";
        } else if (session.isSociete()) {
            fxmlPath = "/fxml/dashboard/societe-dashboard.fxml";
        }
        
        Object controller = loadPage(fxmlPath);
        if (controller instanceof tn.formini.controllers.dashboard.DashboardRoleController roleController) {
            roleController.initializeDashboard(session.getCurrentUser());
        }
        updateActiveButton(btnDashboard);
    }

    private void updateActiveButton(Button activeBtn) {
        for (Button btn : navButtons) {
            if (btn == null) continue;
            btn.getStyleClass().remove("nav-btn-active");
            if (btn == activeBtn) btn.getStyleClass().add("nav-btn-active");
        }
    }

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
        EvenementFormController controller = (EvenementFormController) loadPage("/fxml/evenement/Evenementform.fxml");
        if (controller != null) {
            controller.setMainController(this);
            if (evt != null) {
                controller.setEvenement(evt);
            }
        }
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
    public StackPane getContentArea() { return contentArea; }
    public Label getLabelPageTitle() { return labelPageTitle; }

    public void showBlogList() {
        labelPageTitle.setText("Liste des Blogs");
        BlogListController c = (BlogListController) loadPage("/fxml/blog/Bloglist.fxml");
        if (c != null) c.setMainController(this);
        updateActiveButton(btnBlogList);
    }

    public void showBlogAdd() {
        showBlogForm(null);
    }

    public void showBlogForm(tn.formini.entities.evenements.Blog blog) {
        updateActiveButton(btnBlogAdd);
        labelPageTitle.setText(blog == null ? "Nouveau Blog" : "Modifier le Blog");
        BlogFormController controller = (BlogFormController) loadPage("/fxml/blog/Blogform.fxml");
        if (controller != null) {
            controller.setMainController(this);
            if (blog != null) {
                controller.setBlog(blog);
            }
        }
    }

    @FXML
    public void showProductList() {
        labelPageTitle.setText("Liste des Produits");
        loadPage("/fxml/product/ProduitList.fxml");
        updateActiveButton(btnProductList);
    }

    @FXML
    public void showProductManage() {
        labelPageTitle.setText("Gestion des Produits");
        ProduitListController controller = (ProduitListController) loadPage("/fxml/product/ProduitList.fxml");
        if (controller != null) {
            controller.setMainController(this);
        }
        updateActiveButton(btnProductManage);
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
    public void showSocieteManagement() {
        labelPageTitle.setText("Gestion des Sociétés");
        SocieteCrudController controller = (SocieteCrudController) loadPage("/fxml/crud/societe-crud-cards.fxml");
        if (controller != null) {
            controller.setMainController(this);
        }
        updateActiveButton(btnSocieteManage);
    }

    @FXML
    public void showFormateurManagement() {
        labelPageTitle.setText("Gestion des Formateurs");
        FormateurCrudController controller = (FormateurCrudController) loadPage("/fxml/crud/formateur-crud.fxml");
        if (controller != null) {
            controller.setMainController(this);
        }
        updateActiveButton(btnFormateurManage);
    }

    @FXML
    public void showApprenantManagement() {
        labelPageTitle.setText("Gestion des Apprenants");
        tn.formini.controllers.crud.ApprenantCrudController controller = 
            (tn.formini.controllers.crud.ApprenantCrudController) loadPage("/fxml/crud/apprenant-crud.fxml");
        if (controller != null) {
            controller.setMainController(this);
        }
        updateActiveButton(btnApprenantManage);
    }

    public void showFormateurForm(tn.formini.entities.Users.Formateur formateur) {
        labelPageTitle.setText(formateur == null ? "Ajouter un Formateur" : "Modifier un Formateur");
        tn.formini.controllers.crud.FormateurFormController controller = 
            (tn.formini.controllers.crud.FormateurFormController) loadPage("/fxml/crud/formateur-form.fxml");
        if (controller != null) {
            controller.setMainController(this);
            controller.setMode(formateur == null ? 
                tn.formini.controllers.crud.FormateurFormController.Mode.ADD : 
                tn.formini.controllers.crud.FormateurFormController.Mode.EDIT);
            if (formateur != null) {
                controller.setFormateur(formateur);
            }
        }
    }

    @FXML
    public void showFormateurAdd() {
        showFormateurForm(null);
    }

    @FXML
    public void showSocieteOffres() {
        labelPageTitle.setText("Mes Offres de Stage");
        tn.formini.controllers.stages.StageManagementController controller =
                (tn.formini.controllers.stages.StageManagementController) loadPage("/fxml/stages/stage-management.fxml");
        if (controller != null) {
            controller.setSelectedTab(0);
        }
        updateActiveButton(btnStageList);
    }

    @FXML
    public void showSocieteCandidatures() {
        labelPageTitle.setText("Candidatures Reçues");
        tn.formini.controllers.stages.StageManagementController controller =
                (tn.formini.controllers.stages.StageManagementController) loadPage("/fxml/stages/stage-management.fxml");
        if (controller != null) {
            controller.setSelectedTab(1);
        }
        updateActiveButton(btnStageList);
    }

    @FXML
    public void handleLogout() {
        tn.formini.services.UsersService.SessionManager session = tn.formini.services.UsersService.SessionManager.getInstance();
        session.logout();
        try {
            URL resource = getClass().getResource("/fxml/auth/Login.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
