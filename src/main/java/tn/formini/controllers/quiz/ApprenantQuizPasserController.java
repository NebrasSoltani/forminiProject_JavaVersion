package tn.formini.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import tn.formini.entities.Apprenant;
import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.entities.Reponse;
import tn.formini.entities.ResultatQuiz;
import tn.formini.services.ApprenantQuizService;
import tn.formini.services.QuestionService;
import tn.formini.services.ReponseService;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ApprenantQuizPasserController implements Initializable {

    @FXML private Label quizTitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label questionEnonceLabel;
    @FXML private VBox reponsesContainer;
    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;
    @FXML private Button btnSoumettre;
    @FXML private Label errorLabel;

    @FXML private VBox mainQuizBox;
    @FXML private ScrollPane resultBox;
    @FXML private VBox resultHeaderBox;
    @FXML private Label resultTitleLabel;
    @FXML private Label resultNoteLabel;
    @FXML private Label resultBonnesLabel;

    @FXML private VBox assistantBox;
    @FXML private Label assistantScoreLabel;
    @FXML private Label assistantScoreDetailLabel;
    @FXML private Label assistantSummaryLabel;
    @FXML private Label assistantAnalysisTitleLabel;
    @FXML private Label assistantTextLabel;
    
    @FXML private VBox detailsContainer;

    private Apprenant apprenant;
    private int formationId;
    private Quiz quiz;
    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    
    // Maps Question ID -> Selected Reponse ID
    private Map<Integer, Integer> reponsesUtilisateur = new HashMap<>();

    private final ApprenantQuizService apprenantQuizService = new ApprenantQuizService();
    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    private ToggleGroup toggleGroup;
    
    private javafx.animation.Timeline timeline;
    private int timeSeconds;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toggleGroup = new ToggleGroup();
    }

    public void initData(Apprenant apprenant, int formationId, int quizId) {
        this.apprenant = apprenant;
        this.formationId = formationId;
        
        Map<String, Object> data = apprenantQuizService.passer(apprenant, formationId, quizId);
        if (data.containsKey("erreur") || data.containsKey("avertissement")) {
            errorLabel.setText((String) data.getOrDefault("erreur", data.get("avertissement")));
            btnSuivant.setDisable(true);
            return;
        }

        this.quiz = (Quiz) data.get("quiz");
        quizTitleLabel.setText(quiz.getTitre());

        // Setup Timer
        timeSeconds = quiz.getDuree() * 60;
        updateTimerLabel();

        timeline = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
            timeSeconds--;
            updateTimerLabel();
            if (timeSeconds <= 0) {
                timeline.stop();
                soumettreQuiz(); // Auto-submit when time is up
            }
        }));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);

        // Load actual questions since passer() only returns strings
        List<Question> allQuestions = questionService.getAll();
        this.questions = allQuestions.stream()
                .filter(q -> q.getQuiz() != null && q.getQuiz().getId() == quizId)
                .collect(Collectors.toList());

        if (quiz.isMelanger()) {
            Collections.shuffle(this.questions);
        }

        if (questions.isEmpty()) {
            errorLabel.setText("Ce quiz ne contient aucune question.");
            btnSuivant.setDisable(true);
        } else {
            afficherQuestion(0);
            timeline.play(); // Start the timer
        }
    }

    private void updateTimerLabel() {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        timerLabel.setText(String.format("⏱ Temps restant: %02d:%02d", minutes, seconds));
        
        if (timeSeconds <= 60) {
            timerLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;"); // Turn red in last minute
        } else {
            timerLabel.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        }
    }

    private void afficherQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        
        // Save current selection before moving
        sauvegarderSelection(currentIndex);

        this.currentIndex = index;
        Question currentQ = questions.get(index);

        progressLabel.setText(String.format("Question %d / %d", index + 1, questions.size()));
        if (progressBar != null) {
            progressBar.setProgress((double)(index + 1) / questions.size());
        }
        questionEnonceLabel.setText(currentQ.getEnonce());
        
        reponsesContainer.getChildren().clear();
        toggleGroup.getToggles().clear();

        List<Reponse> reponses = reponseService.getAll().stream()
                .filter(r -> r.getQuestion() != null && r.getQuestion().getId() == currentQ.getId())
                .collect(Collectors.toList());

        Integer reponsePrecedenteId = reponsesUtilisateur.get(currentQ.getId());

        char letter = 'A';
        for (Reponse r : reponses) {
            javafx.scene.layout.HBox answerBox = new javafx.scene.layout.HBox(15);
            answerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label lblLetter = new Label(String.valueOf(letter));
            lblLetter.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 14px;");

            RadioButton rb = new RadioButton();
            rb.setUserData(r.getId());
            rb.setToggleGroup(toggleGroup);
            rb.setMouseTransparent(true); // Let the HBox handle the clicks
            
            Label lblTexte = new Label(r.getTexte());
            lblTexte.setWrapText(true);
            lblTexte.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            javafx.scene.layout.HBox.setHgrow(lblTexte, javafx.scene.layout.Priority.ALWAYS);

            // Premium HBox Styling (Looks like a Card)
            String defaultStyle = "-fx-background-color: white; -fx-padding: 15 20; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;";
            String hoverStyle   = "-fx-background-color: #f8fafc; -fx-padding: 15 20; -fx-border-color: #8b5cf6; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(139,92,246,0.15), 15, 0, 0, 5);";
            String selectedStyle= "-fx-background-color: #f5f3ff; -fx-padding: 15 20; -fx-border-color: #7c3aed; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;";
            
            String defaultTextStyle = "-fx-text-fill: #1e293b; -fx-font-size: 16px;";
            String hoverTextStyle   = "-fx-text-fill: #7c3aed; -fx-font-size: 16px;";
            String selectedTextStyle= "-fx-text-fill: #6d28d9; -fx-font-size: 16px; -fx-font-weight: bold;";

            answerBox.setStyle(defaultStyle);
            lblTexte.setStyle(defaultTextStyle);
            answerBox.setMaxWidth(Double.MAX_VALUE);
            answerBox.setPrefWidth(800);

            // Hover effects
            answerBox.setOnMouseEntered(e -> {
                if (!rb.isSelected()) {
                    answerBox.setStyle(hoverStyle);
                    lblTexte.setStyle(hoverTextStyle);
                    lblLetter.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #7c3aed; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 14px;");
                    answerBox.setTranslateX(5);
                }
            });
            answerBox.setOnMouseExited(e -> {
                if (!rb.isSelected()) {
                    answerBox.setStyle(defaultStyle);
                    lblTexte.setStyle(defaultTextStyle);
                    lblLetter.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 14px;");
                    answerBox.setTranslateX(0);
                }
            });

            // Click on the entire box selects the radio
            answerBox.setOnMouseClicked(e -> rb.setSelected(true));

            // Selection effects
            rb.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    answerBox.setStyle(selectedStyle);
                    lblTexte.setStyle(selectedTextStyle);
                    lblLetter.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 14px;");
                    answerBox.setTranslateX(5);
                } else {
                    answerBox.setStyle(defaultStyle);
                    lblTexte.setStyle(defaultTextStyle);
                    lblLetter.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 14px;");
                    answerBox.setTranslateX(0);
                }
            });

            if (reponsePrecedenteId != null && reponsePrecedenteId == r.getId()) {
                rb.setSelected(true);
            }
            
            answerBox.getChildren().addAll(lblLetter, rb, lblTexte);
            reponsesContainer.getChildren().add(answerBox);
            letter++;
        }

        updateButtons();
    }

    private void sauvegarderSelection(int index) {
        if (index < 0 || index >= questions.size()) return;
        Question currentQ = questions.get(index);
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            Integer repId = (Integer) selectedToggle.getUserData();
            reponsesUtilisateur.put(currentQ.getId(), repId);
        } else {
            reponsesUtilisateur.remove(currentQ.getId());
        }
    }

    private void updateButtons() {
        btnPrecedent.setVisible(currentIndex > 0);
        btnPrecedent.setManaged(currentIndex > 0);

        boolean isLast = currentIndex == questions.size() - 1;
        btnSuivant.setVisible(!isLast);
        btnSuivant.setManaged(!isLast);
        
        btnSoumettre.setVisible(isLast);
        btnSoumettre.setManaged(isLast);
    }

    @FXML
    public void questionSuivante() {
        afficherQuestion(currentIndex + 1);
    }

    @FXML
    public void questionPrecedente() {
        afficherQuestion(currentIndex - 1);
    }

    @FXML
    public void soumettreQuiz() {
        if (timeline != null) {
            timeline.stop();
        }
        sauvegarderSelection(currentIndex);

        // Verification if all questions answered
        if (reponsesUtilisateur.size() < questions.size()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "Vous n'avez pas répondu à toutes les questions. Voulez-vous quand même soumettre ?", 
                ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.YES) {
                if (timeline != null && timeSeconds > 0) {
                    timeline.play(); // Resume if they cancelled
                }
                return;
            }
        }

        try {
            ResultatQuiz resultat = apprenantQuizService.soumettre(apprenant, formationId, quiz.getId(), reponsesUtilisateur);
            
            // Hide quiz UI and show results
            mainQuizBox.setVisible(false);
            mainQuizBox.setManaged(false);
            btnSoumettre.setVisible(false);

            resultBox.setVisible(true);
            resultBox.setManaged(true);
            
            if (btnAssistantIcon != null) {
                btnAssistantIcon.setVisible(true);
                btnAssistantIcon.setManaged(true);
            }

            resultNoteLabel.setText(resultat.getNote() + "%");
            resultBonnesLabel.setText(String.format("Vous avez obtenu %s%% au quiz (Seuil de réussite : %s%%)", 
                resultat.getNote(), quiz.getNote_minimale()));

            if (resultat.isReussi()) {
                resultTitleLabel.setText("🎉 Évaluation Réussie !");
                resultTitleLabel.setStyle("-fx-text-fill: #34d399; -fx-font-size: 28px; -fx-font-weight: 900;");
                resultNoteLabel.setStyle("-fx-text-fill: #34d399; -fx-font-size: 42px; -fx-font-weight: 900;");
            } else {
                resultTitleLabel.setText("⚠️ Évaluation Non Réussie");
                resultTitleLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 28px; -fx-font-weight: 900;");
                resultNoteLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 42px; -fx-font-weight: 900;");
            }

            // Populate the "Détail de vos réponses"
            genererDetailsReponses();

            // Set up Assistant Static Fields
            assistantScoreLabel.setText(resultat.getNote() + "%");
            assistantScoreDetailLabel.setText(resultat.getNombre_bonnes_reponses() + "/" + resultat.getNombre_total_questions() + " bonnes réponses");

            // Prepare Gemini Analysis in background without showing it yet
            analyserAvecGemini(resultat);

        } catch (Exception e) {
            errorLabel.setText("Erreur lors de la soumission : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private Button btnVoice;

    @FXML
    public void dicterReponse() {
        btnVoice.setText("🔴 Écoute (2.5s)...");
        btnVoice.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 6; -fx-font-weight: bold;");
        btnVoice.setDisable(true);

        new Thread(() -> {
            try {
                // Record 2.5 seconds of audio
                javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(16000, 16, 1, true, false);
                javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, format);
                
                if (!javax.sound.sampled.AudioSystem.isLineSupported(info)) {
                    javafx.application.Platform.runLater(() -> {
                        errorLabel.setText("Microphone non détecté ou non supporté.");
                        resetVoiceButton();
                    });
                    return;
                }
                
                javax.sound.sampled.TargetDataLine line = (javax.sound.sampled.TargetDataLine) javax.sound.sampled.AudioSystem.getLine(info);
                line.open(format);
                line.start();
                
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                byte[] data = new byte[line.getBufferSize() / 5];
                
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < 2500) { // 2.5 seconds
                    int numBytesRead = line.read(data, 0, data.length);
                    out.write(data, 0, numBytesRead);
                }
                
                line.stop();
                line.close();
                out.close();

                // Convert RAW to WAV
                byte[] audioBytes = out.toByteArray();
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(audioBytes);
                javax.sound.sampled.AudioInputStream ais = new javax.sound.sampled.AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());
                
                java.io.ByteArrayOutputStream wavOut = new java.io.ByteArrayOutputStream();
                javax.sound.sampled.AudioSystem.write(ais, javax.sound.sampled.AudioFileFormat.Type.WAVE, wavOut);
                
                String base64Audio = java.util.Base64.getEncoder().encodeToString(wavOut.toByteArray());

                // Send to Gemini 1.5 Flash (Multimodal Audio Input)
                String apiKey = "AIzaSyD-CxH5LqUzN6Tj8NP16QuGws7wSBgGfkE";
                String envKey = System.getenv("GEMINI_API_KEY");
                if (envKey != null && !envKey.trim().isEmpty()) {
                    apiKey = envKey;
                }
                
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;

                org.json.JSONObject jsonBody = new org.json.JSONObject();
                org.json.JSONArray contents = new org.json.JSONArray();
                org.json.JSONObject content = new org.json.JSONObject();
                org.json.JSONArray parts = new org.json.JSONArray();

                org.json.JSONObject textPart = new org.json.JSONObject();
                textPart.put("text", "Écoute cet audio. L'utilisateur dicte le numéro d'une réponse de QCM (1, 2, 3 ou 4). Réponds UNIQUEMENT avec le chiffre arabe (ex: 1). Si tu n'entends aucun numéro clair de 1 à 4, réponds 0. N'écris aucun texte supplémentaire.");
                parts.put(textPart);

                org.json.JSONObject audioPart = new org.json.JSONObject();
                org.json.JSONObject inlineData = new org.json.JSONObject();
                inlineData.put("mimeType", "audio/wav");
                inlineData.put("data", base64Audio);
                audioPart.put("inlineData", inlineData);
                parts.put(audioPart);

                content.put("parts", parts);
                contents.put(content);
                jsonBody.put("contents", contents);

                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                        .build();

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(response.body());
                    String textResponse = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text").trim();

                    int detectedIndex = -1;
                    try {
                        detectedIndex = Integer.parseInt(textResponse) - 1; // 1-based (user says "1") to 0-based index
                    } catch (Exception ex) {
                        // Ignore, fallback to index -1
                    }

                    int finalIndex = detectedIndex;
                    javafx.application.Platform.runLater(() -> {
                        if (finalIndex >= 0 && finalIndex < toggleGroup.getToggles().size()) {
                            Toggle t = toggleGroup.getToggles().get(finalIndex);
                            t.setSelected(true);
                            errorLabel.setText("");
                        } else {
                            errorLabel.setText("L'IA n'a pas compris de numéro valide (1, 2, 3 ou 4). Veuillez répéter.");
                        }
                    });
                } else {
                    String err = response.body();
                    System.err.println("API ERROR: " + err);
                    String shortErr = err.length() > 100 ? err.substring(0, 100) + "..." : err;
                    javafx.application.Platform.runLater(() -> errorLabel.setText("Erreur API (" + response.statusCode() + "): " + shortErr));
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> errorLabel.setText("Erreur du microphone. Veuillez vérifier vos autorisations."));
            } finally {
                javafx.application.Platform.runLater(this::resetVoiceButton);
            }
        }).start();
    }

    private void resetVoiceButton() {
        btnVoice.setText("🎤 Dicter");
        btnVoice.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        btnVoice.setDisable(false);
    }

    @FXML
    public void fermer() {
        ((Stage) btnSoumettre.getScene().getWindow()).close();
    }

    @FXML private Button btnAssistantIcon;

    @FXML
    public void afficherAssistant() {
        if (btnAssistantIcon != null) {
            btnAssistantIcon.setVisible(false);
            btnAssistantIcon.setManaged(false);
        }
        assistantBox.setVisible(true);
        assistantBox.setManaged(true);
    }

    @FXML
    public void cacherAssistant() {
        assistantBox.setVisible(false);
        assistantBox.setManaged(false);
        if (btnAssistantIcon != null) {
            btnAssistantIcon.setVisible(true);
            btnAssistantIcon.setManaged(true);
        }
    }

    private void genererDetailsReponses() {
        detailsContainer.getChildren().clear();
        int index = 1;
        for (Question q : questions) {
            Integer userRepId = reponsesUtilisateur.get(q.getId());
            List<Reponse> reps = reponseService.getAll().stream()
                    .filter(r -> r.getQuestion() != null && r.getQuestion().getId() == q.getId())
                    .collect(Collectors.toList());

            VBox questionBox = new VBox(10);
            questionBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");
            
            Label lblNum = new Label("Question " + index);
            lblNum.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-padding: 4 10; -fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            Label lblEnonce = new Label(q.getEnonce());
            lblEnonce.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 16px; -fx-font-weight: bold;");
            lblEnonce.setWrapText(true);
            lblEnonce.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

            VBox repsBox = new VBox(8);
            for (Reponse r : reps) {
                boolean isUserChoice = (userRepId != null && userRepId == r.getId());
                boolean isCorrect = r.isEst_correcte();
                
                String text = r.getTexte();
                if (isUserChoice && isCorrect) {
                    text = "✅ " + text + " (Votre réponse correcte)";
                } else if (isUserChoice && !isCorrect) {
                    text = "❌ " + text + " (Votre réponse incorrecte)";
                } else if (!isUserChoice && isCorrect) {
                    text = "🎯 " + text + " (La bonne réponse)";
                } else {
                    text = "⚪ " + text;
                }
                
                Label lblRep = new Label(text);
                lblRep.setWrapText(true);
                lblRep.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
                
                if (isUserChoice && isCorrect) {
                    lblRep.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-background-color: #d1fae5; -fx-padding: 6 12; -fx-background-radius: 6;");
                } else if (isUserChoice && !isCorrect) {
                    lblRep.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-color: #fee2e2; -fx-padding: 6 12; -fx-background-radius: 6;");
                } else if (!isUserChoice && isCorrect) {
                    lblRep.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold; -fx-padding: 6 12;");
                } else {
                    lblRep.setStyle("-fx-text-fill: #64748b; -fx-padding: 6 12;");
                }
                repsBox.getChildren().add(lblRep);
            }

            questionBox.getChildren().addAll(lblNum, lblEnonce, repsBox);
            
            // Explication note if exists
            Reponse correctRep = reps.stream().filter(Reponse::isEst_correcte).findFirst().orElse(null);
            if (correctRep != null && correctRep.getExplication_reponse() != null && !correctRep.getExplication_reponse().isEmpty()) {
                Label lblNote = new Label("💡 " + correctRep.getExplication_reponse());
                lblNote.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 13px; -fx-border-color: #bfdbfe; -fx-border-radius: 8;");
                lblNote.setWrapText(true);
                lblNote.setMaxWidth(Double.MAX_VALUE);
                questionBox.getChildren().add(lblNote);
            }

            detailsContainer.getChildren().add(questionBox);
            index++;
        }
    }

    private void analyserAvecGemini(ResultatQuiz resultat) {
        // Just prepare the text, don't show the box automatically
        javafx.application.Platform.runLater(() -> {
            assistantTextLabel.setText("L'assistant analyse vos réponses... ⏳");
        });

        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es un assistant d'apprentissage bienveillant. ");
        prompt.append("L'étudiant vient de terminer le quiz '").append(quiz.getTitre()).append("'.\n");
        prompt.append("Sa note est de ").append(resultat.getNote()).append("%.\n\n");

        boolean hasErrors = false;
        for (Question q : questions) {
            Integer userRepId = reponsesUtilisateur.get(q.getId());
            List<Reponse> reps = reponseService.getAll().stream()
                    .filter(r -> r.getQuestion() != null && r.getQuestion().getId() == q.getId())
                    .collect(Collectors.toList());

            Reponse userRep = null;
            Reponse correctRep = null;
            for (Reponse r : reps) {
                if (r.isEst_correcte()) correctRep = r;
                if (userRepId != null && r.getId() == userRepId) userRep = r;
            }

            if (userRepId == null || (userRep != null && !userRep.isEst_correcte())) {
                hasErrors = true;
                prompt.append("- Question : ").append(q.getEnonce()).append("\n");
                prompt.append("  Sa réponse : ").append(userRep != null ? userRep.getTexte() : "Aucune").append("\n");
                prompt.append("  Bonne réponse : ").append(correctRep != null ? correctRep.getTexte() : "Inconnue").append("\n");
                if (correctRep != null && correctRep.getExplication_reponse() != null && !correctRep.getExplication_reponse().isEmpty()) {
                    prompt.append("  Explication du cours : ").append(correctRep.getExplication_reponse()).append("\n");
                }
            }
        }

        if (!hasErrors) {
            assistantTextLabel.setText("Score parfait ! 🌟\nVous n'avez fait aucune erreur. Excellent travail ! Vous maîtrisez parfaitement ce sujet.");
            return;
        }

        prompt.append("\nAnalyse brièvement ces erreurs et explique gentiment pourquoi la bonne réponse est correcte. Sois concis et très encourageant !");

        // Set the Summary text mimicking the screenshot
        String nomApprenant = (apprenant.getUser() != null && apprenant.getUser().getNom() != null) ? apprenant.getUser().getNom() : "l'ami";
        assistantSummaryLabel.setText(String.format("J'ai analysé votre quiz en détail. Voici mon rapport personnalisé pour vous aider à progresser."));
        
        if (resultat.isReussi()) {
            if (resultat.getNote() != null && resultat.getNote().compareTo(new java.math.BigDecimal("100.0")) == 0) {
                assistantAnalysisTitleLabel.setText("✨ Score parfait !");
                assistantAnalysisTitleLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 14px; -fx-font-weight: 900;");
                assistantAnalysisTitleLabel.getParent().setStyle("-fx-background-color: #f0fdf4; -fx-padding: 18; -fx-background-radius: 12; -fx-border-color: #4ade80; -fx-border-width: 0 0 0 5; -fx-border-radius: 12;");
            } else {
                assistantAnalysisTitleLabel.setText("👍 Beau travail !");
                assistantAnalysisTitleLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 14px; -fx-font-weight: 900;");
                assistantAnalysisTitleLabel.getParent().setStyle("-fx-background-color: #f0fdf4; -fx-padding: 18; -fx-background-radius: 12; -fx-border-color: #4ade80; -fx-border-width: 0 0 0 5; -fx-border-radius: 12;");
            }
        } else {
            assistantAnalysisTitleLabel.setText("⚠️ Points à réviser");
            assistantAnalysisTitleLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px; -fx-font-weight: 900;");
            assistantAnalysisTitleLabel.getParent().setStyle("-fx-background-color: #fef2f2; -fx-padding: 18; -fx-background-radius: 12; -fx-border-color: #f87171; -fx-border-width: 0 0 0 5; -fx-border-radius: 12;");
        }

        new Thread(() -> {
            try {
                String apiKey = "AIzaSyD-CxH5LqUzN6Tj8NP16QuGws7wSBgGfkE";
                String envKey = System.getenv("GEMINI_API_KEY");
                if (envKey != null && !envKey.trim().isEmpty()) {
                    apiKey = envKey;
                }
                
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;
                
                org.json.JSONObject jsonBody = new org.json.JSONObject();
                org.json.JSONArray contents = new org.json.JSONArray();
                org.json.JSONObject content = new org.json.JSONObject();
                org.json.JSONArray parts = new org.json.JSONArray();
                org.json.JSONObject part = new org.json.JSONObject();
                
                part.put("text", prompt.toString());
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                jsonBody.put("contents", contents);

                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                        .build();

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(response.body());
                    String textResponse = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    javafx.application.Platform.runLater(() -> assistantTextLabel.setText(textResponse));
                } else {
                    javafx.application.Platform.runLater(() -> assistantTextLabel.setText("L'assistant est indisponible (Erreur API: " + response.statusCode() + "). Vérifiez votre clé GEMINI_API_KEY."));
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> assistantTextLabel.setText("Impossible de contacter l'assistant. Vérifiez votre connexion."));
                e.printStackTrace();
            }
        }).start();
    }
}
