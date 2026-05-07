package tn.formini.controllers.formations;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Lecon;
import tn.formini.services.formations.LeconService;
import tn.formini.services.formations.ProgressionLeconService;
import tn.formini.services.recommendations.FormationRecommendationService;
import tn.formini.services.recommendations.FormationRecommendationService.BookRecommendation;
import tn.formini.services.recommendations.FormationRecommendationService.VideoRecommendation;
import tn.formini.services.recommendations.FormationRecommendationService.RecommendationResult;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.File;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

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

    @FXML
    private Button showRecommendationsButton;

    @FXML
    private TabPane learningTabPane;

    @FXML
    private ListView<BookRecommendation> booksListView;

    @FXML
    private ListView<VideoRecommendation> videosListView;

    @FXML
    private Label bookDetailsLabel;

    @FXML
    private Label videoDetailsLabel;

    @FXML
    private WebView previewWebView;

    @FXML
    private Label previewStatusLabel;

    // Chatbot fields
    @FXML
    private VBox chatMessagesContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private TextField chatInputField;

    @FXML
    private Label chatStatusLabel;

    private final LeconService leconService = new LeconService();
    private final ProgressionLeconService progressionLeconService = new ProgressionLeconService();
    private final FormationRecommendationService recommendationService = new FormationRecommendationService();

    private User currentUser;
    private Formation formation;
    private List<Lecon> lecons = new ArrayList<>();
    private Set<Integer> completedLeconIds;
    private Object lessonWebEngine;
    private String currentBrowserVideoUrl;
    private String currentVideoUrl;
    private WebEngine previewEngine;

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        refreshProgression();
        initializeChatbot();
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        loadLessons();
        loadRecommendations();
        initializeChatbot();
    }

    @FXML
    public void initialize() {
        initializeVideoPlayer();
        initializeLessonListView();
        initializeBooksListView();
        initializeVideosListView();
        initializeChatbot();
    }

    private void initializeLessonListView() {
        lessonsListView.setCellFactory(list -> new ListCell<Lecon>() {
            @Override
            protected void updateItem(Lecon item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                boolean done = completedLeconIds != null && completedLeconIds.contains(item.getId());
                String prefix = done ? "[✓] " : "[ ] ";
                setText(prefix + item.getOrdre() + ". " + item.getTitre());
            }
        });

        lessonsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                showLesson(newItem);
            }
        });
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
                setText(item.toString());
            }
        });

        booksListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                showBookDetails(newItem);
                loadBookPreview(newItem);
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
                setText(item.toString());
            }
        });

        videosListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                showVideoDetails(newItem);
                currentVideoUrl = newItem.url;
                loadVideoPreview(newItem);
            }
        });
    }

    private void initializeChatbot() {
        if (chatMessagesContainer != null && chatMessagesContainer.getChildren().isEmpty()) {
            Platform.runLater(() -> {
                String formationName = formation != null ? formation.getTitre() : "cette formation";
                addMessageToChat("Assistant IA",
                        "Bonjour ! 👋 Je suis votre assistant virtuel pour la formation \"" + formationName + "\".\n\n" +
                                "Je peux vous aider avec :\n" +
                                "• 📖 Les leçons et leur contenu\n" +
                                "• 🎥 Les vidéos recommandées\n" +
                                "• 📚 Les livres et ressources\n" +
                                "• 📊 Votre progression\n\n" +
                                "Posez-moi n'importe quelle question !", "#9b59b6");
            });
        }

        if (chatInputField != null) {
            chatInputField.setOnAction(event -> handleSendChatMessage());
        }
    }

    @FXML
    private void handleSendChatMessage() {
        String userMessage = chatInputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        addMessageToChat("Vous", userMessage, "#3498db");
        chatInputField.clear();
        chatStatusLabel.setText("L'assistant réfléchit...");

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String aiResponse = generateAIResponse(userMessage);

            Platform.runLater(() -> {
                addMessageToChat("Assistant IA", aiResponse, "#9b59b6");
                chatStatusLabel.setText("Prêt à répondre à vos questions");
                if (chatScrollPane != null) {
                    chatScrollPane.setVvalue(1.0);
                }
            });
        }).start();
    }

    @FXML
    private void handleClearChat() {
        if (chatMessagesContainer != null) {
            chatMessagesContainer.getChildren().clear();
            chatStatusLabel.setText("Chat effacé. Posez votre question!");
            String formationName = formation != null ? formation.getTitre() : "cette formation";
            addMessageToChat("Assistant IA",
                    "Chat effacé ! 👋 Je suis toujours là pour vous aider avec la formation \"" + formationName + "\".\n\n" +
                            "N'hésitez pas à me poser vos questions sur :\n" +
                            "• Le contenu des leçons\n" +
                            "• Les vidéos et livres recommandés\n" +
                            "• Votre progression\n" +
                            "• Et bien plus encore !", "#9b59b6");
        }
    }

    private void addMessageToChat(String sender, String message, String colorHex) {
        if (chatMessagesContainer == null) return;

        HBox messageBox = new HBox();
        messageBox.setSpacing(8);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox messageContent = new VBox();
        messageContent.setStyle("-fx-padding: 8 12; -fx-background-color: " + colorHex + "20; -fx-background-radius: 10;");
        messageContent.setMaxWidth(450);

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorHex + "; -fx-font-size: 12px;");

        Text messageText = new Text(message);
        messageText.setWrappingWidth(400);
        TextFlow textFlow = new TextFlow(messageText);

        messageContent.getChildren().addAll(senderLabel, textFlow);

        if (sender.equals("Vous")) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.getChildren().add(messageContent);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            messageBox.getChildren().add(0, spacer);
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.getChildren().add(messageContent);
        }

        chatMessagesContainer.getChildren().add(messageBox);

        if (chatScrollPane != null) {
            chatScrollPane.setVvalue(1.0);
        }
    }

    private String generateAIResponse(String userQuestion) {
        String lowerQuestion = userQuestion.toLowerCase();

        String currentLesson = lessonTitleLabel.getText();
        String formationName = formation != null ? formation.getTitre() : "cette formation";
        String progression = progressionLabel.getText();

        boolean hasLessonSelected = currentLesson != null &&
                !currentLesson.equals("Selectionnez une lecon") &&
                !currentLesson.isEmpty();

        // Bonjour / Salut
        if (lowerQuestion.matches(".*\\b(bonjour|salut|hello|coucou|hey)\\b.*")) {
            return "Bonjour ! 😊 Comment puis-je vous aider avec votre formation \"" + formationName + "\" aujourd'hui ?";
        }

        // Formation
        else if (lowerQuestion.contains("formation") || lowerQuestion.contains("cours") || lowerQuestion.contains("contenu")) {
            int totalLecons = lecons != null ? lecons.size() : 0;
            int completedLecons = completedLeconIds != null ? completedLeconIds.size() : 0;
            return "📚 **" + formationName + "**\n\n" +
                    "• Nombre de leçons : " + totalLecons + "\n" +
                    "• Leçons complétées : " + completedLecons + "\n" +
                    "• Progression : " + progression + "\n\n" +
                    "Les leçons sont listées à gauche. Sélectionnez-en une pour commencer !";
        }

        // Leçon spécifique
        else if (lowerQuestion.contains("leçon") || lowerQuestion.contains("lecon") || lowerQuestion.contains("chapitre")) {
            if (hasLessonSelected) {
                String desc = lessonDescriptionLabel.getText();
                String shortDesc = desc != null && !desc.isEmpty() ?
                        desc.substring(0, Math.min(200, desc.length())) : "Pas de description détaillée";
                return "📖 **Leçon actuelle : " + currentLesson + "**\n\n" +
                        "Description : " + shortDesc + (desc != null && desc.length() > 200 ? "..." : "") + "\n\n" +
                        (lessonDurationLabel.getText() != null && !lessonDurationLabel.getText().isEmpty() ?
                                "⏱️ " + lessonDurationLabel.getText() + "\n\n" : "") +
                        "Voulez-vous que je vous explique un concept spécifique de cette leçon ?";
            } else {
                return "Pour voir le contenu d'une leçon, sélectionnez-la dans la liste à gauche. " +
                        "Ensuite, je pourrai vous donner plus de détails et répondre à vos questions spécifiques !";
            }
        }

        // Progression
        else if (lowerQuestion.contains("progression") || lowerQuestion.contains("avancement") ||
                lowerQuestion.contains("pourcentage") || lowerQuestion.contains("%")) {
            if (currentUser != null) {
                int total = lecons != null ? lecons.size() : 0;
                int done = completedLeconIds != null ? completedLeconIds.size() : 0;
                int remaining = total - done;
                return "📊 **Votre progression** : " + progression + "\n\n" +
                        "• Leçons terminées : " + done + "/" + total + "\n" +
                        "• Leçons restantes : " + remaining + "\n\n" +
                        (remaining > 0 ? "💪 Continue comme ça ! " + remaining + " leçon(s) à compléter." :
                                "🎉 Félicitations ! Vous avez complété toutes les leçons de cette formation !");
            } else {
                return "Veuillez vous connecter pour voir votre progression personnalisée.";
            }
        }

        // Vidéos
        else if (lowerQuestion.contains("video") || lowerQuestion.contains("vidéo") || lowerQuestion.contains("youtube") || lowerQuestion.contains("vimeo")) {
            return "🎥 **Ressources vidéo disponibles :**\n\n" +
                    "• Un onglet 'Vidéos Recommandées' avec des tutoriels YouTube\n" +
                    "• Des vidéos intégrées dans certaines leçons\n" +
                    "• Support des vidéos YouTube et Vimeo\n" +
                    "• Option 'Ouvrir vidéo dans le navigateur' pour meilleure expérience\n\n" +
                    (hasLessonSelected && currentVideoUrl != null ?
                            "La leçon actuelle contient une vidéo. Cliquez sur 'Ouvrir vidéo dans le navigateur' pour la voir !" :
                            "Sélectionnez une leçon pour voir si elle contient une vidéo.");
        }

        // Livres
        else if (lowerQuestion.contains("livre") || lowerQuestion.contains("book") ||
                lowerQuestion.contains("bibliothèque") || lowerQuestion.contains("lecture")) {
            return "📚 **Ressources livresques :**\n\n" +
                    "• Un onglet 'Livres Recommandés' avec des ouvrages pertinents\n" +
                    "• Aperçu disponible pour chaque livre\n" +
                    "• Possibilité de publier un livre comme leçon\n\n" +
                    "Allez dans l'onglet 'Livres Recommandés' pour découvrir ces ressources !";
        }

        // Aide
        else if (lowerQuestion.contains("aide") || lowerQuestion.contains("help") ||
                lowerQuestion.contains("comment") || lowerQuestion.contains("utilisation")) {
            return "💡 **Guide d'utilisation :**\n\n" +
                    "1. Sélectionnez une leçon dans la liste de gauche\n" +
                    "2. Lisez la description et regardez la vidéo\n" +
                    "3. Cliquez 'Marquer leçon terminée' quand vous avez fini\n" +
                    "4. Explorez les onglets 'Livres' et 'Vidéos' pour plus de ressources\n" +
                    "5. Publiez des recommandations comme nouvelles leçons\n\n" +
                    "Posez-moi des questions spécifiques sur n'importe quel concept !";
        }

        // Recommandations
        else if (lowerQuestion.contains("recommandation") || lowerQuestion.contains("suggestion")) {
            return "🎯 **Pour des recommandations personnalisées :**\n\n" +
                    "• Terminez des leçons pour obtenir plus de recommandations\n" +
                    "• Consultez les onglets 'Livres Recommandés' et 'Vidéos Recommandées'\n" +
                    "• Publiez les ressources qui vous intéressent comme nouvelles leçons\n\n" +
                    "Souhaitez-vous que je vous recommande quelque chose de spécifique ?";
        }

        // Difficulté / Explication
        else if (lowerQuestion.contains("difficile") || lowerQuestion.contains("comprendre") ||
                lowerQuestion.contains("explique") || lowerQuestion.contains("expliquer")) {
            return "🤔 Je comprends que certains concepts peuvent être difficiles.\n\n" +
                    "Pour mieux vous aider, pouvez-vous me dire :\n" +
                    "• Quelle leçon ou quel concept spécifique vous pose problème ?\n" +
                    "• Y a-t-il des termes que vous ne comprenez pas ?\n\n" +
                    "Je peux vous fournir des explications supplémentaires ou vous orienter vers des ressources adaptées !";
        }

        // Temps / Durée
        else if (lowerQuestion.contains("temps") || lowerQuestion.contains("durée") ||
                lowerQuestion.contains("combien de temps")) {
            if (hasLessonSelected && lessonDurationLabel.getText() != null && !lessonDurationLabel.getText().isEmpty()) {
                return "⏱️ " + lessonDurationLabel.getText() + "\n\n" +
                        "Conseil : Prévoyez du temps supplémentaire pour la pratique et les exercices, " +
                        "cela renforcera votre compréhension !";
            } else {
                return "La durée varie selon les leçons. Sélectionnez une leçon pour voir sa durée estimée. " +
                        "En général, comptez 15-30 minutes par leçon, plus le temps de pratique.";
            }
        }

        // Merci
        else if (lowerQuestion.contains("merci")) {
            return "Avec plaisir ! 🎉 Je suis ravi de pouvoir vous aider dans votre apprentissage.\n\n" +
                    "N'hésitez pas si vous avez d'autres questions. Bon courage pour la suite de votre formation ! 💪";
        }

        // Par défaut - réponse intelligente
        else {
            return "Merci pour votre question ! 🤔\n\n" +
                    "Pour mieux vous aider, pourriez-vous préciser si vous cherchez des informations sur :\n\n" +
                    "• 📖 Le contenu des leçons\n" +
                    "• 🎥 Les vidéos recommandées\n" +
                    "• 📚 Les livres et ressources\n" +
                    "• 📊 Votre progression\n" +
                    "• 💡 Des conseils d'apprentissage\n\n" +
                    "Ou posez-moi une question plus précise sur le sujet qui vous intéresse !";
        }
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

        // Message de félicitations dans le chat
        if (chatMessagesContainer != null) {
            addMessageToChat("Assistant IA",
                    "Félicitations ! 🎉 Vous avez complété la leçon \"" + selected.getTitre() + "\".\n\n" +
                            "Continuez comme ça ! " + progressionLabel.getText() + " de la formation complétée.", "#9b59b6");
        }
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

    /**
     * Normalise une URL vidéo pour l'embed (YouTube, Vimeo, fichiers locaux)
     */
    private String normalizeVideoUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }

        String url = rawUrl.trim();

        // YouTube Shorts
        if (url.contains("youtube.com/shorts/")) {
            String id = extractYoutubeIdFromShorts(url);
            if (id != null) {
                return "https://www.youtube.com/embed/" + id;
            }
        }

        // YouTube standard
        if (url.contains("youtube.com/watch?v=")) {
            String id = extractYoutubeIdFromWatch(url);
            if (id != null) {
                return "https://www.youtube.com/embed/" + id;
            }
        }

        // youtu.be
        if (url.contains("youtu.be/")) {
            String id = extractYoutubeIdFromShortUrl(url);
            if (id != null) {
                return "https://www.youtube.com/embed/" + id;
            }
        }

        // YouTube embed déjà formaté
        if (url.contains("youtube.com/embed/")) {
            return url;
        }

        // Support Vimeo
        if (url.contains("vimeo.com/") && !url.contains("player.vimeo.com/video/")) {
            String vimeoId = extractVimeoId(url);
            if (vimeoId != null) {
                return "https://player.vimeo.com/video/" + vimeoId;
            }
        }

        // Vimeo embed déjà formaté
        if (url.contains("player.vimeo.com/video/")) {
            return url;
        }

        // Fichiers locaux
        if (isLikelyLocalPath(url)) {
            return new File(url).toURI().toString();
        }

        return url;
    }

    /**
     * Extrait l'ID d'une vidéo YouTube depuis un lien standard
     */
    private String extractYoutubeIdFromWatch(String url) {
        String id = url.substring(url.indexOf("watch?v=") + 8);
        int ampIndex = id.indexOf('&');
        if (ampIndex > 0) {
            id = id.substring(0, ampIndex);
        }
        int qmIndex = id.indexOf('?');
        if (qmIndex > 0) {
            id = id.substring(0, qmIndex);
        }
        return id;
    }

    /**
     * Extrait l'ID d'une vidéo YouTube depuis un lien youtu.be
     */
    private String extractYoutubeIdFromShortUrl(String url) {
        String id = url.substring(url.lastIndexOf('/') + 1);
        int qmIndex = id.indexOf('?');
        if (qmIndex > 0) {
            id = id.substring(0, qmIndex);
        }
        return id;
    }

    /**
     * Extrait l'ID d'une vidéo YouTube Shorts
     */
    private String extractYoutubeIdFromShorts(String url) {
        String id = url.substring(url.indexOf("/shorts/") + 8);
        int qmIndex = id.indexOf('?');
        if (qmIndex > 0) {
            id = id.substring(0, qmIndex);
        }
        return id;
    }

    /**
     * Extrait l'ID d'une vidéo Vimeo depuis différents formats d'URL
     */
    private String extractVimeoId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        String value = url.trim();

        // Format: vimeo.com/123456789
        if (value.contains("vimeo.com/") && !value.contains("player.vimeo.com/video/")) {
            String id = value.substring(value.indexOf("vimeo.com/") + 10);
            // Supprimer les paramètres de requête et les slashes
            int slashIndex = id.indexOf('/');
            if (slashIndex > 0) {
                id = id.substring(0, slashIndex);
            }
            int qmIndex = id.indexOf('?');
            if (qmIndex > 0) {
                id = id.substring(0, qmIndex);
            }
            // Vérifier que c'est bien un nombre
            if (id.matches("\\d+")) {
                return id;
            }
        }

        // Format: player.vimeo.com/video/123456789
        if (value.contains("player.vimeo.com/video/")) {
            String id = value.substring(value.indexOf("player.vimeo.com/video/") + 24);
            int slashIndex = id.indexOf('/');
            if (slashIndex > 0) {
                id = id.substring(0, slashIndex);
            }
            int qmIndex = id.indexOf('?');
            if (qmIndex > 0) {
                id = id.substring(0, qmIndex);
            }
            if (id.matches("\\d+")) {
                return id;
            }
        }

        return null;
    }

    /**
     * Normalise l'URL pour l'ouverture dans le navigateur externe
     */
    private String normalizeBrowserUrl(String rawSource, String embeddedUrl) {
        if (rawSource == null || rawSource.isBlank()) {
            return embeddedUrl;
        }

        String source = rawSource.trim();

        // YouTube
        if (source.contains("youtube.com/watch?v=")) {
            String id = extractYoutubeIdFromWatch(source);
            if (id != null) {
                return "https://www.youtube.com/watch?v=" + id;
            }
        }

        if (source.contains("youtu.be/")) {
            String id = extractYoutubeIdFromShortUrl(source);
            if (id != null) {
                return "https://www.youtube.com/watch?v=" + id;
            }
        }

        if (source.contains("youtube.com/shorts/")) {
            String id = extractYoutubeIdFromShorts(source);
            if (id != null) {
                return "https://www.youtube.com/shorts/" + id;
            }
        }

        // Vimeo
        String vimeoId = extractVimeoId(source);
        if (vimeoId != null) {
            return "https://vimeo.com/" + vimeoId;
        }

        if (isLikelyLocalPath(source)) {
            return new File(source).toURI().toString();
        }

        return source;
    }

    /**
     * Construit le HTML d'iframe pour l'embed vidéo (supporte YouTube et Vimeo)
     */
    private String buildIframeHtml(String embedUrl) {
        String safeUrl = embedUrl.replace("\"", "");

        // Ajouter des paramètres spécifiques selon la plateforme
        if (safeUrl.contains("player.vimeo.com/video/")) {
            // Paramètres Vimeo: autoplay=0, loop=0, title=1, byline=1, portrait=1
            if (!safeUrl.contains("?")) {
                safeUrl += "?autoplay=0&loop=0&title=1&byline=1&portrait=1";
            }
            return buildResponsiveIframe(safeUrl);
        }

        if (safeUrl.contains("youtube.com/embed/")) {
            // Paramètres YouTube: rel=0, modestbranding=1, autoplay=0
            if (!safeUrl.contains("?")) {
                safeUrl += "?rel=0&modestbranding=1&autoplay=0";
            }
            return buildResponsiveIframe(safeUrl);
        }

        if (isDirectVideoFile(safeUrl)) {
            return buildVideoPlayerHtml(safeUrl);
        }

        return buildResponsiveIframe(safeUrl);
    }

    /**
     * Construit un iframe responsive pour les vidéos
     */
    private String buildResponsiveIframe(String embedUrl) {
        return "<html style='height:100%;'>" +
                "<body style='margin:0;background:#111;height:100%;overflow:hidden;'>" +
                "<div style='position:relative;width:100%;height:100%;'>" +
                "<iframe style='position:absolute;top:0;left:0;width:100%;height:100%;border:0;' " +
                "src='" + embedUrl + "' " +
                "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' " +
                "allowfullscreen>" +
                "</iframe>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Construit un lecteur vidéo HTML5 pour les fichiers vidéo locaux
     */
    private String buildVideoPlayerHtml(String videoUrl) {
        return "<html><body style='margin:0;background:#111;height:100vh;display:flex;align-items:stretch;'>" +
                "<video controls autoplay style='width:100%;height:100%;background:#000;'>" +
                "<source src='" + videoUrl + "'>" +
                "Votre navigateur ne supporte pas la lecture video." +
                "</video>" +
                "</body></html>";
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

    private void loadRecommendations() {
        if (formation == null) {
            return;
        }

        try {
            RecommendationResult result = recommendationService.getRecommendations(formation);
            booksListView.setItems(FXCollections.observableArrayList(result.books));
            videosListView.setItems(FXCollections.observableArrayList(result.videos));

            System.out.println("Loaded " + result.books.size() + " book recommendations and " + result.videos.size() + " video recommendations");
            try {
                if (previewWebView != null) {
                    previewEngine = previewWebView.getEngine();
                    previewEngine.loadContent("<html><body style='font-family:Arial;padding:16px;color:#636e72;'>Sélectionnez une ressource pour aperçu.</body></html>");
                }
            } catch (Exception ex) {
                System.out.println("Preview init error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("Error loading recommendations: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showBookDetails(BookRecommendation book) {
        StringBuilder details = new StringBuilder();
        if (book.author != null && !book.author.isEmpty()) {
            details.append("Auteur: ").append(book.author).append("\n");
        }
        if (book.publisher != null && !book.publisher.isEmpty()) {
            details.append("Éditeur: ").append(book.publisher).append("\n");
        }
        if (book.publishedDate != null && !book.publishedDate.isEmpty()) {
            details.append("Date: ").append(book.publishedDate).append("\n");
        }
        if (book.description != null && !book.description.isEmpty()) {
            details.append("\nDescription:\n").append(book.description);
        }
        bookDetailsLabel.setText(details.toString());
    }

    private void showVideoDetails(VideoRecommendation video) {
        StringBuilder details = new StringBuilder();
        if (video.channelTitle != null && !video.channelTitle.isEmpty()) {
            details.append("Chaîne: ").append(video.channelTitle).append("\n");
        }
        if (video.publishedAt != null && !video.publishedAt.isEmpty()) {
            details.append("Date: ").append(video.publishedAt).append("\n");
        }
        if (video.description != null && !video.description.isEmpty()) {
            details.append("\nDescription:\n").append(video.description);
        }
        videoDetailsLabel.setText(details.toString());
    }

    private void loadVideoPreview(VideoRecommendation video) {
        if (previewEngine == null) return;
        try {
            String embed = "https://www.youtube.com/embed/" + video.videoId + "?rel=0&autoplay=0";
            String html = "<html style='height:100%;'><body style='margin:0;background:#111;height:100%;overflow:hidden;'>"
                    + "<iframe style='width:100%;height:100%;border:0;' src='" + embed + "' "
                    + "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' "
                    + "allowfullscreen></iframe>" + "</body></html>";
            previewEngine.loadContent(html);
            if (previewStatusLabel != null) previewStatusLabel.setText("Aperçu vidéo chargé");
        } catch (Exception ex) {
            if (previewStatusLabel != null) previewStatusLabel.setText("Impossible de charger la vidéo en aperçu");
        }
    }

    private void loadBookPreview(BookRecommendation book) {
        if (previewEngine == null) return;
        try {
            if (book.volumeId != null && !book.volumeId.isEmpty()) {
                String embed = "https://books.google.com/books?id=" + book.volumeId + "&printsec=frontcover&source=gbs_api";
                String html = "<html style='height:100%;'><body style='margin:0;height:100%;'>"
                        + "<iframe style='width:100%;height:100%;border:0;' src='" + embed + "'></iframe>" + "</body></html>";
                previewEngine.loadContent(html);
                if (previewStatusLabel != null) previewStatusLabel.setText("Aperçu du livre chargé");
            } else if (book.previewUrl != null && !book.previewUrl.isEmpty()) {
                previewEngine.load(book.previewUrl);
                if (previewStatusLabel != null) previewStatusLabel.setText("Aperçu du livre chargé");
            } else {
                previewEngine.loadContent("<html><body style='font-family:Arial;padding:16px;color:#636e72;'>Aucun aperçu disponible.</body></html>");
                if (previewStatusLabel != null) previewStatusLabel.setText("Aucun aperçu disponible");
            }
        } catch (Exception ex) {
            if (previewStatusLabel != null) previewStatusLabel.setText("Impossible de charger l'aperçu du livre");
        }
    }

    @FXML
    private void handleShowRecommendations() {
        if (learningTabPane != null) {
            learningTabPane.getSelectionModel().selectNext();
        }
    }

    @FXML
    private void handlePublishSelectedBookAsLesson() {
        BookRecommendation selected = booksListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner un livre à publier.");
            return;
        }

        String lessonTitle = sanitizeTitle(selected.title);
        String lessonDescription = selected.description != null ? selected.description : "";
        String sourceUrl = selected.previewUrl != null ? selected.previewUrl : "";
        String sourceLabel = selected.author != null && !selected.author.isEmpty() ?
                "Livre par " + selected.author : "Livre recommandé";
        String extraInfo = "Éditeur: " + (selected.publisher != null ? selected.publisher : "N/A");

        publishRecommendationAsLesson(lessonTitle, lessonDescription, sourceUrl, sourceLabel, extraInfo);
    }

    @FXML
    private void handlePublishSelectedVideoAsLesson() {
        VideoRecommendation selected = videosListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner une vidéo à publier.");
            return;
        }

        String lessonTitle = sanitizeTitle(selected.title);
        String lessonDescription = selected.description != null ? selected.description : "";
        String sourceUrl = selected.url;
        String sourceLabel = "Vidéo YouTube";
        String extraInfo = selected.channelTitle != null && !selected.channelTitle.isEmpty() ?
                "Chaîne: " + selected.channelTitle : "";

        publishRecommendationAsLesson(lessonTitle, lessonDescription, sourceUrl, sourceLabel, extraInfo);
    }

    @FXML
    private void handleRefreshBooks() {
        loadRecommendations();
        showAlert("Actualisation", "Les recommandations ont été actualisées.");
    }

    @FXML
    private void handleRefreshVideos() {
        loadRecommendations();
        showAlert("Actualisation", "Les recommandations ont été actualisées.");
    }

    @FXML
    private void handleOpenBookInBrowser() {
        BookRecommendation selected = booksListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner un livre.");
            return;
        }

        String url = selected.previewUrl;
        if (url == null || url.isEmpty()) {
            showAlert("Lien", "Aucun lien disponible pour ce livre.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                openVideoWithWindowsFallback(url);
            }
        } catch (Exception ex) {
            try {
                openVideoWithWindowsFallback(url);
            } catch (Exception fallbackEx) {
                showAlert("Erreur", "Impossible d'ouvrir le lien: " + fallbackEx.getMessage());
            }
        }
    }

    private void publishRecommendationAsLesson(String lessonTitle, String lessonDescription,
                                               String sourceUrl, String sourceLabel, String extraInfo) {
        if (formation == null) {
            showAlert("Erreur", "Aucune formation sélectionnée.");
            return;
        }

        if (lessonTitle == null || lessonTitle.trim().isEmpty()) {
            showAlert("Validation", "Le titre de la leçon ne peut pas être vide.");
            return;
        }

        String normalizedTitle = normalizeLessonTitle(lessonTitle);

        for (Lecon existing : lecons) {
            if (existing.getTitre().equalsIgnoreCase(normalizedTitle)) {
                showAlert("Duplication", "Une leçon avec ce titre existe déjà.");
                return;
            }
        }

        try {
            Lecon newLecon = new Lecon();
            newLecon.setTitre(normalizedTitle);
            newLecon.setDescription(lessonDescription);
            newLecon.setVideo_url(sourceUrl);
            newLecon.setFormation(formation);
            newLecon.setOrdre(lecons.size() + 1);
            newLecon.setGratuit(true);

            String fullContent = sourceLabel;
            if (extraInfo != null && !extraInfo.isEmpty()) {
                fullContent += "\n" + extraInfo;
            }
            newLecon.setContenu(fullContent);

            newLecon.valider();
            leconService.ajouter(newLecon);
            loadLessons();
            selectPublishedLesson(normalizedTitle, sourceUrl);

            showAlert("Succès", "La leçon a été publiée avec succès!");

            // Notifier dans le chat
            if (chatMessagesContainer != null) {
                addMessageToChat("Assistant IA",
                        "✅ La leçon \"" + normalizedTitle + "\" a été publiée avec succès !\n\n" +
                                "Vous pouvez la retrouver dans la liste des leçons.", "#9b59b6");
            }

        } catch (IllegalArgumentException ex) {
            showAlert("Erreur de validation", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur", "Erreur lors de la publication: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String sanitizeTitle(String title) {
        if (title == null) {
            return "Sans titre";
        }
        return title.replace("\n", " ").replace("\r", " ").trim();
    }

    private String normalizeLessonTitle(String title) {
        String clean = title.trim();
        if (clean.length() <= 255) {
            return clean;
        }
        return clean.substring(0, 255).trim();
    }

    private void selectPublishedLesson(String lessonTitle, String sourceUrl) {
        for (int i = 0; i < lecons.size(); i++) {
            Lecon lesson = lecons.get(i);
            if (lesson.getTitre().equals(lessonTitle) ||
                    (sourceUrl != null && sourceUrl.equals(lesson.getVideo_url()))) {
                lessonsListView.getSelectionModel().select(i);
                lessonsListView.scrollTo(i);
                return;
            }
        }

        if (!lecons.isEmpty()) {
            lessonsListView.getSelectionModel().select(lecons.size() - 1);
            lessonsListView.scrollTo(lecons.size() - 1);
        }
    }
}