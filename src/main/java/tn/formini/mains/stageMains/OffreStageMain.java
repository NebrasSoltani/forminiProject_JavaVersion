package tn.formini.mains.stageMains;

import tn.formini.entities.stages.OffreStage;
import tn.formini.entities.Users.User;
import tn.formini.services.stageService.OffreStageService;

import java.util.Date;
import java.util.List;

public class OffreStageMain {
    private static OffreStageService service = new OffreStageService();

    public static void main(String[] args) {
        System.out.println("=== DÉMONSTRATION CRUD OFFRES DE STAGE ===");
        
        // 1. Créer et ajouter des offres de stage
        System.out.println("\n1. AJOUT D'OFFRES DE STAGE");
        ajouterOffreDemo();
        
        // 2. Afficher toutes les offres
        System.out.println("\n2. AFFICHAGE DE TOUTES LES OFFRES");
        afficherOffres();
        
        // 3. Rechercher par ID
        System.out.println("\n3. RECHERCHE PAR ID");
        rechercherParIdDemo();
        
        // 4. Rechercher par statut
        System.out.println("\n4. RECHERCHE PAR STATUT");
        rechercherParStatutDemo();
        
        // 5. Modifier une offre
        System.out.println("\n5. MODIFICATION D'UNE OFFRE");
        modifierOffreDemo();
        
        // 6. Supprimer une offre
        System.out.println("\n6. SUPPRESSION D'UNE OFFRE");
        supprimerOffreDemo();
        
        // 7. Affichage final
        System.out.println("\n7. AFFICHAGE FINAL APRÈS OPÉRATIONS");
        afficherOffres();
        
        System.out.println("\n=== DÉMONSTRATION TERMINÉE ===");
    }

    private static void ajouterOffreDemo() {
        // Offre 1
        OffreStage offre1 = new OffreStage();
        offre1.setTitre("Stage Développement Web");
        offre1.setDescription("Stage en développement web avec React et Node.js");
        offre1.setEntreprise("Tech Solutions");
        offre1.setDomaine("Informatique");
        offre1.setCompetences_requises("HTML, CSS, JavaScript, React");
        offre1.setProfil_demande("Étudiant en informatique motivé");
        offre1.setDuree("3 mois");
        offre1.setType_stage("Alternance");
        offre1.setLieu("Tunis");
        offre1.setRemuneration("400 TND/mois");
        offre1.setContact_email("contact@techsolutions.tn");
        offre1.setContact_tel("71234567");
        offre1.setStatut("ouvert");
        offre1.setDate_publication(new Date());
        
        User societe1 = new User();
        societe1.setId(1);
        offre1.setSociete(societe1);
        
        try {
            offre1.valider();
            service.ajouter(offre1);
            System.out.println("Offre 1 ajoutée : " + offre1);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation offre 1 : " + e.getMessage());
        }
        
        // Offre 2
        OffreStage offre2 = new OffreStage();
        offre2.setTitre("Stage Marketing Digital");
        offre2.setDescription("Stage en marketing digital et réseaux sociaux");
        offre2.setEntreprise("Digital Agency");
        offre2.setDomaine("Marketing");
        offre2.setCompetences_requises("SEO, Google Ads, Facebook Ads");
        offre2.setProfil_demande("Étudiant en marketing");
        offre2.setDuree("2 mois");
        offre2.setType_stage("Stage d'été");
        offre2.setLieu("Sfax");
        offre2.setRemuneration("300 TND/mois");
        offre2.setContact_email("jobs@digitalagency.tn");
        offre2.setContact_tel("72345678");
        offre2.setStatut("ouvert");
        offre2.setDate_publication(new Date());
        
        User societe2 = new User();
        societe2.setId(2);
        offre2.setSociete(societe2);
        
        try {
            offre2.valider();
            service.ajouter(offre2);
            System.out.println("Offre 2 ajoutée : " + offre2);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation offre 2 : " + e.getMessage());
        }
        
        // Offre 3 avec statut fermé
        OffreStage offre3 = new OffreStage();
        offre3.setTitre("Stage Data Science");
        offre3.setDescription("Stage en analyse de données et machine learning");
        offre3.setEntreprise("AI Company");
        offre3.setDomaine("Data Science");
        offre3.setCompetences_requises("Python, R, SQL, Machine Learning");
        offre3.setProfil_demande("Étudiant en data science");
        offre3.setDuree("6 mois");
        offre3.setType_stage("Stage PFE");
        offre3.setLieu("Sousse");
        offre3.setRemuneration("500 TND/mois");
        offre3.setContact_email("hr@aicompany.tn");
        offre3.setContact_tel("73456789");
        offre3.setStatut("ferme");
        offre3.setDate_publication(new Date());
        
        User societe3 = new User();
        societe3.setId(3);
        offre3.setSociete(societe3);
        
        try {
            offre3.valider();
            service.ajouter(offre3);
            System.out.println("Offre 3 ajoutée : " + offre3);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation offre 3 : " + e.getMessage());
        }
    }

    private static void afficherOffres() {
        List<OffreStage> offres = service.afficher();
        
        if (offres.isEmpty()) {
            System.out.println("Aucune offre de stage trouvée.");
        } else {
            System.out.println("Nombre total d'offres : " + offres.size());
            for (int i = 0; i < offres.size(); i++) {
                OffreStage offre = offres.get(i);
                System.out.println((i + 1) + ". " + offre);
            }
        }
    }

    private static void rechercherParIdDemo() {
        // Rechercher la première offre ajoutée
        OffreStage offre = service.findById(1);
        if (offre != null) {
            System.out.println("Offre trouvée avec ID 1 : " + offre);
        } else {
            System.out.println("Aucune offre trouvée avec ID 1");
        }
        
        // Rechercher une offre qui n'existe pas
        OffreStage offreInexistante = service.findById(999);
        if (offreInexistante == null) {
            System.out.println("Aucune offre trouvée avec ID 999 (test normal)");
        }
    }

    private static void rechercherParStatutDemo() {
        System.out.println("Recherche des offres avec statut 'ouvert' :");
        List<OffreStage> offresOuvertes = service.findByStatut("ouvert");
        if (offresOuvertes.isEmpty()) {
            System.out.println("Aucune offre trouvée avec statut 'ouvert'");
        } else {
            System.out.println("Nombre d'offres ouvertes : " + offresOuvertes.size());
            for (OffreStage offre : offresOuvertes) {
                System.out.println("  - " + offre);
            }
        }
        
        System.out.println("\nRecherche des offres avec statut 'ferme' :");
        List<OffreStage> offresFermees = service.findByStatut("ferme");
        if (offresFermees.isEmpty()) {
            System.out.println("Aucune offre trouvée avec statut 'ferme'");
        } else {
            System.out.println("Nombre d'offres fermées : " + offresFermees.size());
            for (OffreStage offre : offresFermees) {
                System.out.println("  - " + offre);
            }
        }
    }

    private static void modifierOffreDemo() {
        // Modifier la première offre
        OffreStage offre = service.findById(1);
        if (offre != null) {
            System.out.println("Offre avant modification : " + offre);
            
            offre.setStatut("en_attente");
            offre.setRemuneration("450 TND/mois");
            
            try {
                offre.valider();
                service.modifier(offre);
                System.out.println("Offre après modification : " + offre);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur lors de la modification : " + e.getMessage());
            }
        } else {
            System.out.println("Impossible de modifier : offre non trouvée");
        }
    }

    private static void supprimerOffreDemo() {
        // Supprimer la deuxième offre
        OffreStage offre = service.findById(2);
        if (offre != null) {
            System.out.println("Offre à supprimer : " + offre);
            service.supprimer(2);
            System.out.println("Offre supprimée avec succès");
        } else {
            System.out.println("Impossible de supprimer : offre non trouvée");
        }
    }
}
