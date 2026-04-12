package tn.formini.mains.quizMains;

import tn.formini.entities.Quizs.Question;
import tn.formini.entities.Quizs.Quiz;
import tn.formini.services.quizService.QuestionService;

public class QuestionMain {
    public static void main(String[] args) {

        QuestionService service = new QuestionService();

        // Quiz factice (doit exister en DB)
        Quiz quiz = new Quiz();
        quiz.setId(1);

        // ─── CREATE ───────────────────────────────────────────
        System.out.println("=== AJOUTER ===");
        Question q = new Question();
        q.setEnonce("Quelle est la taille d'un int en Java ?");
        q.setType("qcm");
        q.setPoints(10);
        q.setOrdre(1);
        q.setExplication("Un int fait 32 bits en Java.");
        q.setExplications_detaillees("En Java, le type int est un entier signé de 32 bits.");
        q.setQuiz(quiz);
        service.ajouter(q);

        // ─── READ ALL ─────────────────────────────────────────
        System.out.println("\n=== LISTE DES QUESTIONS ===");
        service.getAll().forEach(System.out::println);

        // ─── READ ONE ─────────────────────────────────────────
        System.out.println("\n=== GET BY ID (1) ===");
        Question found = service.getById(1);
        System.out.println(found);

        // ─── UPDATE ───────────────────────────────────────────
        System.out.println("\n=== MODIFIER ===");
        if (found != null) {
            found.setEnonce("Quelle est la taille d'un long en Java ?");
            found.setPoints(20);
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