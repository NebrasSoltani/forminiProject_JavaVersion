package tn.formini.mains.evenementsMains;

import tn.formini.entities.evenements.Evenement;
import tn.formini.entities.evenements.ParticipationEvenement;
import tn.formini.entities.Users.User;
import tn.formini.services.evenementsService.EvenementService;
import tn.formini.services.evenementsService.ParticipationEvenementService;
import tn.formini.services.UsersService.UserService;

import java.util.Date;
import java.util.List;

public class ParticipationMain {
    public static void main(String[] args) {
        ParticipationEvenementService ps = new ParticipationEvenementService();
        UserService us = new UserService();
        EvenementService es = new EvenementService();

        List<User> users = us.afficher();
        List<Evenement> events = es.afficher();

        if (users.isEmpty() || events.isEmpty()) {
            System.out.println("Erreur : Ajoutez d'abord un utilisateur (UserMain) et un événement (EvenementMain) !");
            return;
        }

        User u = users.get(0);
        Evenement e = events.get(0);

        // Vérifier si la participation existe déjà
        List<ParticipationEvenement> existing = ps.afficher();
        boolean alreadyExists = false;
        for (ParticipationEvenement p : existing) {
            if (p.getUser() != null && p.getUser().getId() == u.getId() &&
                p.getEvenement() != null && p.getEvenement().getId() == e.getId()) {
                alreadyExists = true;
                break;
            }
        }

        if (alreadyExists) {
            System.out.println("L'utilisateur " + u.getId() + " participe déjà à l'événement " + e.getId());
        } else {
            // 1. Ajouter une participation
            ParticipationEvenement p1 = new ParticipationEvenement();
            p1.setDate_participation(new Date());
            p1.setUser(u);
            p1.setEvenement(e);
            
            System.out.println("--- Ajout d'une participation ---");
            ps.ajouter(p1);
        }

        // 2. Afficher
        System.out.println("--- Liste des participations ---");
        List<ParticipationEvenement> list = ps.afficher();
        for (ParticipationEvenement p : list) {
            System.out.println(p);
        }
    }
}
