package tn.formini.mains;

import tn.formini.entities.Formateur;
import tn.formini.entities.User;
import tn.formini.services.FormateurService;
import tn.formini.services.UserService;
import tn.formini.tools.MyDataBase;

import java.util.Date;
import java.util.List;

public class FormateurMain {
    public static void main(String[] args) {
        // Initialize database connection
        MyDataBase.getInstance();
        
        FormateurService formateurService = new FormateurService();
        UserService userService = new UserService();
        
        System.out.println("=== TEST CRUD FORMATEUR ===\n");
        
        // CREATE - Ajouter un nouveau formateur
        System.out.println("1. AJOUTER UN FORMATEUR");
        Formateur nouveauFormateur = new Formateur();
        nouveauFormateur.setSpecialite("Développement Java");
        nouveauFormateur.setBio("Formateur expérimenté en Java avec plus de 10 ans d'expérience dans l'enseignement du développement web et mobile.");
        nouveauFormateur.setExperience_annees(12);
        nouveauFormateur.setLinkedin("https://linkedin.com/in/jeanformateur");
        nouveauFormateur.setPortfolio("https://jeanformateur.com");
        nouveauFormateur.setCv("https://jeanformateur.com/cv.pdf");
        nouveauFormateur.setNote_moyenne(4.5);
        
        // Créer un utilisateur pour le formateur
        User user = new User();
        // Utiliser un email unique avec timestamp pour éviter les doublons
        String uniqueEmail = "formateur" + System.currentTimeMillis() + "@test.com";
        user.setEmail(uniqueEmail);
        user.setRoles("[\"ROLE_FORMATEUR\"]");
        user.setPassword("Password123");
        user.setNom("Martin");
        user.setPrenom("Jean");
        user.setTelephone("2234567890");
        user.setGouvernorat("Tunis");
        user.setRole_utilisateur("formateur");
        // Ajouter une date de naissance valide (il y a 35 ans)
        user.setDate_naissance(new Date(System.currentTimeMillis() - (35L * 365 * 24 * 60 * 60 * 1000)));
        
        // Valider et ajouter l'utilisateur
        try {
            user.valider();
            userService.ajouter(user);
            if (user.getId() > 0) {
                System.out.println("Utilisateur créé avec succès, ID: " + user.getId());
                nouveauFormateur.setUser(user);
            } else {
                System.out.println("Erreur: Utilisateur non créé correctement (ID = 0)");
                return;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation utilisateur: " + e.getMessage());
            return;
        }
        
        // Valider et ajouter le formateur
        try {
            nouveauFormateur.valider();
            formateurService.ajouter(nouveauFormateur);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur de validation: " + e.getMessage());
        }
        
        System.out.println();
        
        // READ - Afficher tous les formateurs
        System.out.println("2. AFFICHER TOUS LES FORMATEURS");
        List<Formateur> formateurs = formateurService.afficher();
        for (Formateur f : formateurs) {
            System.out.println(f);
        }
        System.out.println();
        
        // Ajouter d'autres formateurs pour les tests
        System.out.println("3. AJOUTER D'AUTRES FORMATEURS");
        Formateur formateur2 = new Formateur();
        formateur2.setSpecialite("Design UX/UI");
        formateur2.setBio("Designer spécialisé en expérience utilisateur avec une passion pour l'ergonomie web.");
        formateur2.setExperience_annees(8);
        formateur2.setNote_moyenne(4.8);
        
        User user2 = new User();
        String uniqueEmail2 = "formateur" + (System.currentTimeMillis() + 1) + "@test.com";
        user2.setEmail(uniqueEmail2);
        user2.setRoles("[\"ROLE_FORMATEUR\"]");
        user2.setPassword("Password123");
        user2.setNom("Durand");
        user2.setPrenom("Marie");
        user2.setTelephone("2234567891");
        user2.setGouvernorat("Sfax");
        user2.setRole_utilisateur("formateur");
        user2.setDate_naissance(new Date(System.currentTimeMillis() - (28L * 365 * 24 * 60 * 60 * 1000)));
        
        try {
            user2.valider();
            userService.ajouter(user2);
            if (user2.getId() > 0) {
                formateur2.setUser(user2);
                formateur2.valider();
                formateurService.ajouter(formateur2);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        
        Formateur formateur3 = new Formateur();
        formateur3.setSpecialite("Marketing Digital");
        formateur3.setBio("Expert en marketing digital et stratégies de communication en ligne.");
        formateur3.setExperience_annees(15);
        formateur3.setNote_moyenne(4.2);
        
        User user3 = new User();
        String uniqueEmail3 = "formateur" + (System.currentTimeMillis() + 2) + "@test.com";
        user3.setEmail(uniqueEmail3);
        user3.setRoles("[\"ROLE_FORMATEUR\"]");
        user3.setPassword("Password123");
        user3.setNom("Bernard");
        user3.setPrenom("Pierre");
        user3.setTelephone("2234567892");
        user3.setGouvernorat("Sousse");
        user3.setRole_utilisateur("formateur");
        user3.setDate_naissance(new Date(System.currentTimeMillis() - (42L * 365 * 24 * 60 * 60 * 1000)));
        
        try {
            user3.valider();
            userService.ajouter(user3);
            if (user3.getId() > 0) {
                formateur3.setUser(user3);
                formateur3.valider();
                formateurService.ajouter(formateur3);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        
        System.out.println();
        
        // FIND BY ID - Trouver un formateur par son ID
        formateurs = formateurService.afficher();
        if (!formateurs.isEmpty()) {
            System.out.println("4. TROUVER UN FORMATEUR PAR ID");
            int id = formateurs.get(0).getId();
            Formateur formateurTrouve = formateurService.findById(id);
            if (formateurTrouve != null) {
                System.out.println("Formateur trouvé: " + formateurTrouve);
            } else {
                System.out.println("Formateur non trouvé avec l'ID: " + id);
            }
            System.out.println();
        }
        
        // SEARCH BY SPECIALITE - Rechercher des formateurs par spécialité
        System.out.println("5. RECHERCHER PAR SPÉCIALITÉ");
        List<Formateur> formateursJava = formateurService.findBySpecialite("Java");
        System.out.println("Formateurs spécialisés en Java:");
        for (Formateur f : formateursJava) {
            System.out.println("  - " + f.getSpecialite() + " (ID: " + f.getId() + ")");
        }
        System.out.println();
        
        // SEARCH BY EXPERIENCE - Rechercher des formateurs par expérience minimale
        System.out.println("6. RECHERCHER PAR EXPÉRIENCE MINIMALE (10 ans)");
        List<Formateur> formateursExp = formateurService.findByExperienceMin(10);
        System.out.println("Formateurs avec 10+ ans d'expérience:");
        for (Formateur f : formateursExp) {
            System.out.println("  - " + f.getSpecialite() + " (" + f.getExperience_annees() + " ans)");
        }
        System.out.println();
        
        // SEARCH BY NOTE - Rechercher des formateurs par note minimale
        System.out.println("7. RECHERCHER PAR NOTE MINIMALE (4.5)");
        List<Formateur> formateursNote = formateurService.findByNoteMin(4.5);
        System.out.println("Formateurs avec note >= 4.5:");
        for (Formateur f : formateursNote) {
            System.out.println("  - " + f.getSpecialite() + " (Note: " + f.getNote_moyenne() + ")");
        }
        System.out.println();
        
        // UPDATE - Modifier un formateur
        if (!formateurs.isEmpty()) {
            System.out.println("8. MODIFIER UN FORMATEUR");
            Formateur formateurAModifier = formateurs.get(0);
            formateurAModifier.setBio("Bio mise à jour: Formateur senior en Java, Spring et microservices avec 15 ans d'expérience.");
            formateurAModifier.setNote_moyenne(4.7);
            
            try {
                formateurAModifier.valider();
                formateurService.modifier(formateurAModifier);
                System.out.println("Formateur modifié: " + formateurAModifier);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur de validation: " + e.getMessage());
            }
            System.out.println();
        }
        
        // DELETE - Supprimer un formateur
        if (formateurs.size() > 1) {
            System.out.println("9. SUPPRIMER UN FORMATEUR");
            int idASupprimer = formateurs.get(1).getId();
            formateurService.supprimer(idASupprimer);
            System.out.println("Formateur avec ID " + idASupprimer + " supprimé");
            System.out.println();
        }
        
        // Afficher la liste finale
        System.out.println("10. LISTE FINALE DES FORMATEURS");
        List<Formateur> formateursFinaux = formateurService.afficher();
        for (Formateur f : formateursFinaux) {
            System.out.println(f);
        }
        
        System.out.println("\n=== FIN DU TEST ===");
    }
}
