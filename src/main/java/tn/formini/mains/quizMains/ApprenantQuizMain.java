/*package tn.formini;

import tn.formini.services.ApprenantQuizService;
import tn.formini.entities.Quizs.ResultatQuiz;

import java.util.HashMap;
import java.util.Map;

public class ApprenantQuizMain{
    public static void main(String[] args) {

        ApprenantQuizService service = new ApprenantQuizService();

        int apprenantId = 1;  // id apprenant (change selon ta BD)
        int quizId = 1;       // id quiz (change aussi)

        // ─── Exemple : vérifier inscription ───
        boolean inscrit = service.isInscrit(apprenantId, 1);
        System.out.println("Inscrit ? " + inscrit);

        // ─── Exemple : vérifier leçons terminées ───
        boolean termine = service.toutesLeconsTerminees(apprenantId, 1);
        System.out.println("Toutes les leçons terminées ? " + termine);

        // ─── Exemple : vérifier si quiz déjà fait ───
        boolean deja = service.quizDejaComplete(apprenantId, quizId);
        System.out.println("Quiz déjà complété ? " + deja);

        // ─── Exemple : soumettre un quiz ───
        Map<Integer, Integer> reponses = new HashMap<>();

        // questionId → reponseId (tu dois adapter selon ta BD)
        reponses.put(1, 2);
        reponses.put(2, 5);
        reponses.put(3, 7);

        ResultatQuiz resultat = service.soumettre(apprenantId, quizId, reponses);

        if (resultat != null) {
            System.out.println("Note : " + resultat.getNote());
            System.out.println("Réussi ? " + resultat.isReussi());
            System.out.println("Bonnes réponses : " + resultat.getNombreBonnesReponses());
            System.out.println("Total questions : " + resultat.getNombreTotalQuestions());
            System.out.println("Détails : " + resultat.getDetailsReponses());
        }

        // ─── Exemple : récupérer résultat ───
        ResultatQuiz r = service.getResultatByApprenantAndQuiz(apprenantId, quizId);
        if (r != null) {
            System.out.println("Résultat récupéré ID : " + r.getId());
        }
    }
}*/