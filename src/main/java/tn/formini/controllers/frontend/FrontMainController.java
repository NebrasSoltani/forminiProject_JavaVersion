package tn.formini.controllers.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import tn.formini.controllers.auth.EditProfileController;
import tn.formini.controllers.auth.LoginController;
import tn.formini.controllers.auth.SignupController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontMainController implements Initializable {

    private static FrontMainController instance;
    
    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        showHome();
    }
    
    public static FrontMainController getInstance() {
        return instance;
    }
    
    public static void setInstance(FrontMainController controller) {
        instance = controller;
    }

    private void loadPage(String fxml) {
        try {
            URL resource = getClass().getResource(fxml);
            FXMLLoader loader = new FXMLLoader(resource);
            contentArea.getChildren().setAll((Parent) loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadView(String fxml) {
        loadPage(fxml);
    }

    @FXML public void showHome() { loadPage("/fxml/frontend/Home.fxml"); }

    @FXML public void showShop() { loadPage("/fxml/frontend/Shop.fxml"); }

    @FXML public void showCart() { loadPage("/fxml/frontend/Cart.fxml"); }

    @FXML
    public void showLogin() {
        try {
            URL resource = getClass().getResource("/fxml/auth/Login.fxml");
            if (resource == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            LoginController c = loader.getController();
            if (c != null) {
                c.setOnBack(this::showHome);
            }
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSignup() {
        try {
            URL resource = getClass().getResource("/fxml/auth/Signup.fxml");
            if (resource == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            SignupController c = loader.getController();
            if (c != null) {
                c.setOnBack(this::showHome);
            }
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML public void showEvents() { loadPage("/fxml/evenement/EvenementlistFront.fxml"); }

    @FXML
    public void showEditProfile() {
        try {
            URL resource = getClass().getResource("/fxml/auth/EditProfile.fxml");
            if (resource == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            EditProfileController c = loader.getController();
            if (c != null) {
                c.setOnBack(this::showHome);
            }
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML 
    public void showFormations() {
        showAlert("Formations", "Découvrez nos formations catalogue prochainement !");
    }

    @FXML 
    public void showAbout() {
        showAlert("À Propos", "Apprenez-en plus sur Formini.tn et notre mission.");
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information - " + title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void switchToAdmin() {
        try {
            URL resource = getClass().getResource("/fxml/MainMenu.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
