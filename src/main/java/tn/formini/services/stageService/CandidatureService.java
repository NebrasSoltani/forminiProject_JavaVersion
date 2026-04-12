package tn.formini.services.stageService;

import tn.formini.entities.stages.Candidature;
import tn.formini.entities.stages.OffreStage;
import tn.formini.entities.Users.User;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatureService implements service<Candidature> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Candidature candidature) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO candidature (statut, lettre_motivation, cv, date_candidature, commentaire, offre_stage_id, apprenant_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, candidature.getStatut());
            ps.setString(2, candidature.getLettre_motivation());
            ps.setString(3, candidature.getCv());
            ps.setTimestamp(4, candidature.getDate_candidature() != null ? new Timestamp(candidature.getDate_candidature().getTime()) : null);
            ps.setString(5, candidature.getCommentaire());
            ps.setInt(6, candidature.getOffreStage() != null ? candidature.getOffreStage().getId() : 0);
            ps.setInt(7, candidature.getApprenant() != null ? candidature.getApprenant().getId() : 0);
            
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                candidature.setId(rs.getInt(1));
                System.out.println("Candidature ajoutée avec succès ! ID: " + candidature.getId());
            } else {
                System.out.println("Erreur: Aucun ID généré pour la candidature");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter candidature : " + ex.getMessage());
            candidature.setId(0);
        }
    }

    @Override
    public void modifier(Candidature candidature) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE candidature SET statut=?, lettre_motivation=?, cv=?, date_candidature=?, commentaire=?, offre_stage_id=?, apprenant_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, candidature.getStatut());
            ps.setString(2, candidature.getLettre_motivation());
            ps.setString(3, candidature.getCv());
            ps.setTimestamp(4, candidature.getDate_candidature() != null ? new Timestamp(candidature.getDate_candidature().getTime()) : null);
            ps.setString(5, candidature.getCommentaire());
            ps.setInt(6, candidature.getOffreStage() != null ? candidature.getOffreStage().getId() : 0);
            ps.setInt(7, candidature.getApprenant() != null ? candidature.getApprenant().getId() : 0);
            ps.setInt(8, candidature.getId());
            
            ps.executeUpdate();
            System.out.println("Candidature modifiée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier candidature : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "DELETE FROM candidature WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Candidature supprimée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer candidature : " + ex.getMessage());
        }
    }

    @Override
    public List<Candidature> afficher() {
        Connection cnx = getCnx();
        List<Candidature> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM candidature";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setId(rs.getInt("id"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setLettre_motivation(rs.getString("lettre_motivation"));
                candidature.setCv(rs.getString("cv"));
                candidature.setDate_candidature(rs.getTimestamp("date_candidature"));
                candidature.setCommentaire(rs.getString("commentaire"));
                
                // Charger l'offre de stage associée
                int offreStageId = rs.getInt("offre_stage_id");
                if (offreStageId > 0) {
                    OffreStage offreStage = new OffreStage();
                    offreStage.setId(offreStageId);
                    candidature.setOffreStage(offreStage);
                }
                
                // Charger l'apprenant associé
                int apprenantId = rs.getInt("apprenant_id");
                if (apprenantId > 0) {
                    User apprenant = new User();
                    apprenant.setId(apprenantId);
                    candidature.setApprenant(apprenant);
                }
                
                list.add(candidature);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher candidatures : " + ex.getMessage());
        }
        return list;
    }

    // Méthode supplémentaire pour trouver une candidature par ID
    public Candidature findById(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return null;

        String req = "SELECT * FROM candidature WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setId(rs.getInt("id"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setLettre_motivation(rs.getString("lettre_motivation"));
                candidature.setCv(rs.getString("cv"));
                candidature.setDate_candidature(rs.getTimestamp("date_candidature"));
                candidature.setCommentaire(rs.getString("commentaire"));
                
                // Charger l'offre de stage associée
                int offreStageId = rs.getInt("offre_stage_id");
                if (offreStageId > 0) {
                    OffreStage offreStage = new OffreStage();
                    offreStage.setId(offreStageId);
                    candidature.setOffreStage(offreStage);
                }
                
                // Charger l'apprenant associé
                int apprenantId = rs.getInt("apprenant_id");
                if (apprenantId > 0) {
                    User apprenant = new User();
                    apprenant.setId(apprenantId);
                    candidature.setApprenant(apprenant);
                }
                
                return candidature;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById candidature : " + ex.getMessage());
        }
        return null;
    }

    // Méthode pour rechercher des candidatures par statut
    public List<Candidature> findByStatut(String statut) {
        Connection cnx = getCnx();
        List<Candidature> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM candidature WHERE statut=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setId(rs.getInt("id"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setLettre_motivation(rs.getString("lettre_motivation"));
                candidature.setCv(rs.getString("cv"));
                candidature.setDate_candidature(rs.getTimestamp("date_candidature"));
                candidature.setCommentaire(rs.getString("commentaire"));
                
                // Charger l'offre de stage associée
                int offreStageId = rs.getInt("offre_stage_id");
                if (offreStageId > 0) {
                    OffreStage offreStage = new OffreStage();
                    offreStage.setId(offreStageId);
                    candidature.setOffreStage(offreStage);
                }
                
                // Charger l'apprenant associé
                int apprenantId = rs.getInt("apprenant_id");
                if (apprenantId > 0) {
                    User apprenant = new User();
                    apprenant.setId(apprenantId);
                    candidature.setApprenant(apprenant);
                }
                
                list.add(candidature);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByStatut candidature : " + ex.getMessage());
        }
        return list;
    }

    // Méthode pour rechercher des candidatures par offre de stage
    public List<Candidature> findByOffreStage(int offreStageId) {
        Connection cnx = getCnx();
        List<Candidature> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM candidature WHERE offre_stage_id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, offreStageId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setId(rs.getInt("id"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setLettre_motivation(rs.getString("lettre_motivation"));
                candidature.setCv(rs.getString("cv"));
                candidature.setDate_candidature(rs.getTimestamp("date_candidature"));
                candidature.setCommentaire(rs.getString("commentaire"));
                
                // Charger l'offre de stage associée
                OffreStage offreStage = new OffreStage();
                offreStage.setId(offreStageId);
                candidature.setOffreStage(offreStage);
                
                // Charger l'apprenant associé
                int apprenantId = rs.getInt("apprenant_id");
                if (apprenantId > 0) {
                    User apprenant = new User();
                    apprenant.setId(apprenantId);
                    candidature.setApprenant(apprenant);
                }
                
                list.add(candidature);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByOffreStage candidature : " + ex.getMessage());
        }
        return list;
    }

    // Méthode pour rechercher des candidatures par apprenant
    public List<Candidature> findByApprenant(int apprenantId) {
        Connection cnx = getCnx();
        List<Candidature> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM candidature WHERE apprenant_id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, apprenantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Candidature candidature = new Candidature();
                candidature.setId(rs.getInt("id"));
                candidature.setStatut(rs.getString("statut"));
                candidature.setLettre_motivation(rs.getString("lettre_motivation"));
                candidature.setCv(rs.getString("cv"));
                candidature.setDate_candidature(rs.getTimestamp("date_candidature"));
                candidature.setCommentaire(rs.getString("commentaire"));
                
                // Charger l'offre de stage associée
                int offreStageId = rs.getInt("offre_stage_id");
                if (offreStageId > 0) {
                    OffreStage offreStage = new OffreStage();
                    offreStage.setId(offreStageId);
                    candidature.setOffreStage(offreStage);
                }
                
                // Charger l'apprenant associé
                User apprenant = new User();
                apprenant.setId(apprenantId);
                candidature.setApprenant(apprenant);
                
                list.add(candidature);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByApprenant candidature : " + ex.getMessage());
        }
        return list;
    }
}
