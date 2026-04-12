package tn.formini.services;

import tn.formini.entities.Apprenant;
import tn.formini.entities.Quiz;
import tn.formini.entities.ResultatQuiz;
import tn.formini.entities.User;
import tn.formini.tools.MyDataBase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * Equivalent Java du ApprenantQuizController Symfony.
 * Gère : index, passer, soumettre, résultat.
 */
public class ApprenantQuizService {

    private final Connection cnx;
    private final QuizService quizService;
    private final ResultatQuizService resultatQuizService;

    public ApprenantQuizService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        this.quizService = new QuizService();
        this.resultatQuizService = new ResultatQuizService();
    }

    // ─── INDEX : Liste des quiz d'une formation ───────────────────────────────
    /**
     * Équivalent de index() :
     * Retourne les quiz d'une formation si l'apprenant est inscrit.
     * Calcule aussi si toutes les leçons sont terminées.
     */
    public Map<String, Object> index(Apprenant apprenant, int formationId) {
        Map<String, Object> result = new HashMap<>();

        if (!estInscrit(apprenant, formationId)) {
            result.put("erreur", "Vous devez être inscrit à cette formation pour passer les quiz.");
            return result;
        }

        List<Quiz> quizzes = quizService.findByFormation(formationId);
        int totalLecons = countLeconsByFormation(formationId);
        int leconsTerminees = countLeconTerminees(apprenant, formationId);
        boolean toutesLeconsTerminees = totalLecons > 0 && leconsTerminees >= totalLecons;

        result.put("quizzes", quizzes);
        result.put("toutesLeconsTerminees", toutesLeconsTerminees);
        result.put("leconsTerminees", leconsTerminees);
        result.put("totalLecons", totalLecons);

        System.out.println("=== INDEX QUIZ ===");
        System.out.println("Formation ID   : " + formationId);
        System.out.println("Leçons         : " + leconsTerminees + " / " + totalLecons);
        System.out.println("Toutes terminées: " + toutesLeconsTerminees);
        System.out.println("Nombre de quiz : " + quizzes.size());

        return result;
    }

    // ─── PASSER : Accéder à un quiz ──────────────────────────────────────────
    /**
     * Équivalent de passer() :
     * Vérifie les droits, la progression et retourne les questions du quiz.
     */
    public Map<String, Object> passer(Apprenant apprenant, int formationId, int quizId) {
        Map<String, Object> result = new HashMap<>();

        if (!estInscrit(apprenant, formationId)) {
            result.put("erreur", "Vous devez être inscrit à cette formation.");
            return result;
        }

        Quiz quiz = quizService.getById(quizId);
        if (quiz == null) {
            result.put("erreur", "Quiz non trouvé.");
            return result;
        }

        // Vérifier si déjà complété
        boolean dejaComplete = resultatQuizService.getAll().stream()
                .anyMatch(r -> r.getApprenant() != null && r.getApprenant().getId() == apprenant.getId()
                        && r.getQuiz() != null && r.getQuiz().getId() == quiz.getId());
        if (dejaComplete) {
            result.put("avertissement", "Vous avez déjà complété ce quiz. Vous ne pouvez le passer qu'une seule fois.");
            ResultatQuiz ancien = resultatQuizService.getAll().stream()
                    .filter(r -> r.getApprenant() != null && r.getApprenant().getId() == apprenant.getId()
                            && r.getQuiz() != null && r.getQuiz().getId() == quiz.getId())
                    .findFirst().orElse(null);
            result.put("resultatId", ancien != null ? ancien.getId() : -1);
            return result;
        }

        // Vérifier que toutes les leçons sont terminées
        int totalLecons = countLeconsByFormation(formationId);
        int leconsTerminees = countLeconTerminees(apprenant, formationId);
        if (totalLecons > 0 && leconsTerminees < totalLecons) {
            result.put("avertissement", "Vous devez terminer toutes les leçons avant de passer ce quiz.");
            return result;
        }

        // Récupérer les questions (mélanger si activé)
        List<String> questions = getQuestions(quizId);
        if (quiz.isMelanger()) {
            Collections.shuffle(questions);
        }

        result.put("quiz", quiz);
        result.put("questions", questions);

        System.out.println("=== PASSER QUIZ ===");
        System.out.println("Quiz       : " + quiz.getTitre());
        System.out.println("Questions  : " + questions.size());
        System.out.println("Mélangées  : " + quiz.isMelanger());

        return result;
    }

    // ─── SOUMETTRE : Corriger et enregistrer le résultat ─────────────────────
    /**
     * Équivalent de soumettre() :
     * Prend les réponses de l'apprenant, calcule la note et sauvegarde.
     *
     * @param reponses Map<questionId, reponseId>
     */
    public ResultatQuiz soumettre(Apprenant apprenant, int formationId, int quizId, Map<Integer, Integer> reponses) {
        if (!estInscrit(apprenant, formationId)) {
            throw new IllegalStateException("Apprenant non inscrit à la formation.");
        }

        Quiz quiz = quizService.getById(quizId);
        if (quiz == null) throw new IllegalArgumentException("Quiz introuvable : " + quizId);

        List<Map<String, Object>> questions = getQuestionsAvecReponses(quizId);

        int nombreBonnesReponses = 0;
        JSONArray detailsReponses = new JSONArray();

        for (Map<String, Object> question : questions) {
            int questionId = (int) question.get("id");
            Integer reponseUtilisateur = reponses.get(questionId);
            Integer bonneReponseId = (Integer) question.get("bonneReponseId");

            boolean estCorrecte = bonneReponseId != null && bonneReponseId.equals(reponseUtilisateur);
            if (estCorrecte) nombreBonnesReponses++;

            JSONObject detail = new JSONObject();
            detail.put("question_id", questionId);
            detail.put("reponse_utilisateur", reponseUtilisateur != null ? reponseUtilisateur : JSONObject.NULL);
            detail.put("reponse_correcte", bonneReponseId != null ? bonneReponseId : JSONObject.NULL);
            detail.put("correct", estCorrecte);
            detailsReponses.put(detail);
        }

        int total = questions.size();
        double note = total > 0 ? ((double) nombreBonnesReponses / total) * 100.0 : 0.0;
        boolean reussi = note >= quiz.getNote_minimale();

        ResultatQuiz resultat = new ResultatQuiz();
        // ResultatQuiz attend un User — on crée un User avec l'id de l'apprenant
        User user = new User();
        user.setId(apprenant.getId());
        resultat.setApprenant(user);
        resultat.setQuiz(quiz);
        resultat.setNote(new java.math.BigDecimal(Math.round(note * 100.0) / 100.0).setScale(2, java.math.RoundingMode.HALF_UP));
        resultat.setNombre_bonnes_reponses(nombreBonnesReponses);
        resultat.setNombre_total_questions(total);
        resultat.setReussi(reussi);
        resultat.setDetails_reponses(detailsReponses.toString());
        resultat.setDate_tentative(new java.util.Date());

        resultatQuizService.ajouter(resultat);

        System.out.println("=== RÉSULTAT SOUMIS ===");
        System.out.println("Note     : " + resultat.getNote() + "%");
        System.out.println("Réussies : " + nombreBonnesReponses + " / " + total);
        System.out.println("Réussi   : " + reussi);

        return resultat;
    }

    // ─── RÉSULTAT : Afficher le détail d'un résultat ─────────────────────────
    /**
     * Équivalent de resultat() :
     * Récupère et affiche les détails d'un résultat existant.
     */
    public Map<String, Object> resultat(Apprenant apprenant, int resultatId) {
        Map<String, Object> result = new HashMap<>();

        ResultatQuiz resultat = resultatQuizService.getById(resultatId);
        if (resultat == null) {
            result.put("erreur", "Résultat introuvable.");
            return result;
        }

        // Vérification que c'est bien le résultat de cet apprenant
        if (resultat.getApprenant() == null || resultat.getApprenant().getId() != apprenant.getId()) {
            result.put("erreur", "Accès refusé à ce résultat.");
            return result;
        }

        result.put("resultat", resultat);
        result.put("detailsReponses", resultat.getDetails_reponses());

        System.out.println("=== AFFICHAGE RÉSULTAT ===");
        System.out.println("Résultat ID  : " + resultat.getId());
        System.out.println("Note         : " + resultat.getNote() + "%");
        System.out.println("Réussi       : " + resultat.isReussi());
        System.out.println("Détails JSON : " + resultat.getDetails_reponses());

        return result;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ─── MÉTHODES PRIVÉES / UTILITAIRES ────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════

    private boolean estInscrit(Apprenant apprenant, int formationId) {
        String req = "SELECT COUNT(*) FROM inscription WHERE apprenant_id = ? AND formation_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, apprenant.getId());
            ps.setInt(2, formationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Erreur estInscrit : " + e.getMessage());
        }
        return false;
    }

    private int countLeconsByFormation(int formationId) {
        String req = "SELECT COUNT(*) FROM lecon WHERE formation_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, formationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Erreur countLeconsByFormation : " + e.getMessage());
        }
        return 0;
    }

    private int countLeconTerminees(Apprenant apprenant, int formationId) {
        String req = "SELECT COUNT(*) FROM progression_lecon pl " +
                "JOIN lecon l ON pl.lecon_id = l.id " +
                "WHERE pl.apprenant_id = ? AND l.formation_id = ? AND pl.terminee = true";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, apprenant.getId());
            ps.setInt(2, formationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Erreur countLeconTerminees : " + e.getMessage());
        }
        return 0;
    }

    private List<String> getQuestions(int quizId) {
        List<String> questions = new ArrayList<>();
        String req = "SELECT contenu FROM question WHERE quiz_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) questions.add(rs.getString("contenu"));
        } catch (SQLException e) {
            System.out.println("Erreur getQuestions : " + e.getMessage());
        }
        return questions;
    }

    private List<Map<String, Object>> getQuestionsAvecReponses(int quizId) {
        List<Map<String, Object>> questions = new ArrayList<>();
        String req = "SELECT q.id AS qid, r.id AS rid, r.est_correcte " +
                "FROM question q LEFT JOIN reponse r ON r.question_id = q.id " +
                "WHERE q.quiz_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();

            Map<Integer, Map<String, Object>> temp = new LinkedHashMap<>();
            while (rs.next()) {
                int qid = rs.getInt("qid");
                temp.putIfAbsent(qid, new HashMap<>());
                Map<String, Object> q = temp.get(qid);
                q.put("id", qid);
                if (rs.getBoolean("est_correcte")) {
                    q.put("bonneReponseId", rs.getInt("rid"));
                }
            }
            questions.addAll(temp.values());
        } catch (SQLException e) {
            System.out.println("Erreur getQuestionsAvecReponses : " + e.getMessage());
        }
        return questions;
    }
}