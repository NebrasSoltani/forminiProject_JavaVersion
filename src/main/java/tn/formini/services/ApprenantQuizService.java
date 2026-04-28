package tn.formini.services;

import tn.formini.entities.Apprenant;
import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.entities.Reponse;
import tn.formini.entities.ResultatQuiz;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApprenantQuizService {

    private QuizService quizService;
    private QuestionService questionService;
    private ReponseService reponseService;

    public ApprenantQuizService() {
        this.quizService = new QuizService();
        this.questionService = new QuestionService();
        this.reponseService = new ReponseService();
    }

    public Map<String, Object> index(Apprenant apprenant, int formationId) {
        Map<String, Object> result = new HashMap<>();
        
        // Simulation des leçons (vous pouvez intégrer avec un service de leçon/cours plus tard)
        int leconsTerminees = 5;
        int totalLecons = 5;
        boolean toutesLeconsTerminees = (leconsTerminees >= totalLecons);
        
        result.put("leconsTerminees", leconsTerminees);
        result.put("totalLecons", totalLecons);
        result.put("toutesLeconsTerminees", toutesLeconsTerminees);
        
        List<Quiz> quizzes = quizService.findByFormation(formationId);
        result.put("quizzes", quizzes);
        
        return result;
    }

    public Map<String, Object> passer(Apprenant apprenant, int formationId, int quizId) {
        Map<String, Object> result = new HashMap<>();
        
        Quiz quiz = quizService.getById(quizId);
        if (quiz == null) {
            result.put("erreur", "Quiz introuvable.");
            return result;
        }
        
        result.put("quiz", quiz);
        return result;
    }

    public ResultatQuiz soumettre(Apprenant apprenant, int formationId, int quizId, Map<Integer, Integer> reponsesUtilisateur) {
        Quiz quiz = quizService.getById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz introuvable");
        }

        List<Question> allQuestions = questionService.getAll();
        int totalQuestions = 0;
        int bonnesReponses = 0;

        for (Question q : allQuestions) {
            if (q.getQuiz() != null && q.getQuiz().getId() == quizId) {
                totalQuestions++;
                Integer reponseId = reponsesUtilisateur.get(q.getId());
                if (reponseId != null) {
                    Reponse reponse = reponseService.getById(reponseId);
                    if (reponse != null && reponse.isEst_correcte()) {
                        bonnesReponses++;
                    }
                }
            }
        }

        ResultatQuiz resultat = new ResultatQuiz();
        resultat.setApprenant(apprenant.getUser());
        resultat.setQuiz(quiz);
        resultat.setNombre_total_questions(totalQuestions);
        resultat.setNombre_bonnes_reponses(bonnesReponses);
        
        double note = totalQuestions > 0 ? ((double) bonnesReponses / totalQuestions) * 100 : 0.0;
        resultat.setNote(new BigDecimal(note));
        
        boolean reussi = note >= quiz.getNote_minimale();
        resultat.setReussi(reussi);
        resultat.setDate_tentative(new Date());
        
        // Save the result to the database
        tn.formini.services.quizService.ResultatQuizService resultatService = new tn.formini.services.quizService.ResultatQuizService();
        resultatService.ajouter(resultat);
        
        return resultat;
    }

    public Map<String, Object> resultat(Apprenant apprenant, int resultatId) {
        Map<String, Object> result = new HashMap<>();
        
        tn.formini.services.quizService.ResultatQuizService resultatQuizService = new tn.formini.services.quizService.ResultatQuizService();
        ResultatQuiz r = resultatQuizService.getById(resultatId);
        
        if (r == null) {
            result.put("erreur", "Résultat introuvable.");
            return result;
        }
        
        result.put("resultat", r);
        return result;
    }
}
