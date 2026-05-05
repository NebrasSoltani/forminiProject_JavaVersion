package tn.formini.controllers.frontend;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import tn.formini.entities.evenements.Evenement;
import tn.formini.services.evenementsService.EvenementService;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la page d'accueil publique (frontend).
 * Affiche les événements actifs avec filtrage dynamique par type.
 * (Blogs supprimés du frontend Home sur demande.)
 */
@Component
public class HomeController implements Initializable {

    // ── FXML ──────────────────────────────────────────────────────────────────

    /** Zone horizontale défilante des cartes événement */
    @FXML private HBox eventCardsBox;

    /** ComboBox de filtrage par type d'événement */
    @FXML private ComboBox<String> filterCombo;

    /** Label indiquant le nombre de résultats */
    @FXML private Label labelResultats;

    // ── Service (JDBC, comme l'ancien HomeController) ─────────────────────────

    private final EvenementService evenementService = new EvenementService();

    /** Formatage de date affiché sur les cartes */
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

    // ── Données en mémoire ────────────────────────────────────────────────────

    private List<Evenement> tousEvenements = new ArrayList<>();

    // ── Initialisation ────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* Chargement des données en arrière-plan pour ne pas bloquer l'UI */
        new Thread(() -> {
            /* Même appel que l'ancien HomeController qui fonctionnait */
            List<Evenement> evts = evenementService.afficher();

            /* Collecte des types distincts */
            List<String> types = evts.stream()
                    .map(Evenement::getType)
                    .filter(t -> t != null && !t.isBlank())
                    .distinct()
                    .sorted()
                    .toList();

            Platform.runLater(() -> {
                tousEvenements = evts;

                /* Construction du ComboBox (types distincts) */
                List<String> items = new ArrayList<>();
                items.add("— Tous —");
                types.forEach(t -> items.add("📅 " + capitalize(t)));
                filterCombo.getItems().setAll(items);
                filterCombo.setValue("— Tous —");

                /* Écoute de la sélection */
                filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltre(newVal));

                /* Affichage initial */
                afficherEvenements(tousEvenements);
                mettreAJourCompteur();
            });
        }).start();
    }

    // ── Actions FXML ──────────────────────────────────────────────────────────

    /** Bouton "Tout afficher" → réinitialise le filtre */
    @FXML
    public void toutAfficher() {
        filterCombo.setValue("— Tous —");
        afficherEvenements(tousEvenements);
        mettreAJourCompteur();
    }

    // ── Filtrage ──────────────────────────────────────────────────────────────

    /**
     * Applique le filtre sélectionné par type d'événement.
     */
    private void appliquerFiltre(String valeur) {
        if (valeur == null || valeur.equals("— Tous —")) {
            afficherEvenements(tousEvenements);
        } else {
            /* Supprime le préfixe "📅 " */
            String type = valeur.startsWith("📅 ") ? valeur.substring(3).trim() : valeur.trim();
            List<Evenement> filtres = tousEvenements.stream()
                    .filter(e -> e.getType() != null && e.getType().equalsIgnoreCase(type))
                    .toList();
            afficherEvenements(filtres);
        }
        mettreAJourCompteur();
    }

    // ── Rendu des cartes Événement ────────────────────────────────────────────

    private void afficherEvenements(List<Evenement> evts) {
        eventCardsBox.getChildren().clear();
        if (evts.isEmpty()) {
            Label vide = new Label("Aucun événement disponible pour ce filtre.");
            vide.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 30;");
            eventCardsBox.getChildren().add(vide);
            return;
        }
        evts.forEach(e -> eventCardsBox.getChildren().add(creerCarteEvenement(e)));
    }

    /** Construit une carte événement selon la Charte Graphique Formini (Light Theme) */
    private VBox creerCarteEvenement(Evenement e) {
        VBox card = new VBox();
        card.getStyleClass().add("home-card");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        /* Fond: Clair #FFFFFF */
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 5);"
                + "-fx-cursor: hand;");

        /* ── Hover effect accent violet #5B48D9 ── */
        card.setOnMouseEntered(ev -> card.setStyle(card.getStyle()
                + "-fx-effect: dropshadow(three-pass-box, rgba(91,72,217,0.25), 20, 0, 0, 8);"
                + "-fx-translate-y: -6;"));
        card.setOnMouseExited(ev -> {
            card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16;"
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 5);"
                    + "-fx-cursor: hand;");
            card.setTranslateY(0);
        });

        /* ── Image ── Fond Surface #F3F4F6 */
        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(160);
        imgBox.setMaxHeight(160);
        imgBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 16 16 0 0;");
        imgBox.setAlignment(Pos.CENTER);

        Label placeholder = new Label("Image de l'événement");
        placeholder.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");
        imgBox.getChildren().add(placeholder);

        if (e.getImage() != null && !e.getImage().trim().isEmpty()) {
            chargerImageAsync(e.getImage(), imgBox, 280, 160);
        }

        /* ── Corps (Body) de la carte ── */
        VBox body = new VBox(10);
        body.setPadding(new Insets(20));
        body.setAlignment(Pos.TOP_CENTER); // Centrage fidèle à la charte

        /* ── Badge type ── */
        String typeStr = e.getType() != null ? e.getType() : "Formation";
        Label badge = new Label(capitalize(typeStr));
        String badgeColor = switch(typeStr.toLowerCase()) {
            case "atelier" -> "#2563EB";     // Accent Bleu
            case "webinaire" -> "#0BBFA2";   // Accent Vert
            default -> "#5B48D9";            // Primaire 
        };
        badge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white;"
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 16;"
                + "-fx-background-radius: 20;");

        /* ── Titre Principal ── */
        Label titre = new Label(e.getTitre() != null ? e.getTitre() : "Titre de l'événement");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        titre.setMaxWidth(240);
        titre.setWrapText(true);
        titre.setAlignment(Pos.CENTER);
        titre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        /* ── Description courte ── */
        String descStr = e.getDescription() != null ? e.getDescription() : "Description courte...";
        if(descStr.length() > 50) descStr = descStr.substring(0, 50) + "...";
        Label desc = new Label(descStr);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        desc.setMaxWidth(240);
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        /* ── Infos : Lieu, Date, Places ── */
        VBox infosBox = new VBox(4);
        infosBox.setAlignment(Pos.CENTER);
        
        Label lieu = new Label("📍 " + (e.getLieu() != null ? e.getLieu() : "Lieu de l'événement"));
        lieu.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        
        String dateStr = e.getDate_debut() != null ? sdf.format(e.getDate_debut()) : "jj/mm/aaaa";
        Label date = new Label("🗓 " + dateStr);
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        
        String places = e.getNombre_places() != null ? e.getNombre_places() + " place(s) restante(s)" : "N place(s) restante(s)";
        Label placesLbl = new Label("👥 " + places);
        placesLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        
        infosBox.getChildren().addAll(lieu, date,placesLbl);

        /* ── Bouton Principal ── Primaire #5B48D9 */
        Button voir = new Button("Connectez-Vous Pour Participer");
        voir.setStyle("-fx-background-color: #5B48D9; -fx-text-fill: white;"
                + "-fx-font-weight: 600; -fx-font-size: 11px; -fx-padding: 10 20;"
                + "-fx-background-radius: 20; -fx-cursor: hand;");
        voir.setMaxWidth(Double.MAX_VALUE);
        voir.setOnMouseEntered(ev -> voir.setStyle(voir.getStyle().replace("#5B48D9", "#4338ca")));
        voir.setOnMouseExited(ev  -> voir.setStyle(voir.getStyle().replace("#4338ca", "#5B48D9")));

        /* ── Actions (Live / 360) horizontales ── */
        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER);
        actionsRow.setMaxWidth(Double.MAX_VALUE);

        // Bouton LIVE - Alerte #E53E3E
        Button btnLive = new Button("● VOIR LIVE");
        btnLive.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnLive, Priority.ALWAYS);
        if (e.isLive()) {
            btnLive.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white;"
                    + "-fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 8 10;"
                    + "-fx-background-radius: 20; -fx-cursor: hand;");
            
            FadeTransition ft = new FadeTransition(Duration.millis(800), btnLive);
            ft.setFromValue(1.0);
            ft.setToValue(0.4);
            ft.setCycleCount(FadeTransition.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();

            btnLive.setOnAction(ev -> {
                String liveUrl = e.getStream_url();
                if (liveUrl == null || liveUrl.trim().isEmpty()) liveUrl = e.getUrl_live();
                if (liveUrl != null && !liveUrl.trim().isEmpty()) {
                    try {
                        if (!liveUrl.startsWith("http")) liveUrl = "https://" + liveUrl;
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(liveUrl));
                    } catch (Exception ex) {}
                }
            });
        } else {
            btnLive.setDisable(true);
            btnLive.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #9CA3AF;"
                    + "-fx-font-size: 10px; -fx-padding: 8 10; -fx-background-radius: 20;");
        }

        // Bouton 360 - Vert #0BBFA2 / Blue
        Button btn360 = new Button("VOIR 360°");
        btn360.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn360, Priority.ALWAYS);
        if (e.getImage360() != null && !e.getImage360().trim().isEmpty()) {
            btn360.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white;"
                    + "-fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 8 10;"
                    + "-fx-background-radius: 20; -fx-cursor: hand;");
            btn360.setOnAction(ev -> {
                try {
                    String url360 = e.getImage360();
                    if (!url360.startsWith("http")) url360 = "https://" + url360;
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url360));
                } catch (Exception ex) {}
            });
        } else {
            btn360.setDisable(true);
            btn360.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #9CA3AF;"
                    + "-fx-font-size: 10px; -fx-padding: 8 10; -fx-background-radius: 20;");
        }

        actionsRow.getChildren().addAll(btnLive, btn360);

        body.getChildren().addAll(badge, titre, desc, infosBox, voir, actionsRow);
        card.getChildren().addAll(imgBox, body);
        return card;
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    /**
     * Charge une image depuis une URL en arrière-plan et la place dans le StackPane.
     * En cas d'erreur, le placeholder emoji reste visible (erreur silencieuse).
     */
    private void chargerImageAsync(String imageUrl, StackPane container, double w, double h) {
        new Thread(() -> {
            try {
                String url = imageUrl;
                if (!url.startsWith("http") && !url.startsWith("file:")) {
                    url = "file:" + url;
                }
                Image img = new Image(url, w, h, true, true, false);
                if (!img.isError()) {
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(w);
                    iv.setFitHeight(h);
                    iv.setPreserveRatio(true);
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        container.getChildren().add(iv);
                    });
                }
            } catch (Exception ex) {
                /* Erreur silencieuse — le placeholder reste */
                System.out.println("[HomeController] Erreur image : " + ex.getMessage());
            }
        }).start();
    }

    /** Met à jour le compteur total de résultats affichés. */
    private void mettreAJourCompteur() {
        int nb = eventCardsBox.getChildren().size();
        labelResultats.setText(nb + " événement(s) affiché(s)");
    }

    /** Retourne le style du badge selon le type de l'événement. */
    private String badgeCouleurEvenement(String type) {
        String base = "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10;"
                + "-fx-background-radius: 20;";
        if (type == null) return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
        return switch (type.toLowerCase()) {
            case "conference", "conférence" -> base + "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
            case "atelier"    -> base + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
            case "webinaire"  -> base + "-fx-background-color: #f3e8ff; -fx-text-fill: #7c3aed;";
            case "formation"  -> base + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
            case "musique"    -> base + "-fx-background-color: #fce7f3; -fx-text-fill: #be185d;";
            case "technologie"-> base + "-fx-background-color: #cffafe; -fx-text-fill: #0e7490;";
            case "exposition" -> base + "-fx-background-color: #fff7ed; -fx-text-fill: #c2410c;";
            case "innovation" -> base + "-fx-background-color: #f0fdf4; -fx-text-fill: #166534;";
            default           -> base + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
        };
    }

    /** Met en majuscule la première lettre. */
    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
