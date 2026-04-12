package tn.formini.mains.usersMains;

import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.UsersService.SocieteService;
import tn.formini.services.UsersService.UserService;

import java.util.Date;
import java.util.List;

public class SocieteMain {
    public static void main(String[] args) {

        
        SocieteService societeService = new SocieteService();
        UserService userService = new UserService();
        
        System.out.println("=== TEST CRUD SOCIETE ===\n");
        
        // CREATE - Ajouter une nouvelle société
        System.out.println("1. AJOUTER UNE SOCIETE");
        Societe nouvelleSociete = new Societe();
        nouvelleSociete.setNom_societe("Tech Solutions SA");
        nouvelleSociete.setSecteur("Technologies de l'Information");
        nouvelleSociete.setDescription("Entreprise spécialisée dans le développement de solutions logicielles innovantes pour les entreprises.");
        nouvelleSociete.setAdresse("123 Avenue Habib Bourguiba, Tunis, Tunisie");
        nouvelleSociete.setSite_web("https://techsolutions.tn");
        
        // Créer un utilisateur pour la société
        User user = new User();
        // Utiliser un email unique avec timestamp pour éviter les doublons
        String uniqueEmail = "societe" + System.currentTimeMillis() + "@test.com";
        user.setEmail(uniqueEmail);
        user.setRoles("[\"ROLE_SOCIETE\"]");
        user.setPassword("Password123");
        user.setNom("Responsable");
        user.setPrenom("Société");
        user.setTelephone("2234567890");
        user.setGouvernorat("Tunis");
        user.setRole_utilisateur("societe");
        // Ajouter une date de naissance valide (il y a 40 ans)
        user.setDate_naissance(new Date(System.currentTimeMillis() - (40L * 365 * 24 * 60 * 60 * 1000)));
        
        // Valider et ajouter l'utilisateur
        try {
            user.valider();
            userService.ajouter(user);
            if (user.getId() > 0) {
                System.out.println("Utilisateur créé avec succès, ID: " + user.getId());
                nouvelleSociete.setUser(user);
            } else {
                System.out.println("Erreur: Utilisateur non créé correctement (ID = 0)");
                return;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation utilisateur: " + e.getMessage());
            return;
        }
        
        // Valider et ajouter la société
        try {
            nouvelleSociete.valider();
            societeService.ajouter(nouvelleSociete);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur de validation: " + e.getMessage());
        }
        
        System.out.println();
        
        // READ - Afficher toutes les sociétés
        System.out.println("2. AFFICHER TOUTES LES SOCIETES");
        List<Societe> societes = societeService.afficher();
        for (Societe s : societes) {
            System.out.println(s);
        }
        System.out.println();
        
        // Ajouter d'autres sociétés pour les tests
        System.out.println("3. AJOUTER D'AUTRES SOCIETES");
        Societe societe2 = new Societe();
        societe2.setNom_societe("Digital Marketing Pro");
        societe2.setSecteur("Marketing Digital");
        societe2.setDescription("Agence de marketing digital spécialisée dans les stratégies en ligne et les réseaux sociaux.");
        societe2.setAdresse("45 Rue Farhat Hached, Sfax, Tunisie");
        societe2.setSite_web("https://dmpro.tn");
        
        User user2 = new User();
        String uniqueEmail2 = "societe" + (System.currentTimeMillis() + 1) + "@test.com";
        user2.setEmail(uniqueEmail2);
        user2.setRoles("[\"ROLE_SOCIETE\"]");
        user2.setPassword("Password123");
        user2.setNom("Marketing");
        user2.setPrenom("Manager");
        user2.setTelephone("2234567891");
        user2.setGouvernorat("Sfax");
        user2.setRole_utilisateur("societe");
        user2.setDate_naissance(new Date(System.currentTimeMillis() - (35L * 365 * 24 * 60 * 60 * 1000)));
        
        try {
            user2.valider();
            userService.ajouter(user2);
            if (user2.getId() > 0) {
                societe2.setUser(user2);
                societe2.valider();
                societeService.ajouter(societe2);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        
        Societe societe3 = new Societe();
        societe3.setNom_societe("Formation Plus");
        societe3.setSecteur("Éducation et Formation");
        societe3.setDescription("Centre de formation professionnelle proposant des cours en ligne et en présentiel.");
        societe3.setAdresse("78 Avenue de la République, Sousse, Tunisie");
        societe3.setSite_web("https://formationplus.tn");
        
        User user3 = new User();
        String uniqueEmail3 = "societe" + (System.currentTimeMillis() + 2) + "@test.com";
        user3.setEmail(uniqueEmail3);
        user3.setRoles("[\"ROLE_SOCIETE\"]");
        user3.setPassword("Password123");
        user3.setNom("Education");
        user3.setPrenom("Director");
        user3.setTelephone("2234567892");
        user3.setGouvernorat("Sousse");
        user3.setRole_utilisateur("societe");
        user3.setDate_naissance(new Date(System.currentTimeMillis() - (45L * 365 * 24 * 60 * 60 * 1000)));
        
        try {
            user3.valider();
            userService.ajouter(user3);
            if (user3.getId() > 0) {
                societe3.setUser(user3);
                societe3.valider();
                societeService.ajouter(societe3);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        
        System.out.println();
        
        // FIND BY ID - Trouver une société par son ID
        societes = societeService.afficher();
        if (!societes.isEmpty()) {
            System.out.println("4. TROUVER UNE SOCIETE PAR ID");
            int id = societes.get(0).getId();
            Societe societeTrouvee = societeService.findById(id);
            if (societeTrouvee != null) {
                System.out.println("Société trouvée: " + societeTrouvee);
            } else {
                System.out.println("Société non trouvée avec l'ID: " + id);
            }
            System.out.println();
        }
        
        // SEARCH BY NOM - Rechercher des sociétés par nom
        System.out.println("5. RECHERCHER PAR NOM");
        List<Societe> societesTech = societeService.findByNom("Tech");
        System.out.println("Sociétés contenant 'Tech' dans le nom:");
        for (Societe s : societesTech) {
            System.out.println("  - " + s.getNom_societe() + " (ID: " + s.getId() + ")");
        }
        System.out.println();
        
        // SEARCH BY SECTEUR - Rechercher des sociétés par secteur
        System.out.println("6. RECHERCHER PAR SECTEUR");
        List<Societe> societesMarketing = societeService.findBySecteur("Marketing");
        System.out.println("Sociétés dans le secteur 'Marketing':");
        for (Societe s : societesMarketing) {
            System.out.println("  - " + s.getNom_societe() + " (" + s.getSecteur() + ")");
        }
        System.out.println();
        
        // SEARCH BY ADRESSE - Rechercher des sociétés par adresse
        System.out.println("7. RECHERCHER PAR ADRESSE");
        List<Societe> societesTunis = societeService.findByAdresse("Tunis");
        System.out.println("Sociétés à Tunis:");
        for (Societe s : societesTunis) {
            System.out.println("  - " + s.getNom_societe() + " (" + s.getAdresse() + ")");
        }
        System.out.println();
        
        // EXISTS CHECKS - Vérifier l'existence des sociétés
        if (!societes.isEmpty()) {
            System.out.println("8. VÉRIFICATIONS D'EXISTENCE");
            int id = societes.get(0).getId();
            boolean existe = societeService.exists(id);
            System.out.println("La société avec ID " + id + " existe: " + existe);
            
            String nom = societes.get(0).getNom_societe();
            boolean existeParNom = societeService.existsByNom(nom);
            System.out.println("La société '" + nom + "' existe: " + existeParNom);
            
            boolean existePas = societeService.existsByNom("SociétéInexistante");
            System.out.println("La société 'SociétéInexistante' existe: " + existePas);
            System.out.println();
        }
        
        // UPDATE - Modifier une société
        if (!societes.isEmpty()) {
            System.out.println("9. MODIFIER UNE SOCIETE");
            Societe societeAModifier = societes.get(0);
            societeAModifier.setDescription("Description mise à jour: Leader tunisien dans les solutions technologiques innovantes et le conseil en transformation numérique.");
            societeAModifier.setSite_web("https://www.techsolutions.tn");
            
            try {
                societeAModifier.valider();
                societeService.modifier(societeAModifier);
                System.out.println("Société modifiée: " + societeAModifier);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur de validation: " + e.getMessage());
            }
            System.out.println();
        }
        
        // DELETE - Supprimer une société
        if (societes.size() > 1) {
            System.out.println("10. SUPPRIMER UNE SOCIETE");
            int idASupprimer = societes.get(1).getId();
            societeService.supprimer(idASupprimer);
            System.out.println("Société avec ID " + idASupprimer + " supprimée");
            System.out.println();
        }
        
        // Afficher la liste finale
        System.out.println("11. LISTE FINALE DES SOCIETES");
        List<Societe> societesFinales = societeService.afficher();
        for (Societe s : societesFinales) {
            System.out.println(s);
        }
        
        System.out.println("\n=== FIN DU TEST ===");
    }
}
