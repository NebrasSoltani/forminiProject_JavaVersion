package tn.formini.controllers.formations;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Lecon;
import tn.formini.services.formations.LeconService;
import tn.formini.services.formations.ProgressionLeconService;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.File;

public class FormationLearningController {

    @FXML
    private Label formationTitleLabel;

    @FXML
    private Label progressionLabel;

    @FXML
    private ListView<Lecon> lessonsListView;

    @FXML
    private Label lessonTitleLabel;

    @FXML
    private Label lessonDescriptionLabel;

    @FXML
    private Label lessonDurationLabel;

    @FXML
    private Label videoStatusLabel;

    @FXML
    private VBox videoContainer;

    @FXML
    private Button completeLessonButton;

    private final LeconService leconService = new LeconService();
    private final ProgressionLeconService progressionLeconService = new ProgressionLeconService();

    private User currentUser;
    private Formation formation;
    private List<Lecon> lecons = new ArrayList<>();
    private Set<Integer> completedLeconIds;
    private Object lessonWebEngine;
    private String currentBrowserVideoUrl;

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        refreshProgression();
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        loadLessons();
    }

    @FXML
    public void initialize() {
        initializeVideoPlayer();

        lessonsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Lecon item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                boolean done = completedLeconIds != null && completedLeconIds.contains(item.getId());
                String prefix = done ? "[OK] " : "[ ] ";
                setText(prefix + item.getOrdre() + ". " + item.getTitre());
            }
        });

        lessonsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                showLesson(newItem);
            }
        });
    }

    @FXML
    private void handleMarkLessonDone() {
        Lecon selected = lessonsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection", "Selectionnez une lecon.");
            return;
        }
        if (currentUser == null) {
            showAlert("Session", "Utilisateur non connecte.");
            return;
        }

        progressionLeconService.markCompleted(currentUser.getId(), selected.getId());
        refreshProgression();
        lessonsListView.refresh();
        showAlert("Progression", "Lecon marquee comme terminee.");
    }

    @FXML
    private void handleClose() {
        if (formationTitleLabel.getScene() != null) {
            formationTitleLabel.getScene().getWindow().hide();
        }
    }

    @FXML
    private void handleOpenVideoInBrowser() {
        if (currentBrowserVideoUrl == null || currentBrowserVideoUrl.isBlank()) {
            showAlert("Video", "Aucun lien video disponible pour cette lecon.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(currentBrowserVideoUrl));
            } else {
                openVideoWithWindowsFallback(currentBrowserVideoUrl);
            }
        } catch (Exception ex) {
            try {
                openVideoWithWindowsFallback(currentBrowserVideoUrl);
            } catch (Exception fallbackEx) {
                showAlert("Video", "Impossible d'ouvrir le lien video: " + fallbackEx.getMessage());
            }
        }
    }

    private void loadLessons() {
        if (formation == null) {
            return;
        }

        formationTitleLabel.setText("Formation: " + formation.getTitre());
        lecons = leconService.findByFormationId(formation.getId());
        lessonsListView.setItems(FXCollections.observableArrayList(lecons));
        refreshProgression();

        if (!lecons.isEmpty()) {
            lessonsListView.getSelectionModel().selectFirst();
        } else {
            lessonTitleLabel.setText("Aucune lecon disponible");
            lessonDescriptionLabel.setText("");
            lessonDurationLabel.setText("");
            videoStatusLabel.setText("Aucune video pour le moment.");
            loadHtmlInPlayer(emptyVideoHtml("Aucune video disponible."));
            completeLessonButton.setDisable(true);
        }
    }

    private void showLesson(Lecon lecon) {
        lessonTitleLabel.setText(lecon.getTitre());
        lessonDescriptionLabel.setText(lecon.getDescription() == null ? "" : lecon.getDescription());
        lessonDurationLabel.setText(lecon.getDuree() == null ? "" : "Duree: " + lecon.getDuree() + " min");

        String rawVideoSource = firstNonBlank(lecon.getVideo_url(), lecon.getFichier());
        String videoUrl = normalizeVideoUrl(rawVideoSource);
        currentBrowserVideoUrl = normalizeBrowserUrl(rawVideoSource, videoUrl);

        if (videoUrl == null) {
            videoStatusLabel.setText("Cette lecon ne contient pas de lien video (video_url/fichier vide).");
            loadHtmlInPlayer(emptyVideoHtml("Pas de video pour cette lecon."));
        } else {
            if (lessonWebEngine == null) {
                videoStatusLabel.setText("Lecteur integre indisponible. Utilisez 'Ouvrir video'.");
                return;
            }

            boolean loaded = loadHtmlInPlayer(buildIframeHtml(videoUrl));
            if (loaded) {
                videoStatusLabel.setText("Lecture video dans la page.");
            } else {
                videoStatusLabel.setText("Lecture integree impossible. Utilisez 'Ouvrir video'.");
            }
        }

        completeLessonButton.setDisable(false);
    }

    private void refreshProgression() {
        if (formation == null || currentUser == null) {
            progressionLabel.setText("Progression: 0%");
            return;
        }

        completedLeconIds = progressionLeconService.findCompletedLeconIds(currentUser.getId(), formation.getId());
        int total = lecons == null ? 0 : lecons.size();
        int done = completedLeconIds == null ? 0 : completedLeconIds.size();
        int percent = total == 0 ? 0 : (int) Math.round((done * 100.0) / total);
        progressionLabel.setText("Progression: " + percent + "% (" + done + "/" + total + ")");
    }

    private String normalizeVideoUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }

        String url = rawUrl.trim();

        if (url.contains("youtube.com/shorts/")) {
            String id = url.substring(url.indexOf("/shorts/") + 8);
            int qmIndex = id.indexOf('?');
            if (qmIndex > 0) {
                id = id.substring(0, qmIndex);
            }
            return "https://www.youtube.com/embed/" + id;
        }

        if (url.contains("youtube.com/watch?v=")) {
            String id = url.substring(url.indexOf("watch?v=") + 8);
            int ampIndex = id.indexOf('&');
            if (ampIndex > 0) {
                id = id.substring(0, ampIndex);
            }
            return "https://www.youtube.com/embed/" + id;
        }
        if (url.contains("youtu.be/")) {
            String id = url.substring(url.lastIndexOf('/') + 1);
            int qmIndex = id.indexOf('?');
            if (qmIndex > 0) {
                id = id.substring(0, qmIndex);
            }
            return "https://www.youtube.com/embed/" + id;
        }

        if (url.contains("youtube.com/embed/")) {
            return url;
        }

        if (isLikelyLocalPath(url)) {
            return new File(url).toURI().toString();
        }

        return url;
    }

    private String normalizeBrowserUrl(String rawSource, String embeddedUrl) {
        if (rawSource == null || rawSource.isBlank()) {
            return embeddedUrl;
        }

        String source = rawSource.trim();
        String youtubeId = extractYoutubeId(source);
        if (youtubeId != null) {
            return "https://www.youtube.com/watch?v=" + youtubeId;
        }

        if (isLikelyLocalPath(source)) {
            return new File(source).toURI().toString();
        }
        return source;
    }

    private String extractYoutubeId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        String value = url.trim();
        if (value.contains("watch?v=")) {
            String id = value.substring(value.indexOf("watch?v=") + 8);
            int amp = id.indexOf('&');
            return amp > 0 ? id.substring(0, amp) : id;
        }
        if (value.contains("youtu.be/")) {
            String id = value.substring(value.lastIndexOf('/') + 1);
            int qm = id.indexOf('?');
            return qm > 0 ? id.substring(0, qm) : id;
        }
        if (value.contains("/shorts/")) {
            String id = value.substring(value.indexOf("/shorts/") + 8);
            int qm = id.indexOf('?');
            return qm > 0 ? id.substring(0, qm) : id;
        }
        if (value.contains("/embed/")) {
            String id = value.substring(value.indexOf("/embed/") + 7);
            int qm = id.indexOf('?');
            return qm > 0 ? id.substring(0, qm) : id;
        }
        return null;
    }

    private String buildIframeHtml(String embedUrl) {
        String safeUrl = embedUrl.replace("\"", "");
        if (isDirectVideoFile(safeUrl)) {
            return "<html><body style='margin:0;background:#111;height:100vh;display:flex;align-items:stretch;'>"
                    + "<video controls autoplay style='width:100%;height:100%;background:#000;'>"
                    + "<source src='" + safeUrl + "'>"
                    + "Votre navigateur ne supporte pas la lecture video."
                    + "</video>"
                    + "</body></html>";
        }

        return "<html style='height:100%;'><body style='margin:0;background:#111;height:100%;overflow:hidden;'>"
                + "<iframe style='width:100%;height:100%;border:0;' src='" + safeUrl + "' "
                + "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' "
                + "allowfullscreen></iframe>"
                + "</body></html>";
    }

    private String emptyVideoHtml(String message) {
        return "<html><body style='font-family:Arial;padding:16px;color:#636e72;'>" + message + "</body></html>";
    }

    private boolean isDirectVideoFile(String url) {
        String lower = url.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".ogg") || lower.endsWith(".m3u8");
    }

    private boolean isLikelyLocalPath(String url) {
        return url.matches("^[a-zA-Z]:\\\\.*") || (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file:"));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private void initializeVideoPlayer() {
        try {
            Class<?> webViewClass = Class.forName("javafx.scene.web.WebView");
            Object webView = webViewClass.getDeclaredConstructor().newInstance();
            webViewClass.getMethod("setMinHeight", double.class).invoke(webView, 340d);

            VBox.setVgrow((Node) webView, javafx.scene.layout.Priority.ALWAYS);
            videoContainer.getChildren().setAll((Node) webView);

            lessonWebEngine = webViewClass.getMethod("getEngine").invoke(webView);
            loadHtmlInPlayer(emptyVideoHtml("Selectionnez une lecon pour lancer la video."));
        } catch (Exception ex) {
            lessonWebEngine = null;
            videoContainer.getChildren().clear();
            videoStatusLabel.setText("Lecteur integre indisponible: javafx-web absent au runtime (" + ex.getClass().getSimpleName() + ").");
        }
    }

    private boolean loadHtmlInPlayer(String html) {
        if (lessonWebEngine == null) {
            return false;
        }
        try {
            lessonWebEngine.getClass().getMethod("loadContent", String.class).invoke(lessonWebEngine, html);
            return true;
        } catch (Exception ex) {
            videoStatusLabel.setText("Impossible de charger la video: " + ex.getMessage());
            return false;
        }
    }

    private void openVideoWithWindowsFallback(String url) throws Exception {
        String cleanUrl = url.replace("\"", "");
        new ProcessBuilder("cmd", "/c", "start", "", cleanUrl).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}











