package tn.formini.controllers.formations;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Lecon;
import tn.formini.services.formations.LeconService;
import tn.formini.services.recommendations.FormationRecommendationService;
import tn.formini.services.recommendations.FormationRecommendationService.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class FormationRecommendationsController {

    @FXML private Label titleLabel;
    @FXML private TabPane recommendationsTabPane;
    @FXML private ListView<BookRecommendation> booksListView;
    @FXML private ListView<VideoRecommendation> videosListView;
    @FXML private WebView booksPreviewWebView;
    @FXML private WebView videosPreviewWebView;
    @FXML private Label bookDetailsLabel;
    @FXML private Label bookAuthorLabel;
    @FXML private Label videoDetailsLabel;
    @FXML private Label videoChannelLabel;
    @FXML private Label booksCountLabel;
    @FXML private Label videosCountLabel;
    @FXML private Label bottomStatusLabel;
    @FXML private Label booksPreviewStatusLabel;
    @FXML private Label videosPreviewStatusLabel;

    private final LeconService leconService = new LeconService();
    private final FormationRecommendationService recommendationService = new FormationRecommendationService();
    private Formation formation;
    private List<Lecon> currentLecons;
    private WebEngine booksWebEngine;
    private WebEngine videosWebEngine;

    public void setFormation(Formation formation) {
        this.formation = formation;
        if (titleLabel != null) {
            titleLabel.setText("📚 Recommandations: " + formation.getTitre());
        }
        currentLecons = leconService.findByFormationId(formation.getId());
        loadRecommendations();
    }

    @FXML
    private void initialize() {
        initializeBooksListView();
        initializeVideosListView();
        initializeWebViews();
    }

    private void initializeWebViews() {
        if (booksPreviewWebView != null) {
            booksWebEngine = booksPreviewWebView.getEngine();
            booksWebEngine.setJavaScriptEnabled(true);
        }

        if (videosPreviewWebView != null) {
            videosWebEngine = videosPreviewWebView.getEngine();
            videosWebEngine.setJavaScriptEnabled(true);
        }
    }

    private void initializeBooksListView() {
        booksListView.setCellFactory(list -> new ListCell<BookRecommendation>() {
            @Override
            protected void updateItem(BookRecommendation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText("📖 " + item.title);
                setStyle("-fx-padding: 10;");
            }
        });

        booksListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                showBookDetails(newItem);
                displayBookPreview(newItem);
                updateStatus("📖 " + newItem.title);
            }
        });
    }

    private void initializeVideosListView() {
        videosListView.setCellFactory(list -> new ListCell<VideoRecommendation>() {
            @Override
            protected void updateItem(VideoRecommendation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText("🎬 " + item.title);
                setStyle("-fx-padding: 10;");
            }
        });

        videosListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                showVideoDetails(newItem);
                playVideo(newItem);
                updateStatus("🎬 " + newItem.title);
            }
        });
    }

    private void displayBookPreview(BookRecommendation book) {
        if (booksWebEngine == null) return;

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
                + "body{font-family:'Segoe UI',Arial,sans-serif;margin:0;padding:30px;background:#f5f5f5;}"
                + ".card{max-width:900px;margin:0 auto;background:white;border-radius:15px;box-shadow:0 10px 30px rgba(0,0,0,0.2);overflow:hidden;}"
                + ".header{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:30px;text-align:center;}"
                + ".header h1{font-size:24px;margin:0;}"
                + ".body{padding:30px;}"
                + ".info{margin-bottom:20px;}"
                + ".label{font-weight:bold;color:#667eea;}"
                + ".value{color:#333;margin-top:5px;}"
                + ".desc{background:#f8f9fa;padding:20px;border-radius:10px;margin-top:20px;}"
                + ".button{display:inline-block;background:#667eea;color:white;padding:10px 20px;text-decoration:none;border-radius:8px;margin-top:20px;}"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div class='header'><h1>📖 " + escapeHtml(book.title) + "</h1></div>"
                + "<div class='body'>"
                + "<div class='info'><div class='label'>✍️ Auteur</div><div class='value'>" + escapeHtml(book.author) + "</div></div>"
                + "<div class='info'><div class='label'>📘 Éditeur</div><div class='value'>" + escapeHtml(book.publisher) + "</div></div>"
                + "<div class='info'><div class='label'>📅 Date</div><div class='value'>" + escapeHtml(book.publishedDate) + "</div></div>"
                + "<div class='desc'><strong>📖 Description</strong><br/><br/>" + escapeHtml(book.description) + "</div>";

        if (book.previewUrl != null && !book.previewUrl.isEmpty()) {
            html += "<a href='" + book.previewUrl + "' class='button' target='_blank'>🔗 Voir sur Google Books</a>";
        }

        html += "</div></div></body></html>";

        booksWebEngine.loadContent(html);
        if (booksPreviewStatusLabel != null) booksPreviewStatusLabel.setText("Aperçu chargé");
    }

    private void playVideo(VideoRecommendation video) {
        if (videosWebEngine == null) return;

        if (video.videoId != null && !video.videoId.isEmpty()) {
            String embedUrl = "https://www.youtube.com/embed/" + video.videoId + "?autoplay=0&rel=0";
            String html = "<!DOCTYPE html><html><head><style>body{margin:0;background:#000;}</style></head>"
                    + "<body><iframe width='100%' height='100%' src='" + embedUrl + "' "
                    + "frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' "
                    + "allowfullscreen></iframe></body></html>";
            videosWebEngine.loadContent(html);
            if (videosPreviewStatusLabel != null) videosPreviewStatusLabel.setText("▶ Vidéo prête");
        }
    }

    private void showBookDetails(BookRecommendation book) {
        bookDetailsLabel.setText(book.title);
        String authorText = "Par " + book.author;
        if (book.publisher != null && !book.publisher.isEmpty()) {
            authorText += " | " + book.publisher;
        }
        if (book.publishedDate != null && !book.publishedDate.isEmpty()) {
            authorText += " | " + book.publishedDate;
        }
        bookAuthorLabel.setText(authorText);
    }

    private void showVideoDetails(VideoRecommendation video) {
        videoDetailsLabel.setText(video.title);
        String channelText = video.channelTitle;
        if (video.publishedAt != null && !video.publishedAt.isEmpty()) {
            channelText += " | " + video.publishedAt;
        }
        videoChannelLabel.setText(channelText);
    }

    private void loadRecommendations() {
        if (formation == null) return;

        updateStatus("⏳ Chargement depuis Google Books & YouTube...");

        new Thread(() -> {
            try {
                RecommendationResult result = recommendationService.getRecommendations(formation);
                Platform.runLater(() -> {
                    booksListView.setItems(FXCollections.observableArrayList(result.books));
                    videosListView.setItems(FXCollections.observableArrayList(result.videos));
                    booksCountLabel.setText(result.books.size() + " livre(s)");
                    videosCountLabel.setText(result.videos.size() + " vidéo(s)");
                    updateStatus("✅ " + result.books.size() + " livres et " + result.videos.size() + " vidéos chargés");

                    if (!result.books.isEmpty()) booksListView.getSelectionModel().selectFirst();
                    if (!result.videos.isEmpty()) videosListView.getSelectionModel().selectFirst();
                });
            } catch (Exception e) {
                Platform.runLater(() -> updateStatus("❌ Erreur: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleRefreshBooks() { loadRecommendations(); }
    @FXML private void handleRefreshVideos() { loadRecommendations(); }

    @FXML private void handlePublishBook() {
        BookRecommendation selected = booksListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Sélection", "Veuillez sélectionner un livre."); return; }
        publishAsLesson(selected.title, selected.description, selected.previewUrl);
    }

    @FXML private void handlePublishVideo() {
        VideoRecommendation selected = videosListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Sélection", "Veuillez sélectionner une vidéo."); return; }
        publishAsLesson(selected.title, selected.description, selected.url);
    }

    @FXML private void handleOpenBook() {
        BookRecommendation selected = booksListView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.previewUrl != null) openUrl(selected.previewUrl);
    }

    @FXML private void handleOpenVideo() {
        VideoRecommendation selected = videosListView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.url != null) openUrl(selected.url);
    }

    @FXML private void handleClose() {
        if (titleLabel.getScene() != null) titleLabel.getScene().getWindow().hide();
    }

    private void publishAsLesson(String title, String description, String url) {
        if (formation == null) return;

        try {
            Lecon newLesson = new Lecon();
            newLesson.setTitre(title.length() > 100 ? title.substring(0, 97) + "..." : title);
            newLesson.setDescription(description != null ? description : "");
            newLesson.setVideo_url(url != null ? url : "");
            newLesson.setFormation(formation);
            newLesson.setOrdre(currentLecons.size() + 1);
            newLesson.setGratuit(true);
            newLesson.setContenu("Recommandation: " + title);
            newLesson.valider();
            leconService.ajouter(newLesson);
            currentLecons = leconService.findByFormationId(formation.getId());
            showAlert("Succès", "Leçon publiée avec succès!");
            updateStatus("✅ Publié: " + title);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void openUrl(String url) {
        try { Desktop.getDesktop().browse(URI.create(url)); }
        catch (Exception e) { showAlert("Erreur", "Impossible d'ouvrir le lien"); }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }

    private void updateStatus(String msg) {
        Platform.runLater(() -> { if (bottomStatusLabel != null) bottomStatusLabel.setText(msg); });
        System.out.println(msg);
    }
}