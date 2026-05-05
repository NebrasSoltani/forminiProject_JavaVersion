package tn.formini.controllers.quiz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import tn.formini.entities.Question;
import tn.formini.entities.ResultatQuiz;
import tn.formini.services.QuestionService;
import tn.formini.services.quizService.QuizService;
import tn.formini.services.quizService.ResultatQuizService;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StatistiquesController implements Initializable {

    @FXML private Label lblTotalQuestions;
    @FXML private Label lblTotalQuiz;
    @FXML private Label lblPassagesReussis;
    @FXML private Label lblPassagesEchoues;
    @FXML private ProgressBar progressReussite;
    @FXML private Label lblPourcentageReussite;

    @FXML private PieChart pieReussite;
    @FXML private PieChart pieQuestions;
    @FXML private BarChart<String, Number> barScores;

    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();
    private final ResultatQuizService resultatService = new ResultatQuizService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerStatistiques();
        // Apply white labels after data is set (needs runLater so chart nodes exist)
        javafx.application.Platform.runLater(this::appliquerStylesGraphiques);
    }

    private void appliquerStylesGraphiques() {
        // Force white text on all PieChart labels
        for (PieChart chart : new PieChart[]{pieReussite, pieQuestions}) {
            chart.lookupAll(".chart-pie-label").forEach(n -> {
                n.setStyle("-fx-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            });
            chart.lookupAll(".chart-legend-item").forEach(n -> {
                n.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            });
            chart.lookupAll(".chart-legend").forEach(n -> {
                n.setStyle("-fx-background-color: transparent;");
            });
        }
        // BarChart axis tick labels
        barScores.lookupAll(".axis-label").forEach(n -> n.setStyle("-fx-text-fill: white;"));
        barScores.lookupAll(".tick-mark").forEach(n -> n.setStyle("-fx-fill: white;"));
        barScores.lookupAll(".axis").forEach(n -> n.setStyle("-fx-tick-label-fill: white;"));
        barScores.lookupAll(".chart-legend-item").forEach(n -> n.setStyle("-fx-text-fill: white;"));
    }

    private void chargerStatistiques() {
        List<ResultatQuiz> resultats = resultatService.getAll();
        List<Question> questions = questionService.getAll();
        List<tn.formini.entities.Quiz> allQuizzes = quizService.getAll();

        long reussis = resultats.stream().filter(ResultatQuiz::isReussi).count();
        long echoues = resultats.size() - reussis;

        // Set Labels
        lblTotalQuestions.setText(String.valueOf(questions.size()));
        lblTotalQuiz.setText(String.valueOf(allQuizzes.size()));
        lblPassagesReussis.setText(String.valueOf(reussis));
        lblPassagesEchoues.setText(String.valueOf(echoues));

        double tauxReussite = resultats.isEmpty() ? 0 : (double) reussis / resultats.size();
        progressReussite.setProgress(tauxReussite);
        lblPourcentageReussite.setText(String.format("%.1f %%", tauxReussite * 100));

        // Pie Chart: Taux de réussite
        ObservableList<PieChart.Data> reussiteData = FXCollections.observableArrayList(
                new PieChart.Data("Réussis", reussis),
                new PieChart.Data("Échoués", echoues)
        );
        pieReussite.setData(reussiteData);

        // Map Quiz ID to Title
        Map<Integer, String> quizTitleMap = allQuizzes.stream()
                .collect(Collectors.toMap(tn.formini.entities.Quiz::getId, tn.formini.entities.Quiz::getTitre, (v1, v2) -> v1));

        // Pie Chart: Questions per Quiz
        Map<String, Long> questionsParQuiz = questions.stream()
                .filter(q -> q.getQuiz() != null)
                .collect(Collectors.groupingBy(
                        q -> quizTitleMap.getOrDefault(q.getQuiz().getId(), "Quiz Inconnu"),
                        Collectors.counting()
                ));
        
        ObservableList<PieChart.Data> questionData = FXCollections.observableArrayList();
        for (Map.Entry<String, Long> entry : questionsParQuiz.entrySet()) {
            questionData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        pieQuestions.setData(questionData);

        // Bar Chart: Scores moyens par Quiz
        Map<String, Double> scoreMoyenParQuiz = resultats.stream()
                .filter(r -> r.getQuiz() != null && r.getNote() != null)
                .collect(Collectors.groupingBy(
                        r -> quizTitleMap.getOrDefault(r.getQuiz().getId(), "Quiz Inconnu"),
                        Collectors.averagingDouble(r -> r.getNote().doubleValue())
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Score moyen (%)");
        for (Map.Entry<String, Double> entry : scoreMoyenParQuiz.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barScores.getData().clear();
        barScores.getData().add(series);
    }
}
