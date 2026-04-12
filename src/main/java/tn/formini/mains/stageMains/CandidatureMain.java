package tn.formini.mains.stageMains;

import tn.formini.entities.stages.Candidature;
import tn.formini.entities.stages.OffreStage;
import tn.formini.entities.Users.User;
import tn.formini.services.stageService.CandidatureService;
import tn.formini.services.stageService.OffreStageService;

import java.util.Date;
import java.util.List;

public class CandidatureMain {
    private static CandidatureService service = new CandidatureService();
    private static OffreStageService offreStageService = new OffreStageService();

    public static void main(String[] args) {
        System.out.println("=== DÉMONSTRATION CRUD CANDIDATURES ===");
        
        // 1. Créer et ajouter des offres de stage (prérequis)
        System.out.println("\n0. CRÉATION D'OFFRES DE STAGE (PRÉREQUIS)");
        creerOffresDeStageDemo();
        
        // 2. Créer et ajouter des candidatures
        System.out.println("\n1. AJOUT DE CANDIDATURES");
        ajouterCandidatureDemo();
        
        // 3. Afficher toutes les candidatures
        System.out.println("\n2. AFFICHAGE DE TOUTES LES CANDIDATURES");
        afficherCandidatures();
        
        // 4. Rechercher par ID
        System.out.println("\n3. RECHERCHE PAR ID");
        rechercherParIdDemo();
        
        // 5. Rechercher par statut
        System.out.println("\n4. RECHERCHE PAR STATUT");
        rechercherParStatutDemo();
        
        // 6. Rechercher par offre de stage
        System.out.println("\n5. RECHERCHE PAR OFFRE DE STAGE");
        rechercherParOffreStageDemo();
        
        // 7. Rechercher par apprenant
        System.out.println("\n6. RECHERCHE PAR APPRENANT");
        rechercherParApprenantDemo();
        
        // 8. Modifier une candidature
        System.out.println("\n7. MODIFICATION D'UNE CANDIDATURE");
        modifierCandidatureDemo();
        
        // 9. Supprimer une candidature
        System.out.println("\n8. SUPPRESSION D'UNE CANDIDATURE");
        supprimerCandidatureDemo();
        
        // 10. Affichage final
        System.out.println("\n9. AFFICHAGE FINAL APRÈS OPÉRATIONS");
        afficherCandidatures();
        
        System.out.println("\n=== DÉMONSTRATION TERMINÉE ===");
    }

    private static void creerOffresDeStageDemo() {
        // Créer des offres de stage pour les candidatures
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
            offreStageService.ajouter(offre1);
            System.out.println("Offre de stage 1 créée : " + offre1.getTitre());
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation offre 1 : " + e.getMessage());
        }
        
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
            offreStageService.ajouter(offre2);
            System.out.println("Offre de stage 2 créée : " + offre2.getTitre());
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation offre 2 : " + e.getMessage());
        }
    }

    private static void ajouterCandidatureDemo() {
        // Candidature 1
        Candidature candidature1 = new Candidature();
        candidature1.setStatut("en_attente");
        candidature1.setLettre_motivation("Je suis très intéressé par ce stage en développement web. J'ai des compétences en React et Node.js et je souhaite développer mes compétences professionnelles.");
        candidature1.setCv("cv_mohamed_benali.pdf");
        candidature1.setCommentaire("Disponible immédiatement");
        candidature1.setDate_candidature(new Date());
        
        OffreStage offre1 = offreStageService.findById(7);
        if (offre1 != null) {
            candidature1.setOffreStage(offre1);
        }
        
        User apprenant1 = new User();
        apprenant1.setId(1);
        candidature1.setApprenant(apprenant1);
        
        try {
            candidature1.valider();
            service.ajouter(candidature1);
            System.out.println("Candidature 1 ajoutée pour l'offre : " + offre1.getTitre());
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation candidature 1 : " + e.getMessage());
        }
        
        // Candidature 2
        Candidature candidature2 = new Candidature();
        candidature2.setStatut("en_attente");
        candidature2.setLettre_motivation("Passionné par le marketing digital, je souhaite mettre en pratique mes connaissances en SEO et gestion des réseaux sociaux.");
        candidature2.setCv("cv_sarah_trabelsi.pdf");
        candidature2.setCommentaire("Étudiant en 3ème année marketing");
        candidature2.setDate_candidature(new Date());
        
        OffreStage offre2 = offreStageService.findById(8);
        if (offre2 != null) {
            candidature2.setOffreStage(offre2);
        }
        
        User apprenant2 = new User();
        apprenant2.setId(2);
        candidature2.setApprenant(apprenant2);
        
        try {
            candidature2.valider();
            service.ajouter(candidature2);
            System.out.println("Candidature 2 ajoutée pour l'offre : " + offre2.getTitre());
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation candidature 2 : " + e.getMessage());
        }
        
        // Candidature 3 avec statut acceptée
        Candidature candidature3 = new Candidature();
        candidature3.setStatut("acceptee");
        candidature3.setLettre_motivation("Développeur junior avec expérience en projets universitaires, je suis prêt à relever de nouveaux défis.");
        candidature3.setCv("cv_ahmed_jarray.pdf");
        candidature3.setCommentaire("Entretien passé avec succès");
        candidature3.setDate_candidature(new Date());
        
        OffreStage offre1_ref = offreStageService.findById(7);
        if (offre1_ref != null) {
            candidature3.setOffreStage(offre1_ref);
        }
        
        User apprenant3 = new User();
        apprenant3.setId(3);
        candidature3.setApprenant(apprenant3);
        
        try {
            candidature3.valider();
            service.ajouter(candidature3);
            System.out.println("Candidature 3 ajoutée pour l'offre : " + offre1_ref.getTitre());
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur validation candidature 3 : " + e.getMessage());
        }
    }

    private static void afficherCandidatures() {
        List<Candidature> candidatures = service.afficher();
        
        if (candidatures.isEmpty()) {
            System.out.println("Aucune candidature trouvée.");
        } else {
            System.out.println("Nombre total de candidatures : " + candidatures.size());
            for (int i = 0; i < candidatures.size(); i++) {
                Candidature candidature = candidatures.get(i);
                System.out.println((i + 1) + ". " + candidature);
            }
        }
    }

    private static void rechercherParIdDemo() {
        // Rechercher la première candidature
        Candidature candidature = service.findById(1);
        if (candidature != null) {
            System.out.println("Candidature trouvée avec ID 1 : " + candidature);
        } else {
            System.out.println("Aucune candidature trouvée avec ID 1");
        }
        
        // Rechercher une candidature qui n'existe pas
        Candidature candidatureInexistante = service.findById(999);
        if (candidatureInexistante == null) {
            System.out.println("Aucune candidature trouvée avec ID 999 (test normal)");
        }
    }

    private static void rechercherParStatutDemo() {
        System.out.println("Recherche des candidatures avec statut 'en_attente' :");
        List<Candidature> candidaturesEnAttente = service.findByStatut("en_attente");
        if (candidaturesEnAttente.isEmpty()) {
            System.out.println("Aucune candidature trouvée avec statut 'en_attente'");
        } else {
            System.out.println("Nombre de candidatures en attente : " + candidaturesEnAttente.size());
            for (Candidature candidature : candidaturesEnAttente) {
                System.out.println("  - " + candidature);
            }
        }
        
        System.out.println("\nRecherche des candidatures avec statut 'acceptee' :");
        List<Candidature> candidaturesAcceptees = service.findByStatut("acceptee");
        if (candidaturesAcceptees.isEmpty()) {
            System.out.println("Aucune candidature trouvée avec statut 'acceptee'");
        } else {
            System.out.println("Nombre de candidatures acceptées : " + candidaturesAcceptees.size());
            for (Candidature candidature : candidaturesAcceptees) {
                System.out.println("  - " + candidature);
            }
        }
    }

    private static void rechercherParOffreStageDemo() {
        System.out.println("Recherche des candidatures pour l'offre de stage ID 7 :");
        List<Candidature> candidaturesOffre1 = service.findByOffreStage(7);
        if (candidaturesOffre1.isEmpty()) {
            System.out.println("Aucune candidature trouvée pour l'offre de stage ID 7");
        } else {
            System.out.println("Nombre de candidatures pour l'offre ID 7 : " + candidaturesOffre1.size());
            for (Candidature candidature : candidaturesOffre1) {
                System.out.println("  - " + candidature);
            }
        }
        
        System.out.println("\nRecherche des candidatures pour l'offre de stage ID 8 :");
        List<Candidature> candidaturesOffre2 = service.findByOffreStage(8);
        if (candidaturesOffre2.isEmpty()) {
            System.out.println("Aucune candidature trouvée pour l'offre de stage ID 8");
        } else {
            System.out.println("Nombre de candidatures pour l'offre ID 8 : " + candidaturesOffre2.size());
            for (Candidature candidature : candidaturesOffre2) {
                System.out.println("  - " + candidature);
            }
        }
    }

    private static void rechercherParApprenantDemo() {
        System.out.println("Recherche des candidatures pour l'apprenant ID 1 :");
        List<Candidature> candidaturesApprenant1 = service.findByApprenant(1);
        if (candidaturesApprenant1.isEmpty()) {
            System.out.println("Aucune candidature trouvée pour l'apprenant ID 1");
        } else {
            System.out.println("Nombre de candidatures pour l'apprenant ID 1 : " + candidaturesApprenant1.size());
            for (Candidature candidature : candidaturesApprenant1) {
                System.out.println("  - " + candidature);
            }
        }
        
        System.out.println("\nRecherche des candidatures pour l'apprenant ID 2 :");
        List<Candidature> candidaturesApprenant2 = service.findByApprenant(2);
        if (candidaturesApprenant2.isEmpty()) {
            System.out.println("Aucune candidature trouvée pour l'apprenant ID 2");
        } else {
            System.out.println("Nombre de candidatures pour l'apprenant ID 2 : " + candidaturesApprenant2.size());
            for (Candidature candidature : candidaturesApprenant2) {
                System.out.println("  - " + candidature);
            }
        }
    }

    private static void modifierCandidatureDemo() {
        // Modifier la première candidature
        Candidature candidature = service.findById(1);
        if (candidature != null) {
            System.out.println("Candidature avant modification : " + candidature);
            
            candidature.setStatut("en_cours");
            candidature.setCommentaire("Entretien prévu la semaine prochaine");
            
            try {
                candidature.valider();
                service.modifier(candidature);
                System.out.println("Candidature après modification : " + candidature);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur lors de la modification : " + e.getMessage());
            }
        } else {
            System.out.println("Impossible de modifier : candidature non trouvée");
        }
    }

    private static void supprimerCandidatureDemo() {
        // Supprimer la deuxième candidature
        Candidature candidature = service.findById(2);
        if (candidature != null) {
            System.out.println("Candidature à supprimer : " + candidature);
            service.supprimer(2);
            System.out.println("Candidature supprimée avec succès");
        } else {
            System.out.println("Impossible de supprimer : candidature non trouvée");
        }
    }
}
