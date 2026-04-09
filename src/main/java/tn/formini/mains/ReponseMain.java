package tn.formini.mains;

import tn.formini.entities.Question;
import tn.formini.entities.Reponse;
import tn.formini.services.ReponseService;

public class ReponseMain {
    public static void main(String[] args) {

        ReponseService service = new ReponseService();

        // Question factice (doit exister en DB)
        Question question = new Question();
        question.setId(1);

        // ─── CREATE ───────────────────────────────────────────
        System.out.println("=== AJOUTER ===");
        Reponse r = new Reponse();
        r.setTexte("32 bits");
        r.setEst_correcte(true);
        r.setExplication_reponse("En Java, int = 32 bits.");
        r.setQuestion(question);
        service.ajouter(r);

        Reponse r2 = new Reponse();
        r2.setTexte("64 bits");
        r2.setEst_correcte(false);
        r2.setExplication_reponse("64 bits correspond au type long.");
        r2.setQuestion(question);
        service.ajouter(r2);

        // ─── READ ALL ─────────────────────────────────────────
        System.out.println("\n=== LISTE DES REPONSES ===");
        service.getAll().forEach(System.out::println);

        // ─── READ ONE ─────────────────────────────────────────
        System.out.println("\n=== GET BY ID (1) ===");
        Reponse found = service.getById(1);
        System.out.println(found);

        // ─── UPDATE ───────────────────────────────────────────
        System.out.println("\n=== MODIFIER ===");
        if (found != null) {
            found.setTexte("32 bits (modifié)");
            found.setEst_correcte(true);
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