package tn.formini.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.formini.entities.stages.Candidature;

import java.util.function.Consumer;

public class CandidatureCardBuilder {

    public static Pane createCard(Candidature cand, 
                                Consumer<Candidature> onAccept, 
                                Consumer<Candidature> onReject, 
                                Consumer<Candidature> onAnalyse,
                                Consumer<Candidature> onViewFormations,
                                Consumer<Candidature> onSelect,
                                Consumer<Candidature> onViewCV) {
        
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 20, 12, 20));
        
        String statusColor = getStatusColor(cand.getStatut());
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #eee; -fx-border-width: 1; -fx-border-radius: 12;";
        String shadowStyle = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 3);";
        
        card.setStyle(baseStyle + shadowStyle + "-fx-cursor: hand;");

        // HOVER EFFECT
        card.setOnMouseEntered(e -> {
            card.setStyle(baseStyle + "-fx-effect: dropshadow(three-pass-box, rgba(102,126,234,0.15), 15, 0, 0, 5); -fx-translate-y: -2; -fx-cursor: hand;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle + shadowStyle + "-fx-cursor: hand;");
        });

        // SELECTION AU CLIC
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                onSelect.accept(cand);
            }
        });

        // --- AVATAR & INFO ---
        HBox userInfo = new HBox(12);
        userInfo.setAlignment(Pos.CENTER_LEFT);
        userInfo.setMinWidth(180);
        
        StackPane avatar = new StackPane();
        Circle circle = new Circle(18, Color.web("#667eea"));
        Label initial = new Label("C");
        if (cand.getApprenant() != null && cand.getApprenant().getNom() != null && !cand.getApprenant().getNom().isEmpty()) {
            initial.setText(cand.getApprenant().getNom().substring(0, 1).toUpperCase());
        }
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        avatar.getChildren().addAll(circle, initial);
        
        VBox names = new VBox(2);
        String nameStr = (cand.getApprenant() != null) ? cand.getApprenant().getNom() + " " + (cand.getApprenant().getPrenom() != null ? cand.getApprenant().getPrenom() : "") : "Candidat #" + cand.getId();
        Label candId = new Label(nameStr);
        candId.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13;");
        
        String offreRef = (cand.getOffreStage() != null) ? "Offre: " + cand.getOffreStage().getTitre() : "Ref #" + cand.getId();
        Label refLabel = new Label(offreRef);
        refLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");
        names.getChildren().addAll(candId, refLabel);
        userInfo.getChildren().addAll(avatar, names);

        // --- PILL STATUS BADGE ---
        Label statusPill = new Label(cand.getStatut().toUpperCase());
        statusPill.setPadding(new Insets(3, 10, 3, 10));
        statusPill.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-font-size: 9; -fx-font-weight: bold; -fx-background-radius: 20;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // --- ICON ACTIONS ---
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCV = createActionButton("📄", "#fdf2f8", "#db2777", "Voir CV");
        Button btnFormations = createActionButton("🎓", "#eef2ff", "#4f46e5", "Voir Formations");
        Button btnAnalyse = createActionButton("🧠", "#f3e8ff", "#9333ea", "Analyse IA");
        Button btnAccept = createActionButton("✅", "#dcfce7", "#16a34a", "Accepter");
        Button btnReject = createActionButton("❌", "#fee2e2", "#dc2626", "Refuser");
        
        btnCV.setOnAction(e -> onViewCV.accept(cand));
        btnFormations.setOnAction(e -> onViewFormations.accept(cand));
        btnAnalyse.setOnAction(e -> onAnalyse.accept(cand));
        btnAccept.setOnAction(e -> onAccept.accept(cand));
        btnReject.setOnAction(e -> onReject.accept(cand));
        
        actions.getChildren().addAll(btnCV, btnFormations, btnAnalyse, btnAccept, btnReject);

        card.getChildren().addAll(userInfo, statusPill, spacer, actions);
        return card;
    }

    private static String getStatusColor(String status) {
        if (status == null) return "#94a3b8";
        switch (status.toLowerCase()) {
            case "acceptee": case "acceptée": return "#2ecc71";
            case "refusee": case "refusée": return "#e74c3c";
            default: return "#f39c12";
        }
    }

    private static Button createActionButton(String icon, String bg, String text, String tooltipText) {
        Button b = new Button(icon);
        b.setTooltip(new Tooltip(tooltipText));
        b.setPrefSize(34, 34);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + text + "; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 14;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle() + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        b.setOnMouseExited(e -> b.setStyle(b.getStyle().replace("-fx-scale-x: 1.1; -fx-scale-y: 1.1;", "")));
        return b;
    }
}
