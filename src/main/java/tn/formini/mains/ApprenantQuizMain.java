package tn.formini;

import tn.formini.entities.Apprenant;
import tn.formini.entities.Quiz;
import tn.formini.entities.ResultatQuiz;
import tn.formini.services.ApprenantQuizService;
import tn.formini.services.QuizService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApprenantQuizMain {

    public static void main(String[] args) {

        ApprenantQuizService apprenantQuizService = new ApprenantQuizService();
        QuizService quizService = new QuizService();

        // ── Simuler un apprenant connecté ─────────────────────────────────────
        Apprenant apprenant = new Apprenant();
        apprenant.setId(1); // ID de l'apprenant en base

        int formationId = 1;

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║       APPRENANT QUIZ - DEMO          ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        // ─────────────────────────────────────────────────────────────────────
        // 1. INDEX : Lister les quiz de la formation
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("▶ ÉTAPE 1 — Lister les quiz de la formation " + formationId);
        Map<String, Object> indexData = apprenantQuizService.index(apprenant, formationId);

        if (indexData.containsKey("erreur")) {
            System.out.println("  ❌ " + indexData.get("erreur"));
        } else {
            List<Quiz> quizzes = (List<Quiz>) indexData.get("quizzes");
            System.out.println("  Leçons terminées     : " + indexData.get("leconsTerminees") + " / " + indexData.get("totalLecons"));
            System.out.println("  Toutes leçons faites : " + indexData.get("toutesLeconsTerminees"));
            System.out.println("  Quiz disponibles     : " + quizzes.size());
            for (Quiz q : quizzes) {
                System.out.println("    → [" + q.getId() + "] " + q.getTitre() + " (note min: " + q.getNote_minimale() + "%)");
            }
        }

        System.out.println();

        // ─────────────────────────────────────────────────────────────────────
        // 2. PASSER : Accéder à un quiz spécifique
        // ─────────────────────────────────────────────────────────────────────
        int quizId = 1;
        System.out.println("▶ ÉTAPE 2 — Accéder au quiz " + quizId);
        Map<String, Object> passerData = apprenantQuizService.passer(apprenant, formationId, quizId);

        if (passerData.containsKey("erreur")) {
            System.out.println("  ❌ " + passerData.get("erreur"));
            return;
        }
        if (passerData.containsKey("avertissement")) {
            System.out.println("  ⚠️  " + passerData.get("avertissement"));
            if (passerData.containsKey("resultatId")) {
                System.out.println("  → Résultat existant ID : " + passerData.get("resultatId"));
            }
            return;
        }

        List<String> questions = (List<String>) passerData.get("questions");
        System.out.println("  Questions disponibles : " + questions.size());
        for (int i = 0; i < questions.size(); i++) {
            System.out.println("    Q" + (i + 1) + ": " + questions.get(i));
        }

        System.out.println();

        // ─────────────────────────────────────────────────────────────────────
        // 3. SOUMETTRE : Envoyer les réponses
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("▶ ÉTAPE 3 — Soumettre les réponses pour le quiz " + quizId);

        // Simuler les réponses de l'apprenant : Map<questionId, reponseId>
        // Remplacez ces IDs par des valeurs réelles de votre base de données
        Map<Integer, Integer> reponses = new HashMap<>();
        reponses.put(1, 2);  // Question 1 → Réponse 2
        reponses.put(2, 5);  // Question 2 → Réponse 5
        reponses.put(3, 7);  // Question 3 → Réponse 7

        try {
            ResultatQuiz resultat = apprenantQuizService.soumettre(apprenant, formationId, quizId, reponses);

            System.out.println("  ✅ Résultat enregistré !");
            System.out.println("  ID résultat : " + resultat.getId());
            System.out.println("  Note        : " + resultat.getNote() + "%");
            System.out.println("  Bonnes rép. : " + resultat.getNombre_bonnes_reponses() + " / " + resultat.getNombre_total_questions());
            System.out.println("  Réussi      : " + (resultat.isReussi() ? "✅ OUI" : "❌ NON"));

            System.out.println();

            // ─────────────────────────────────────────────────────────────────
            // 4. RÉSULTAT : Consulter le détail
            // ─────────────────────────────────────────────────────────────────
            System.out.println("▶ ÉTAPE 4 — Consulter le résultat " + resultat.getId());
            Map<String, Object> resultatData = apprenantQuizService.resultat(apprenant, resultat.getId());

            if (resultatData.containsKey("erreur")) {
                System.out.println("  ❌ " + resultatData.get("erreur"));
            } else {
                ResultatQuiz r = (ResultatQuiz) resultatData.get("resultat");
                System.out.println("  Note finale     : " + r.getNote() + "%");
                System.out.println("  Réussi          : " + r.isReussi());
                System.out.println("  Détails JSON    : " + r.getDetails_reponses());
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("  ❌ Erreur : " + e.getMessage());
        }

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║              FIN DE DEMO             ║");
        System.out.println("╚══════════════════════════════════════╝");
    }
}/*package tn.formini;

import tn.formini.services.ApprenantQuizService;
import tn.formini.entities.ResultatQuiz;

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