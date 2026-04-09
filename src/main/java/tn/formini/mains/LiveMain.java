package tn.formini.mains;

import tn.formini.entities.Evenement;
import tn.formini.entities.LiveComment;
import tn.formini.entities.LiveReaction;
import tn.formini.entities.User;
import tn.formini.services.EvenementService;
import tn.formini.services.LiveCommentService;
import tn.formini.services.LiveReactionService;
import tn.formini.services.UserService;

import java.util.Date;
import java.util.List;

public class LiveMain {
    public static void main(String[] args) {
        LiveCommentService cs = new LiveCommentService();
        LiveReactionService rs = new LiveReactionService();
        UserService us = new UserService();
        EvenementService es = new EvenementService();

        // Récupérer un utilisateur et un événement existants
        List<User> users = us.afficher();
        List<Evenement> events = es.afficher();

        if (users.isEmpty() || events.isEmpty()) {
            System.out.println("Erreur : Ajoutez d'abord un utilisateur (UserMain) et un événement (EvenementMain) !");
            return;
        }

        User u = users.get(0);
        Evenement e = events.get(0);

        // 1. Ajouter un commentaire
        LiveComment c1 = new LiveComment();
        c1.setContent("Super live !");
        c1.setCreated_at(new Date());
        c1.setUser(u);
        c1.setEvenement(e);
        System.out.println("--- Ajout d'un commentaire live ---");
        cs.ajouter(c1);

        // 2. Ajouter une réaction
        LiveReaction r1 = new LiveReaction();
        r1.setType("❤️");
        r1.setCreated_at(new Date());
        r1.setUser(u);
        r1.setEvenement(e);
        System.out.println("--- Ajout d'une réaction live ---");
        rs.ajouter(r1);

        // 3. Afficher
        System.out.println("--- Liste des commentaires ---");
        cs.afficher().forEach(System.out::println);
        System.out.println("--- Liste des réactions ---");
        rs.afficher().forEach(System.out::println);
    }
}
