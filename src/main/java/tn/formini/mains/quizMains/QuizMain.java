package tn.formini.mains.quizMains;

import tn.formini.entities.formations.Formation;
import tn.formini.entities.Quizs.Quiz;
import tn.formini.services.quizService.QuizService;

public class QuizMain {
    public static void main(String[] args) {

        QuizService service = new QuizService();

        // Formation factice (doit exister en DB)
        Formation f = new Formation();
        f.setId(1);

        // ─── CREATE ───────────────────────────────────────────
        System.out.println("=== AJOUTER ===");
        Quiz q = new Quiz();
        q.setTitre("Quiz Java");
        q.setDescription("Quiz sur les bases de Java");
        q.setDuree(30);
        q.setNote_minimale(50);
        q.setAfficher_correction(true);
        q.setMelanger(false);
        q.setFormation(f);
        service.ajouter(q);

        // ─── READ ALL ─────────────────────────────────────────
        System.out.println("\n=== LISTE DES QUIZ ===");
        service.getAll().forEach(System.out::println);

        // ─── READ ONE ─────────────────────────────────────────
        System.out.println("\n=== GET BY ID (1) ===");
        Quiz found = service.getById(1);
        System.out.println(found);

        // ─── UPDATE ───────────────────────────────────────────
        System.out.println("\n=== MODIFIER ===");
        if (found != null) {
            found.setTitre("Quiz Java Modifié");
            found.setDuree(45);
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