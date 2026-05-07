package tn.formini.mains.quizMains;

import tn.formini.entities.Quizs.Quiz;
import tn.formini.entities.Quizs.ResultatQuiz;
import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.services.ApprenantQuizService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Démonstration console pour {@link ApprenantQuizService}.
 */
public class ApprenantQuizMain {

    public static void main(String[] args) {

        ApprenantQuizService apprenantQuizService = new ApprenantQuizService();

        Apprenant apprenant = new Apprenant();
        apprenant.setId(1);

        User user = new User();
        user.setId(1);
        user.setEmail("demo@formini.tn");
        user.setPassword("DemoPass1");
        user.setNom("Demo");
        user.setPrenom("User");
        user.setRoles("[\"apprenant\"]");
        user.setRole_utilisateur("apprenant");
        apprenant.setUser(user);

        int formationId = 1;

        System.out.println("=== APPRENANT QUIZ - DEMO ===\n");

        System.out.println("--- 1. Index (quiz de la formation) ---");
        Map<String, Object> indexData = apprenantQuizService.index(apprenant, formationId);
        if (indexData.containsKey("erreur")) {
            System.out.println("Erreur : " + indexData.get("erreur"));
        } else {
            @SuppressWarnings("unchecked")
            List<Quiz> quizzes = (List<Quiz>) indexData.get("quizzes");
            System.out.println("Quiz disponibles : " + (quizzes != null ? quizzes.size() : 0));
            if (quizzes != null) {
                for (Quiz q : quizzes) {
                    System.out.println("  [" + q.getId() + "] " + q.getTitre());
                }
            }
        }

        int quizId = 1;
        System.out.println("\n--- 2. Passer (quiz " + quizId + ") ---");
        Map<String, Object> passerData = apprenantQuizService.passer(apprenant, formationId, quizId);
        if (passerData.containsKey("erreur")) {
            System.out.println("Erreur : " + passerData.get("erreur"));
            return;
        }
        Quiz quiz = (Quiz) passerData.get("quiz");
        if (quiz != null) {
            System.out.println("Quiz chargé : " + quiz.getTitre());
        }

        System.out.println("\n--- 3. Soumettre ---");
        Map<Integer, Integer> reponses = new HashMap<>();
        reponses.put(1, 2);
        reponses.put(2, 5);
        reponses.put(3, 7);

        try {
            ResultatQuiz resultat = apprenantQuizService.soumettre(apprenant, formationId, quizId, reponses);
            System.out.println("Note : " + resultat.getNote() + " — Réussi : " + resultat.isReussi());

            System.out.println("\n--- 4. Consulter résultat ---");
            Map<String, Object> resultatData = apprenantQuizService.resultat(apprenant, resultat.getId());
            if (resultatData.containsKey("erreur")) {
                System.out.println("Erreur : " + resultatData.get("erreur"));
            } else {
                ResultatQuiz r = (ResultatQuiz) resultatData.get("resultat");
                if (r != null) {
                    System.out.println("Note finale : " + r.getNote());
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
