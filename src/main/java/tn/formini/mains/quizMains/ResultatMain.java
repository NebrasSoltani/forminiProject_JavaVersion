package tn.formini.mains.quizMains;

import tn.formini.entities.Quiz;
import tn.formini.entities.ResultatQuiz;
import tn.formini.entities.Users.User;
import tn.formini.services.quizService.ResultatQuizService;

import java.math.BigDecimal;
import java.util.Date;

public class ResultatMain {
    public static void main(String[] args) {

        ResultatQuizService service = new ResultatQuizService();

        // User et Quiz factices (doivent exister en DB)
        User apprenant = new User();
        apprenant.setId(1);

        Quiz quiz = new Quiz();
        quiz.setId(1);

        // ─── CREATE ───────────────────────────────────────────
        System.out.println("=== AJOUTER ===");
        ResultatQuiz r = new ResultatQuiz();
        r.setNote(new BigDecimal("75.00"));
        r.setNombre_bonnes_reponses(15);
        r.setNombre_total_questions(20);
        r.setDate_tentative(new Date());
        r.setReussi(true);
        r.setDetails_reponses("Q1:correct, Q2:incorrect");
        r.setApprenant(apprenant);
        r.setQuiz(quiz);
        service.ajouter(r);

        // ─── READ ALL ─────────────────────────────────────────
        System.out.println("\n=== LISTE DES RESULTATS ===");
        service.getAll().forEach(System.out::println);

        // ─── READ ONE ─────────────────────────────────────────
        System.out.println("\n=== GET BY ID (1) ===");
        ResultatQuiz found = service.getById(1);
        System.out.println(found);

        // ─── UPDATE ───────────────────────────────────────────
        System.out.println("\n=== MODIFIER ===");
        if (found != null) {
            found.setNote(new BigDecimal("85.00"));
            found.setNombre_bonnes_reponses(17);
            found.setNombre_total_questions(20);
            found.setDate_tentative(new Date());
            found.setReussi(true);
            found.setDetails_reponses("Q1:correct, Q2:correct");
            service.modifier(found);
        }

        // ─── READ ALL AFTER UPDATE ────────────────────────────
        System.out.println("\n=== LISTE APRES MODIFICATION ===");
        service.getAll().forEach(System.out::println);

        // ─── DELETE ───────────────────────────────────────────
        System.out.println("\n=== SUPPRIMER ID=1 ===");
        service.supprimer(1);

        // ─── READ ALL AFTER DELETE ────────────────────────────
        System.out.println("\n=== LISTE APRES SUPPRESSION ===");
        service.getAll().forEach(System.out::println);
    }
}