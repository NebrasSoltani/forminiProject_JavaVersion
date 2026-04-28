package tn.formini.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.entities.Reponse;
import tn.formini.services.QuestionService;
import tn.formini.services.QuizService;
import tn.formini.services.ReponseService;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GenererQuestionIAController implements Initializable {

    @FXML private Label quizInfoLabel;
    @FXML private ComboBox<Quiz> comboQuiz;
    @FXML private TextArea contextArea;
    @FXML private TextField nbQuestionsField;
    @FXML private TextField pointsField;
    @FXML private CheckBox typeQCM;
    @FXML private CheckBox typeVraiFaux;
    @FXML private CheckBox typeTexteLibre;
    @FXML private Label statusLabel;

    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    
    private Runnable onComplete;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Quiz> quizzes = quizService.getAll();
        comboQuiz.getItems().addAll(quizzes);
        comboQuiz.setConverter(new javafx.util.StringConverter<Quiz>() {
            @Override
            public String toString(Quiz object) {
                return object != null ? object.getTitre() : "";
            }

            @Override
            public Quiz fromString(String string) {
                return null;
            }
        });
        
        comboQuiz.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                quizInfoLabel.setText("Quiz : " + newVal.getTitre());
            } else {
                quizInfoLabel.setText("Quiz : Aucun quiz sélectionné");
            }
        });
    }

    public void initData(Quiz selectedQuiz, Runnable onComplete) {
        this.onComplete = onComplete;
        if (selectedQuiz != null) {
            comboQuiz.setValue(selectedQuiz);
        }
    }

    @FXML
    public void fermer() {
        ((Stage) contextArea.getScene().getWindow()).close();
    }

    @FXML
    public void genererQuestions() {
        Quiz selectedQuiz = comboQuiz.getValue();
        if (selectedQuiz == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un quiz.");
            return;
        }

        String context = contextArea.getText();
        if (context == null || context.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez fournir un sujet ou contexte.");
            return;
        }

        int nbQuestions = 5;
        int points = 1;
        try {
            nbQuestions = Integer.parseInt(nbQuestionsField.getText());
            points = Integer.parseInt(pointsField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Le nombre de questions et les points doivent être des nombres entiers.");
            return;
        }

        List<String> types = new ArrayList<>();
        if (typeQCM.isSelected()) types.add("QCM (4 propositions dont 1 ou plusieurs correctes)");
        if (typeVraiFaux.isSelected()) types.add("Vrai/Faux");
        if (typeTexteLibre.isSelected()) types.add("Texte libre");

        if (types.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner au moins un type de question.");
            return;
        }

        statusLabel.setText("Génération en cours, veuillez patienter... ⏳");
        statusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);

        int finalNbQuestions = nbQuestions;
        int finalPoints = points;
        
        new Thread(() -> {
            try {
                String prompt = buildPrompt(context, finalNbQuestions, finalPoints, types);
                String responseText = callGeminiAPI(prompt);
                
                Platform.runLater(() -> {
                    try {
                        parseAndSaveQuestions(responseText, selectedQuiz, finalPoints);
                        statusLabel.setText("✅ Génération réussie !");
                        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Succès");
                        success.setHeaderText(null);
                        success.setContentText(finalNbQuestions + " questions générées et ajoutées au quiz avec succès !");
                        success.showAndWait();
                        
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        fermer();
                    } catch (Exception e) {
                        statusLabel.setText("❌ Erreur lors de l'enregistrement des questions.");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de lire la réponse de l'IA: " + e.getMessage());
                        System.err.println("JSON Content: " + responseText);
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Erreur de communication avec l'IA.");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de contacter Gemini API: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    private String buildPrompt(String context, int nbQuestions, int points, List<String> types) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un expert pédagogique. Tu dois générer exactement ").append(nbQuestions);
        sb.append(" questions pour un quiz sur le sujet suivant :\n\"").append(context).append("\"\n\n");
        sb.append("Les types de questions autorisés sont : ").append(String.join(", ", types)).append(".\n\n");
        sb.append("Tu DOIS renvoyer ta réponse EXCLUSIVEMENT sous la forme d'un objet JSON valide contenant un tableau nommé 'questions'. ");
        sb.append("Ne renvoie PAS de markdown, pas de texte avant ni après le JSON.\n");
        sb.append("Chaque objet question doit avoir le format suivant :\n");
        sb.append("{\n");
        sb.append("  \"enonce\": \"Le texte de la question\",\n");
        sb.append("  \"type\": \"Soit 'QCM', 'Vrai/Faux' ou 'Texte libre'\",\n");
        sb.append("  \"reponses\": [\n");
        sb.append("    {\n");
        sb.append("      \"texte\": \"Le texte de la réponse\",\n");
        sb.append("      \"est_correcte\": true ou false,\n");
        sb.append("      \"explication\": \"Explication courte de pourquoi c'est vrai ou faux\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");
        sb.append("Pour Vrai/Faux, donne 2 réponses. Pour QCM, donne 4 réponses. Pour Texte libre, donne 1 réponse qui est un mot clé ou une phrase courte correcte.");
        return sb.toString();
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String apiKey = "AIzaSyD-CxH5LqUzN6Tj8NP16QuGws7wSBgGfkE";
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            apiKey = envKey;
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;

        JSONObject jsonBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();

        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        jsonBody.put("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            String textResponse = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
            
            // Clean markdown blocks if Gemini adds them despite prompt
            textResponse = textResponse.trim();
            if (textResponse.startsWith("```json")) {
                textResponse = textResponse.substring(7);
            } else if (textResponse.startsWith("```")) {
                textResponse = textResponse.substring(3);
            }
            if (textResponse.endsWith("```")) {
                textResponse = textResponse.substring(0, textResponse.length() - 3);
            }
            
            return textResponse.trim();
        } else {
            throw new Exception("API Error " + response.statusCode() + ": " + response.body());
        }
    }

    private void parseAndSaveQuestions(String jsonText, Quiz quiz, int points) throws Exception {
        JSONObject obj = new JSONObject(jsonText);
        JSONArray questionsArray = obj.getJSONArray("questions");
        
        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject qJson = questionsArray.getJSONObject(i);
            
            Question question = new Question();
            question.setEnonce(qJson.getString("enonce"));
            question.setType(qJson.getString("type"));
            question.setPoints(points);
            question.setOrdre(i + 1);
            question.setQuiz(quiz);
            
            questionService.ajouter(question);
            
            if (qJson.has("reponses")) {
                JSONArray repsArray = qJson.getJSONArray("reponses");
                for (int j = 0; j < repsArray.length(); j++) {
                    JSONObject rJson = repsArray.getJSONObject(j);
                    
                    Reponse reponse = new Reponse();
                    reponse.setTexte(rJson.getString("texte"));
                    reponse.setEst_correcte(rJson.getBoolean("est_correcte"));
                    if (rJson.has("explication")) {
                        reponse.setExplication_reponse(rJson.getString("explication"));
                    }
                    reponse.setQuestion(question); // Assuming adding the question object is enough, or we might need the ID
                    
                    // The 'ajouter' method of QuestionService usually sets the generated ID.
                    reponseService.ajouter(reponse);
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
