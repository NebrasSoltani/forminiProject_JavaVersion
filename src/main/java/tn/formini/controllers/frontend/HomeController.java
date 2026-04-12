package tn.formini.controllers.frontend;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import tn.formini.entities.evenements.Evenement;
import tn.formini.services.evenementsService.EvenementService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private FlowPane eventGrid;

    private final EvenementService eventService = new EvenementService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadFeaturedContent();
    }

    private void loadFeaturedContent() {
        try {
            List<Evenement> events = eventService.afficher();
            System.out.println("Home: Chargement des événements... Nb trouvés = " + events.size());
            eventGrid.getChildren().clear();
            events.stream().limit(6).forEach(this::addEventCard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEventCard(Evenement e) {
        VBox card = new VBox();
        card.getStyleClass().add("event-card");
        card.setPrefWidth(320);
        
        StackPane imgHeader = new StackPane();
        imgHeader.getStyleClass().add("card-image-cover");
        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 50px;");
        imgHeader.getChildren().add(icon);
        
        VBox body = new VBox(15);
        body.setPadding(new javafx.geometry.Insets(25));
        
        Label badge = new Label(e.getType().toUpperCase());
        badge.getStyleClass().add("card-badge");
        
        Label title = new Label(e.getTitre());
        title.getStyleClass().add("card-title-lg");
        title.setWrapText(true);
        title.setPrefHeight(50);
        
        VBox details = new VBox(5);
        Label lieu = new Label("📍 " + e.getLieu());
        Label date = new Label("🕒 " + e.getDate_debut());
        lieu.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        details.getChildren().addAll(lieu, date);
        
        Button btn = new Button("Participer maintenant");
        btn.getStyleClass().add("main-button-onix");
        btn.setMaxWidth(Double.MAX_VALUE);
        
        body.getChildren().addAll(badge, title, details, btn);
        card.getChildren().addAll(imgHeader, body);
        
        eventGrid.getChildren().add(card);
    }
}
