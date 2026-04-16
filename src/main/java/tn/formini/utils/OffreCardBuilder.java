package tn.formini.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.formini.entities.stages.OffreStage;

import java.util.function.Consumer;

public class OffreCardBuilder {

    public static Pane createCard(OffreStage offre, 
                                Consumer<OffreStage> onEdit, 
                                Consumer<OffreStage> onDelete, 
                                Consumer<OffreStage> onView) {
        
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15, 20, 15, 20));
        
        // 1. STYLE DE LA CARTE (White bg, 12px radius, Shadow)
        String statusColor = getStatusColor(offre.getStatut());
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4); " +
                     "-fx-border-color: " + statusColor + "; " +
                     "-fx-border-width: 0 0 0 5; " + // Left border 4-5px solid status color
                     "-fx-border-radius: 12 0 0 12; " +
                     "-fx-cursor: hand;");

        // 2. HOVER EFFECT (translateY -2px + enhanced shadow)
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-translate-y: -3; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 8);"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-translate-y: -3; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 8);", "")));

        // 3. SELECTION EVENT (Click anywhere on card to select/edit)
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                onEdit.accept(offre);
            }
        });

        // --- HEADER SECTION: Logo + Name + Status ---
        VBox leftInfo = new VBox(10);
        leftInfo.setMinWidth(150);
        
        HBox companyHeader = new HBox(10);
        companyHeader.setAlignment(Pos.CENTER_LEFT);
        
        // Company logo circle (40px)
        StackPane logoContainer = new StackPane();
        Circle logoCircle = new Circle(20, Color.web("#f0f2f5"));
        String ent = (offre.getEntreprise() != null && !offre.getEntreprise().isEmpty()) ? offre.getEntreprise() : "Offre";
        Label initial = new Label(ent.substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-weight: bold; -fx-text-fill: #764ba2; -fx-font-size: 16;");
        logoContainer.getChildren().addAll(logoCircle, initial);
        
        VBox companyDetails = new VBox(2);
        Label companyName = new Label(ent);
        companyName.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13;");
        
        // Status badge (pill-shaped, solid background, white text)
        Label statusBadge = new Label(offre.getStatut().toUpperCase());
        statusBadge.setPadding(new Insets(3, 10, 3, 10));
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; " +
                           "-fx-text-fill: white; " +
                           "-fx-font-size: 9; " +
                           "-fx-font-weight: bold; " +
                           "-fx-background-radius: 20;");
        
        companyDetails.getChildren().addAll(companyName, statusBadge);
        companyHeader.getChildren().addAll(logoContainer, companyDetails);

        // --- BODY SECTION: Title + Tags ---
        VBox body = new VBox(8);
        HBox.setHgrow(body, Priority.ALWAYS);
        
        Label title = new Label(offre.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 15;");
        title.setWrapText(true);
        
        HBox tags = new HBox(8);
        tags.getChildren().addAll(
            createTag("📍 " + offre.getLieu(), "#eef2ff", "#4f46e5"),
            createTag("📂 " + offre.getDomaine(), "#f0fdf4", "#16a34a")
        );
        
        body.getChildren().addAll(title, tags);

        // --- FOOTER SECTION: Icon Action Buttons ---
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnView = createIconButton("👁", "#f8f9fa", "#555", "Voir détails");
        Button btnEdit = createIconButton("✏", "#e0f2fe", "#0284c7", "Modifier");
        Button btnDelete = createIconButton("🗑", "#fef2f2", "#dc2626", "Supprimer");
        
        btnView.setOnAction(e -> onView.accept(offre));
        btnEdit.setOnAction(e -> onEdit.accept(offre));
        btnDelete.setOnAction(e -> onDelete.accept(offre));
        
        actions.getChildren().addAll(btnView, btnEdit, btnDelete);

        card.getChildren().addAll(companyHeader, body, actions);
        return card;
    }

    private static String getStatusColor(String status) {
        if (status == null) return "#7f8c8d";
        switch (status.toLowerCase()) {
            case "ouvert": case "actif": return "#2ecc71"; // Green
            case "fermé": case "clôturé": return "#e74c3c"; // Red
            case "en attente": case "pending": return "#f39c12"; // Orange
            default: return "#3498db"; // Blue
        }
    }

    private static Label createTag(String text, String bgColor, String textColor) {
        Label tag = new Label(text);
        tag.setPadding(new Insets(3, 10, 3, 10));
        tag.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-text-fill: " + textColor + "; " +
                    "-fx-font-size: 11; " +
                    "-fx-background-radius: 15;");
        return tag;
    }

    private static Button createIconButton(String icon, String bgColor, String iconColor, String tooltip) {
        Button btn = new Button(icon);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setPrefSize(35, 35);
        btn.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-text-fill: " + iconColor + "; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 20; " +
                    "-fx-cursor: hand;");
        
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: " + iconColor + "; -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-background-color: " + iconColor + "; -fx-text-fill: white;", "")));
        
        return btn;
    }
}
