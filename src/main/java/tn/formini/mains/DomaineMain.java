package tn.formini.mains;

import tn.formini.entities.Domaine;
import tn.formini.services.DomaineService;
import tn.formini.tools.MyDataBase;

import java.util.List;

public class DomaineMain {
    public static void main(String[] args) {
        // Initialize database connection
        MyDataBase.getInstance();
        
        DomaineService domaineService = new DomaineService();
        
        System.out.println("=== TEST CRUD DOMAINE ===\n");
        
        // CREATE - Ajouter un nouveau domaine
        System.out.println("1. AJOUTER UN DOMAINE");
        Domaine nouveauDomaine = new Domaine();
        nouveauDomaine.setNom("Informatique");
        
        try {
            nouveauDomaine.valider();
            domaineService.ajouter(nouveauDomaine);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur de validation: " + e.getMessage());
        }
        
        // Ajouter d'autres domaines
        Domaine domaine2 = new Domaine();
        domaine2.setNom("Marketing");
        
        Domaine domaine3 = new Domaine();
        domaine3.setNom("Design");
        
        try {
            domaine2.valider();
            domaineService.ajouter(domaine2);
            
            domaine3.valider();
            domaineService.ajouter(domaine3);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur de validation: " + e.getMessage());
        }
        
        System.out.println();
        
        // READ - Afficher tous les domaines
        System.out.println("2. AFFICHER TOUS LES DOMAINES");
        List<Domaine> domaines = domaineService.afficher();
        for (Domaine d : domaines) {
            System.out.println(d);
        }
        System.out.println();
        
        // FIND BY ID - Trouver un domaine par son ID
        if (!domaines.isEmpty()) {
            System.out.println("3. TROUVER UN DOMAINE PAR ID");
            int id = domaines.get(0).getId();
            Domaine domaineTrouve = domaineService.findById(id);
            if (domaineTrouve != null) {
                System.out.println("Domaine trouvé: " + domaineTrouve);
            } else {
                System.out.println("Domaine non trouvé avec l'ID: " + id);
            }
            System.out.println();
        }
        
        // FIND BY NOM - Trouver un domaine par son nom
        System.out.println("4. TROUVER UN DOMAINE PAR NOM");
        Domaine domaineParNom = domaineService.findByNom("Informatique");
        if (domaineParNom != null) {
            System.out.println("Domaine trouvé par nom: " + domaineParNom);
        } else {
            System.out.println("Domaine non trouvé avec le nom: Informatique");
        }
        System.out.println();
        
        // SEARCH - Rechercher des domaines par terme
        System.out.println("5. RECHERCHER DES DOMAINES PAR TERME");
        List<Domaine> domainesRecherches = domaineService.searchByNom("info");
        System.out.println("Domaines contenant 'info':");
        for (Domaine d : domainesRecherches) {
            System.out.println("  - " + d);
        }
        System.out.println();
        
        // UPDATE - Modifier un domaine
        if (!domaines.isEmpty()) {
            System.out.println("6. MODIFIER UN DOMAINE");
            Domaine domaineAModifier = domaines.get(0);
            domaineAModifier.setNom("Informatique Avancée");
            
            try {
                domaineAModifier.valider();
                domaineService.modifier(domaineAModifier);
                System.out.println("Domaine modifié: " + domaineAModifier);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur de validation: " + e.getMessage());
            }
            System.out.println();
        }
        
        // EXISTS - Vérifier si un domaine existe
        if (!domaines.isEmpty()) {
            System.out.println("7. VÉRIFIER L'EXISTENCE D'UN DOMAINE");
            int id = domaines.get(0).getId();
            boolean existe = domaineService.exists(id);
            System.out.println("Le domaine avec ID " + id + " existe: " + existe);
            
            boolean existeParNom = domaineService.existsByNom("Informatique");
            System.out.println("Le domaine 'Informatique' existe: " + existeParNom);
            
            boolean existePas = domaineService.existsByNom("DomaineInexistant");
            System.out.println("Le domaine 'DomaineInexistant' existe: " + existePas);
            System.out.println();
        }
        
        // DELETE - Supprimer un domaine
        if (domaines.size() > 1) {
            System.out.println("8. SUPPRIMER UN DOMAINE");
            int idASupprimer = domaines.get(1).getId();
            domaineService.supprimer(idASupprimer);
            System.out.println("Domaine avec ID " + idASupprimer + " supprimé");
            System.out.println();
        }
        
        // Afficher la liste finale
        System.out.println("9. LISTE FINALE DES DOMAINES");
        List<Domaine> domainesFinaux = domaineService.afficher();
        for (Domaine d : domainesFinaux) {
            System.out.println(d);
        }
        
        System.out.println("\n=== FIN DU TEST ===");
    }
}
