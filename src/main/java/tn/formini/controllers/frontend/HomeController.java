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
import tn.formini.entities.Evenement;
import tn.formini.services.EvenementService;

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
        if (e.getImage() != null && !e.getImage().trim().isEmpty()) {
            try {
                javafx.scene.image.Image image = new javafx.scene.image.Image(e.getImage());
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
                imageView.setFitWidth(320);
                imageView.setFitHeight(180);
                imageView.setPreserveRatio(false);
                imgHeader.getChildren().add(imageView);
            } catch (Exception ex) {
                Label icon = new Label("📅");
                icon.setStyle("-fx-font-size: 50px;");
                imgHeader.getChildren().add(icon);
            }
        } else {
            Label icon = new Label("📅");
            icon.setStyle("-fx-font-size: 50px;");
            imgHeader.getChildren().add(icon);
        }

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

        Button btn360 = new Button("Voir 360°");
        btn360.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand;");
        btn360.setMaxWidth(Double.MAX_VALUE);

        if (e.getImage360() != null && !e.getImage360().trim().isEmpty()) {
            btn360.setOnAction(event -> {
                String url360 = e.getImage360();
                if (!url360.startsWith("http")) {
                    url360 = "https://" + url360;
                }
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url360));
                } catch (Exception ex) {
                    System.err.println("Erreur d'ouverture 360 : " + ex.getMessage());
                }
            });
        } else {
            btn360.setDisable(true);
            btn360.setText("360° indisponible");
        }

        Button btnLive = new Button();
        btnLive.setMaxWidth(Double.MAX_VALUE);

        if (e.isLive()) {
            btnLive.setText("🔴 Voir en live");
            btnLive.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(800), btnLive);
            ft.setFromValue(1.0);
            ft.setToValue(0.4);
            ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();

            btnLive.setOnAction(event -> {
                String liveUrl = e.getStream_url();
                // Fallback struct
                if (liveUrl == null || liveUrl.trim().isEmpty()) {
                    liveUrl = e.getUrl_live();
                }

                if (liveUrl != null && !liveUrl.trim().isEmpty()) {
                    if (!liveUrl.startsWith("http")) {
                        liveUrl = "https://" + liveUrl;
                    }
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(liveUrl));
                    } catch (Exception ex) {
                        System.err.println("Erreur d'ouverture live : " + ex.getMessage());
                    }
                }
            });
        } else {
            btnLive.setText("⚪ Live indisponible");
            btnLive.setDisable(true);
        }

        body.getChildren().addAll(badge, title, details, btn, btn360, btnLive);
        card.getChildren().addAll(imgHeader, body);

        eventGrid.getChildren().add(card);
    }
}
