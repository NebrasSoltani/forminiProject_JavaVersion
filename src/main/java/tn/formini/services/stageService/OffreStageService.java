package tn.formini.services.stageService;

import tn.formini.entities.stages.OffreStage;
import tn.formini.entities.Users.User;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreStageService implements service<OffreStage> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(OffreStage offre) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO offre_stage (titre, description, entreprise, domaine, competences_requises, profil_demande, duree, date_debut, date_fin, type_stage, lieu, remuneration, contact_email, contact_tel, statut, date_publication, societe_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription() != null ? offre.getDescription() : "Pas de description");
            ps.setString(3, offre.getEntreprise() != null ? offre.getEntreprise() : "Société");
            ps.setString(4, offre.getDomaine() != null ? offre.getDomaine() : "Général");
            ps.setString(5, offre.getCompetences_requises() != null ? offre.getCompetences_requises() : "");
            ps.setString(6, offre.getProfil_demande() != null ? offre.getProfil_demande() : "");
            ps.setString(7, offre.getDuree() != null ? offre.getDuree() : "3 mois");
            ps.setTimestamp(8, offre.getDate_debut() != null ? new Timestamp(offre.getDate_debut().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(9, offre.getDate_fin() != null ? new Timestamp(offre.getDate_fin().getTime()) : new Timestamp(System.currentTimeMillis() + 7776000000L));
            ps.setString(10, offre.getType_stage() != null ? offre.getType_stage() : "Stage");
            ps.setString(11, offre.getLieu() != null ? offre.getLieu() : "Tunis");
            ps.setString(12, offre.getRemuneration() != null ? offre.getRemuneration() : "Non spécifié");
            ps.setString(13, offre.getContact_email() != null ? offre.getContact_email() : "contact@formini.tn");
            ps.setString(14, offre.getContact_tel() != null ? offre.getContact_tel() : "00000000");
            ps.setString(15, offre.getStatut() != null ? offre.getStatut() : "ouvert");
            ps.setTimestamp(16, new Timestamp(System.currentTimeMillis()));
            ps.setInt(17, offre.getSociete() != null ? offre.getSociete().getId() : 1);
            
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                offre.setId(rs.getInt(1));
                System.out.println("Offre de stage ajoutée avec succès ! ID: " + offre.getId());
            } else {
                System.out.println("Erreur: Aucun ID généré pour l'offre de stage");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter offre de stage : " + ex.getMessage());
            offre.setId(0);
        }
    }

    @Override
    public void modifier(OffreStage offre) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE offre_stage SET titre=?, description=?, entreprise=?, domaine=?, competences_requises=?, profil_demande=?, duree=?, date_debut=?, date_fin=?, type_stage=?, lieu=?, remuneration=?, contact_email=?, contact_tel=?, statut=?, date_publication=?, societe_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setString(3, offre.getEntreprise());
            ps.setString(4, offre.getDomaine());
            ps.setString(5, offre.getCompetences_requises());
            ps.setString(6, offre.getProfil_demande());
            ps.setString(7, offre.getDuree());
            ps.setTimestamp(8, offre.getDate_debut() != null ? new Timestamp(offre.getDate_debut().getTime()) : null);
            ps.setTimestamp(9, offre.getDate_fin() != null ? new Timestamp(offre.getDate_fin().getTime()) : null);
            ps.setString(10, offre.getType_stage());
            ps.setString(11, offre.getLieu());
            ps.setString(12, offre.getRemuneration());
            ps.setString(13, offre.getContact_email());
            ps.setString(14, offre.getContact_tel());
            ps.setString(15, offre.getStatut());
            ps.setTimestamp(16, offre.getDate_publication() != null ? new Timestamp(offre.getDate_publication().getTime()) : null);
            ps.setInt(17, offre.getSociete() != null ? offre.getSociete().getId() : 0);
            ps.setInt(18, offre.getId());
            
            ps.executeUpdate();
            System.out.println("Offre de stage modifiée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier offre de stage : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "DELETE FROM offre_stage WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Offre de stage supprimée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer offre de stage : " + ex.getMessage());
        }
    }

    @Override
    public List<OffreStage> afficher() {
        Connection cnx = getCnx();
        List<OffreStage> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM offre_stage";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                OffreStage offre = new OffreStage();
                offre.setId(rs.getInt("id"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescription(rs.getString("description"));
                offre.setEntreprise(rs.getString("entreprise"));
                offre.setDomaine(rs.getString("domaine"));
                offre.setCompetences_requises(rs.getString("competences_requises"));
                offre.setProfil_demande(rs.getString("profil_demande"));
                offre.setDuree(rs.getString("duree"));
                offre.setDate_debut(rs.getTimestamp("date_debut"));
                offre.setDate_fin(rs.getTimestamp("date_fin"));
                offre.setType_stage(rs.getString("type_stage"));
                offre.setLieu(rs.getString("lieu"));
                offre.setRemuneration(rs.getString("remuneration"));
                offre.setContact_email(rs.getString("contact_email"));
                offre.setContact_tel(rs.getString("contact_tel"));
                offre.setStatut(rs.getString("statut"));
                offre.setDate_publication(rs.getTimestamp("date_publication"));
                
                // Charger la société associée
                int societeId = rs.getInt("societe_id");
                if (societeId > 0) {
                    User societe = new User();
                    societe.setId(societeId);
                    offre.setSociete(societe);
                }
                
                list.add(offre);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher offres de stage : " + ex.getMessage());
        }
        return list;
    }

    // Méthode supplémentaire pour trouver une offre par ID
    public OffreStage findById(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return null;

        String req = "SELECT * FROM offre_stage WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                OffreStage offre = new OffreStage();
                offre.setId(rs.getInt("id"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescription(rs.getString("description"));
                offre.setEntreprise(rs.getString("entreprise"));
                offre.setDomaine(rs.getString("domaine"));
                offre.setCompetences_requises(rs.getString("competences_requises"));
                offre.setProfil_demande(rs.getString("profil_demande"));
                offre.setDuree(rs.getString("duree"));
                offre.setDate_debut(rs.getTimestamp("date_debut"));
                offre.setDate_fin(rs.getTimestamp("date_fin"));
                offre.setType_stage(rs.getString("type_stage"));
                offre.setLieu(rs.getString("lieu"));
                offre.setRemuneration(rs.getString("remuneration"));
                offre.setContact_email(rs.getString("contact_email"));
                offre.setContact_tel(rs.getString("contact_tel"));
                offre.setStatut(rs.getString("statut"));
                offre.setDate_publication(rs.getTimestamp("date_publication"));
                
                // Charger la société associée
                int societeId = rs.getInt("societe_id");
                if (societeId > 0) {
                    User societe = new User();
                    societe.setId(societeId);
                    offre.setSociete(societe);
                }
                
                return offre;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById offre de stage : " + ex.getMessage());
        }
        return null;
    }

    // Méthode pour rechercher des offres par statut
    public List<OffreStage> findByStatut(String statut) {
        Connection cnx = getCnx();
        List<OffreStage> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM offre_stage WHERE statut=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OffreStage offre = new OffreStage();
                offre.setId(rs.getInt("id"));
                offre.setTitre(rs.getString("titre"));
                offre.setDescription(rs.getString("description"));
                offre.setEntreprise(rs.getString("entreprise"));
                offre.setDomaine(rs.getString("domaine"));
                offre.setCompetences_requises(rs.getString("competences_requises"));
                offre.setProfil_demande(rs.getString("profil_demande"));
                offre.setDuree(rs.getString("duree"));
                offre.setDate_debut(rs.getTimestamp("date_debut"));
                offre.setDate_fin(rs.getTimestamp("date_fin"));
                offre.setType_stage(rs.getString("type_stage"));
                offre.setLieu(rs.getString("lieu"));
                offre.setRemuneration(rs.getString("remuneration"));
                offre.setContact_email(rs.getString("contact_email"));
                offre.setContact_tel(rs.getString("contact_tel"));
                offre.setStatut(rs.getString("statut"));
                offre.setDate_publication(rs.getTimestamp("date_publication"));
                
                // Charger la société associée
                int societeId = rs.getInt("societe_id");
                if (societeId > 0) {
                    User societe = new User();
                    societe.setId(societeId);
                    offre.setSociete(societe);
                }
                
                list.add(offre);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByStatut offre de stage : " + ex.getMessage());
        }
        return list;
    }
}
