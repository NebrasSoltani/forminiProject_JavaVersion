package tn.formini.controllers;

import tn.formini.entities.Candidature;
import tn.formini.entities.OffreStage;
import tn.formini.entities.User;
import tn.formini.services.CandidatureService;
import tn.formini.services.OffreStageService;

import java.util.*;

/**
 * Équivalent de SocieteController — version JDBC pure (style UserService)
 */
public class SocieteController {

    private final OffreStageService  offreStageService  = new OffreStageService();
    private final CandidatureService candidatureService = new CandidatureService();

    // ---------------------------------------------------------------
    // Lister les offres (paginé + filtré)
    // ---------------------------------------------------------------
    public Map<String, Object> index(
            User user,
            int page, int limit,
            String titre, String typeStage,
            String statut, String domaine, String lieu) {

        checkSociete(user);

        if (limit <= 0) limit = 10;
        if (page  <= 0) page  = 1;

        Map<String, String> filters = new HashMap<>();
        filters.put("titre",     titre     != null ? titre     : "");
        filters.put("typeStage", typeStage != null ? typeStage : "");
        filters.put("statut",    statut    != null ? statut    : "");
        filters.put("domaine",   domaine   != null ? domaine   : "");
        filters.put("lieu",      lieu      != null ? lieu      : "");

        Map<String, Object> result = offreStageService
                .rechercherParSocietePagine(user.getId(), filters, page, limit);

        long total = (long) result.get("total");
        int  pages = (int) Math.max(1, Math.ceil((double) total / limit));
        if (page > pages) page = pages;

        result.put("filters", filters);
        result.put("page",    page);
        result.put("pages",   pages);
        result.put("limit",   limit);
        return result;
    }

    // ---------------------------------------------------------------
    // Créer une offre
    // ---------------------------------------------------------------
    public void creerOffre(User user, OffreStage offre) {
        checkSociete(user);
        offre.setSocieteId(user.getId());
        offreStageService.ajouter(offre);
        System.out.println("Offre de stage publiée avec succès !");
    }

    // ---------------------------------------------------------------
    // Afficher le détail d'une offre avec ses candidatures
    // ---------------------------------------------------------------
    public Map<String, Object> afficherOffre(int id, User user) {
        OffreStage offre = getOffreAndCheckOwner(id, user);
        List<Candidature> candidatures = candidatureService.findByOffreStage(id);

        Map<String, Object> data = new HashMap<>();
        data.put("offre",        offre);
        data.put("candidatures", candidatures);
        return data;
    }

    // ---------------------------------------------------------------
    // Modifier une offre
    // ---------------------------------------------------------------
    public void modifierOffre(int id, User user, OffreStage offreForm) {
        OffreStage offre = getOffreAndCheckOwner(id, user);

        offre.setTitre(offreForm.getTitre());
        offre.setDescription(offreForm.getDescription());
        offre.setEntreprise(offreForm.getEntreprise());
        offre.setDomaine(offreForm.getDomaine());
        offre.setCompetencesRequises(offreForm.getCompetencesRequises());
        offre.setProfilDemande(offreForm.getProfilDemande());
        offre.setDuree(offreForm.getDuree());
        offre.setDateDebut(offreForm.getDateDebut());
        offre.setDateFin(offreForm.getDateFin());
        offre.setTypeStage(offreForm.getTypeStage());
        offre.setLieu(offreForm.getLieu());
        offre.setRemuneration(offreForm.getRemuneration());
        offre.setContactEmail(offreForm.getContactEmail());
        offre.setContactTel(offreForm.getContactTel());
        offre.setStatut(offreForm.getStatut());

        offreStageService.modifier(offre);
        System.out.println("Offre modifiée avec succès !");
    }

    // ---------------------------------------------------------------
    // Supprimer une offre
    // ---------------------------------------------------------------
    public void supprimerOffre(int id, User user) {
        getOffreAndCheckOwner(id, user); // vérifie propriété
        offreStageService.supprimer(id);
        System.out.println("Offre supprimée avec succès !");
    }

    // ---------------------------------------------------------------
    // Mettre à jour le statut d'une candidature
    // ---------------------------------------------------------------
    public void updateStatutCandidature(int candidatureId, User user,
                                        String statut, String commentaire) {
        Candidature candidature = candidatureService.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable : " + candidatureId));

        // Vérifier que l'offre appartient bien à cette société
        getOffreAndCheckOwner(candidature.getOffreStageId(), user);

        List<String> statutsValides = List.of("en_attente", "acceptee", "refusee");
        if (statutsValides.contains(statut)) {
            candidatureService.changerStatut(candidatureId, statut, commentaire);
            System.out.println("Statut de la candidature mis à jour !");
        } else {
            System.out.println("Statut invalide : " + statut);
        }
    }

    // ---------------------------------------------------------------
    // Utilitaires privés
    // ---------------------------------------------------------------

    private void checkSociete(User user) {
        if (!"societe".equals(user.getRole_utilisateur())) {
            throw new RuntimeException("Accès réservé aux sociétés");
        }
    }

    private OffreStage getOffreAndCheckOwner(int id, User user) {
        OffreStage offre = offreStageService.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre introuvable : " + id));
        if (offre.getSocieteId() != user.getId()) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas propriétaire de cette offre");
        }
        return offre;
    }
}