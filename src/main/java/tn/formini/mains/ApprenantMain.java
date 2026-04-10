package tn.formini.mains;

import tn.formini.entities.Apprenant;
import tn.formini.entities.User;
import tn.formini.entities.Domaine;
import tn.formini.services.ApprenantService;
import tn.formini.services.UserService;
import tn.formini.services.DomaineService;
import tn.formini.tools.MyDataBase;

import java.util.List;
import java.util.Date;

public class ApprenantMain {
    public static void main(String[] args) {

        
        ApprenantService apprenantService = new ApprenantService();
        UserService userService = new UserService();
        DomaineService domaineService = new DomaineService();
        
        System.out.println("=== TEST CRUD APPRENANT ===\n");
        
        // CREATE - Ajouter un nouvel apprenant
        System.out.println("1. AJOUTER UN APPRENANT");
        Apprenant nouvelApprenant = new Apprenant();
        nouvelApprenant.setGenre("homme");
        nouvelApprenant.setEtat_civil("celibataire");
        nouvelApprenant.setObjectif("Devenir développeur Java");
        nouvelApprenant.setDomaines_interet("[\"Java\", \"Web\", \"Base de données\"]");
        
        // Créer un utilisateur pour l'apprenant
        User user = new User();
        // Utiliser un email unique avec timestamp pour éviter les doublons
        String uniqueEmail = "apprenant" + System.currentTimeMillis() + "@test.com";
        user.setEmail(uniqueEmail);
        user.setRoles("[\"ROLE_APPRENANT\"]");
        user.setPassword("Password123");
        user.setNom("Dupont");
        user.setPrenom("Jean");
        user.setTelephone("1234567890");
        user.setGouvernorat("Tunis");
        user.setRole_utilisateur("apprenant");
        // Ajouter une date de naissance valide (il y a 20 ans)
        user.setDate_naissance(new Date(System.currentTimeMillis() - (20L * 365 * 24 * 60 * 60 * 1000)));
        
        // Valider et ajouter l'utilisateur
        try {
            user.valider();
            userService.ajouter(user);
            if (user.getId() > 0) {
                System.out.println("Utilisateur créé avec succès, ID: " + user.getId());
                nouvelApprenant.setUser(user);
            } else {
                System.out.println("Erreur: Utilisateur non créé correctement (ID = 0)");
                return;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation utilisateur: " + e.getMessage());
            return;
        }
        
        // Créer un domaine pour l'apprenant
        Domaine domaine = new Domaine();
        domaine.setNom("Informatique");
        
        try {
            domaine.valider();
            domaineService.ajouter(domaine);
            System.out.println("Domaine créé avec succès, ID: " + domaine.getId());
            nouvelApprenant.setDomaine(domaine);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation domaine: " + e.getMessage());
            // Continuer sans domaine (optionnel)
        }
        
        // Valider et ajouter l'apprenant
        try {
            nouvelApprenant.valider();
            apprenantService.ajouter(nouvelApprenant);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur de validation: " + e.getMessage());
        }
        
        System.out.println();
        
        // READ - Afficher tous les apprenants
        System.out.println("2. AFFICHER TOUS LES APPRENANTS");
        List<Apprenant> apprenants = apprenantService.afficher();
        for (Apprenant a : apprenants) {
            System.out.println(a);
        }
        System.out.println();
        
        // UPDATE - Modifier un apprenant
        if (!apprenants.isEmpty()) {
            System.out.println("3. MODIFIER UN APPRENANT");
            Apprenant apprenantAModifier = apprenants.get(0);
            apprenantAModifier.setObjectif("Devenir expert en Java Spring");
            apprenantAModifier.setDomaines_interet("[\"Java\", \"Spring\", \"Microservices\"]");
            
            try {
                apprenantAModifier.valider();
                apprenantService.modifier(apprenantAModifier);
                System.out.println("Apprenant modifié: " + apprenantAModifier);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur de validation: " + e.getMessage());
            }
            System.out.println();
        }
        
        // FIND BY ID - Trouver un apprenant par son ID
        if (!apprenants.isEmpty()) {
            System.out.println("4. TROUVER UN APPRENANT PAR ID");
            int id = apprenants.get(0).getId();
            Apprenant apprenantTrouve = apprenantService.findById(id);
            if (apprenantTrouve != null) {
                System.out.println("Apprenant trouvé: " + apprenantTrouve);
            } else {
                System.out.println("Apprenant non trouvé avec l'ID: " + id);
            }
            System.out.println();
        }
        
        // DELETE - Supprimer un apprenant
        if (!apprenants.isEmpty()) {
            System.out.println("5. SUPPRIMER UN APPRENANT");
            int idASupprimer = apprenants.get(0).getId();
            apprenantService.supprimer(idASupprimer);
            System.out.println("Apprenant avec ID " + idASupprimer + " supprimé");
            System.out.println();
        }
        
        // Afficher la liste finale
        System.out.println("6. LISTE FINALE DES APPRENANTS");
        List<Apprenant> apprenantsFinaux = apprenantService.afficher();
        for (Apprenant a : apprenantsFinaux) {
            System.out.println(a);
        }
        
        System.out.println("\n=== FIN DU TEST ===");
    }
}
