package tn.formini.mains.evenementsMains;

import tn.formini.entities.evenements.Evenement;
import tn.formini.services.evenementsService.EvenementService;

import java.util.Date;
import java.util.List;

public class EvenementMain {
    public static void main(String[] args) {
        EvenementService es = new EvenementService();

        // 1. Ajouter événement 1
        Evenement e1 = new Evenement();
        e1.setTitre("Conférence Tech 2026 – Édition révisée");
        e1.setDescription("Une conférence sur l'intelligence artificielle.");
        e1.setDate_debut(new Date());
        e1.setDate_fin(new Date(System.currentTimeMillis() + 3600000));
        e1.setLieu("Tunis");
        e1.setType("conférence");
        e1.setNombre_places(100);
        e1.setIs_actif(true);

        // Pour filieres : mettre du JSON valide
        e1.setFilieres("[\"informatique\", \"marketing\"]");  // Tableau JSON


        // Pour tags : mettre du JSON valide
        e1.setTags("[\"tech\", \"IA\", \"conférence\"]");  // Tableau JSON

        // Pour live_summary_data : mettre du JSON valide
        e1.setLive_summary_data("{}");  // Objet JSON vide

        e1.setImage("");
        e1.setImage360("");
        e1.setUrl_street_view("");
        e1.setResume_auto("");
        e1.setUrl_live("");
        e1.setStream_url("");
        e1.setLive(false);

        System.out.println("--- Ajout événement 1 ---");
        es.ajouter(e1);

        // 2. Ajouter événement 2
        Evenement e2 = new Evenement();
        e2.setTitre("Salon de l'Innovation");
        e2.setDescription("Salon sur l'innovation technologique.");
        e2.setDate_debut(new Date());
        e2.setDate_fin(new Date(System.currentTimeMillis() + 3600000));
        e2.setLieu("Monastir");
        e2.setType("salon");
        e2.setNombre_places(100);
        e2.setIs_actif(true);

        // JSON valide pour e2 aussi
        e2.setFilieres("[\"gestion\", \"marketing\"]");
        e2.setTags("[\"innovation\", \"technologie\"]");
        e2.setLive_summary_data("{}");

        e2.setImage("");
        e2.setImage360("");
        e2.setUrl_street_view("");
        e2.setResume_auto("");
        e2.setUrl_live("");
        e2.setStream_url("");
        e2.setLive(false);

        System.out.println("--- Ajout événement 2 ---");
        es.ajouter(e2);

        // 3. Afficher les événements
        System.out.println("--- Liste des événements ---");
        List<Evenement> events = es.afficher();
        for (Evenement e : events) {
            System.out.println(e);
        }

        // 4. Modifier le dernier
        if (!events.isEmpty()) {
            Evenement lastEvent = events.get(events.size() - 1);
            lastEvent.setTitre("Conférence IA Modifiée");
            System.out.println("--- Modification ID: " + lastEvent.getId() + " ---");
            es.modifier(lastEvent);
        }
    }
}