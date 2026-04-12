package tn.formini.controllers.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontMainController implements Initializable {

    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showHome();
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

    @FXML public void showHome() { loadPage("/fxml/frontend/Home.fxml"); }
    @FXML public void showEvents() { loadPage("/fxml/evenement/Evenementlist.fxml"); }
    @FXML public void showBlogs() { loadPage("/fxml/blog/Bloglist.fxml"); }

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
