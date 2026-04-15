package tn.formini.utils;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.formini.entities.Evenement;

public class EventUiUtils {

    public static StackPane createEventImageHeader(Evenement evt) {
        StackPane imgHeader = new StackPane();
        imgHeader.getStyleClass().add("card-image-cover");
        
        if (evt.getImage() != null && !evt.getImage().trim().isEmpty()) {
            try {
                Image image = new Image(evt.getImage());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(320);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(false);
                imgHeader.getChildren().add(imageView);
            } catch (Exception ex) {
                addDefaultIcon(imgHeader);
            }
        } else {
            addDefaultIcon(imgHeader);
        }
        return imgHeader;
    }

    public static VBox createEventDetails(Evenement evt) {
        VBox details = new VBox(5);
        Label dateLieu = new Label("📍 " + evt.getLieu() + " | 👥 " + evt.getNombre_places() + " places");
        dateLieu.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        details.getChildren().add(dateLieu);
        return details;
    }

    private static void addDefaultIcon(StackPane pane) {
        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 40px;");
        pane.getChildren().add(icon);
    }
}
