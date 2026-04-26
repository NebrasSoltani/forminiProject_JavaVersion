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

    /** Construit une carte événement (230×400 env.) */
    private VBox creerCarteEvenement(Evenement e) {
        VBox card = new VBox();
        card.getStyleClass().add("home-card");
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 12, 0, 0, 4);"
                + "-fx-cursor: hand;");

        /* ── Hover effect ── */
        card.setOnMouseEntered(ev -> card.setStyle(card.getStyle()
                + "-fx-effect: dropshadow(three-pass-box, rgba(99,102,241,0.22), 18, 0, 0, 8);"
                + "-fx-translate-y: -4;"));
        card.setOnMouseExited(ev -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 12, 0, 0, 4);"
                    + "-fx-cursor: hand;");
            card.setTranslateY(0);
        });

        /* ── Image ── */
        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(150);
        imgBox.setMaxHeight(150);
        imgBox.setStyle("-fx-background-color: #dde1f4; -fx-background-radius: 14 14 0 0;");
        imgBox.setAlignment(Pos.CENTER);

        Label placeholder = new Label("📅");
        placeholder.setStyle("-fx-font-size: 40px;");
        imgBox.getChildren().add(placeholder);

        /* Chargement image en arrière-plan (idem ancien HomeController) */
        if (e.getImage() != null && !e.getImage().trim().isEmpty()) {
            chargerImageAsync(e.getImage(), imgBox, 250, 150);
        }

        /* ── Badge LIVE clignotant ── */
        if (e.isLive()) {
            Label liveBadge = new Label("🔴 LIVE");
            liveBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;"
                    + "-fx-font-size: 10px; -fx-font-weight: bold;"
                    + "-fx-padding: 3 8; -fx-background-radius: 20;");
            FadeTransition ft = new FadeTransition(Duration.millis(700), liveBadge);
            ft.setFromValue(1.0);
            ft.setToValue(0.2);
            ft.setCycleCount(FadeTransition.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();
            StackPane.setAlignment(liveBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(liveBadge, new Insets(8));
            imgBox.getChildren().add(liveBadge);
        }

        /* ── Badge type ── */
        String typeLabel = e.getType() != null ? e.getType().toUpperCase() : "ÉVÉNEMENT";
        Label badge = new Label(typeLabel);
        badge.setStyle(badgeCouleurEvenement(e.getType()));

        /* ── Titre ── */
        Label titre = new Label(e.getTitre() != null ? e.getTitre() : "—");
        titre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titre.setMaxWidth(220);
        titre.setWrapText(true);

        /* ── Date ── */
        String dateStr = e.getDate_debut() != null ? sdf.format(e.getDate_debut()) : "—";
        Label date = new Label("🗓 " + dateStr);
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        /* ── Lieu ── */
        Label lieu = new Label("📍 " + (e.getLieu() != null ? e.getLieu() : "—"));
        lieu.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        lieu.setMaxWidth(220);
        lieu.setWrapText(true);

        /* ── Places ── */
        String places = e.getNombre_places() != null
                ? e.getNombre_places() + " places" : "Places illimitées";
        Label placesLbl = new Label("👥 " + places);
        placesLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        /* ── Bouton "Voir >" ── */
        Button voir = new Button("Voir ›");
        voir.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-font-size: 12px;"
                + "-fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        voir.setMaxWidth(Double.MAX_VALUE);
        voir.setOnMouseEntered(ev -> voir.setStyle(voir.getStyle().replace("#10b981", "#059669")));
        voir.setOnMouseExited(ev  -> voir.setStyle(voir.getStyle().replace("#059669", "#10b981")));

        /* ── Bouton 360° ── */
        Button btn360 = new Button("🌐 Voir 360°");
        btn360.setMaxWidth(Double.MAX_VALUE);
        if (e.getImage360() != null && !e.getImage360().trim().isEmpty()) {
            btn360.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;"
                    + "-fx-font-weight: bold; -fx-font-size: 12px;"
                    + "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand;");
            btn360.setOnAction(ev -> {
                try {
                    String url360 = e.getImage360();
                    if (!url360.startsWith("http")) url360 = "https://" + url360;
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url360));
                } catch (Exception ex) {
                    System.err.println("[HomeController] Erreur 360° : " + ex.getMessage());
                }
            });
        } else {
            btn360.setDisable(true);
            btn360.setText("🌐 360° indisponible");
            btn360.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #cbd5e1;"
                    + "-fx-font-size: 11px; -fx-padding: 8 15; -fx-background-radius: 8;");
        }

        /* ── Bouton Live 🔴 avec animation clignotante ── */
        Button btnLive = new Button();
        btnLive.setMaxWidth(Double.MAX_VALUE);
        if (e.isLive()) {
            btnLive.setText("🔴 Voir en live");
            btnLive.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;"
                    + "-fx-font-weight: bold; -fx-font-size: 12px;"
                    + "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand;");
            /* Animation clignotante */
            FadeTransition ft = new FadeTransition(Duration.millis(800), btnLive);
            ft.setFromValue(1.0);
            ft.setToValue(0.4);
            ft.setCycleCount(FadeTransition.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();
            /* Action : ouvre le stream dans le navigateur */
            btnLive.setOnAction(ev -> {
                String liveUrl = e.getStream_url();
                if (liveUrl == null || liveUrl.trim().isEmpty()) liveUrl = e.getUrl_live();
                if (liveUrl != null && !liveUrl.trim().isEmpty()) {
                    try {
                        if (!liveUrl.startsWith("http")) liveUrl = "https://" + liveUrl;
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(liveUrl));
                    } catch (Exception ex) {
                        System.err.println("[HomeController] Erreur live : " + ex.getMessage());
                    }
                }
            });
        } else {
            btnLive.setText("⚪ Live indisponible");
            btnLive.setDisable(true);
            btnLive.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #cbd5e1;"
                    + "-fx-font-size: 11px; -fx-padding: 8 15; -fx-background-radius: 8;");
        }

        /* ── Assemblage du body ── */
        VBox body = new VBox(8);
        body.setPadding(new Insets(14));
        body.getChildren().addAll(badge, titre, date, lieu, placesLbl, voir, btn360, btnLive);
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
