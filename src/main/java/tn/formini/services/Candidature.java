package tn.formini.services;

import tn.formini.entities.Candidature;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.*;

public class CandidatureService implements service<Candidature> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Candidature c) {
        String req = "INSERT INTO candidature (offre_stage_id, etudiant_id, statut, commentaire, date_candidature) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1,       c.getOffreStageId());
            ps.setInt(2,       c.getEtudiantId());
            ps.setString(3,    c.getStatut());
            ps.setString(4,    c.getCommentaire());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            System.out.println("Candidature ajoutée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter candidature : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Candidature c) {
        String req = "UPDATE candidature SET statut=?, commentaire=? WHERE id=?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setString(1, c.getStatut());
            ps.setString(2, c.getCommentaire());
            ps.setInt(3,    c.getId());
            ps.executeUpdate();
            System.out.println("Candidature modifiée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier candidature : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM candidature WHERE id=?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Candidature supprimée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer candidature : " + ex.getMessage());
        }
    }

    @Override
    public List<Candidature> afficher() {
        List<Candidature> list = new ArrayList<>();
        String req = "SELECT * FROM candidature";
        try (Statement ste = getCnx().createStatement();
             ResultSet rs  = ste.executeQuery(req)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            System.out.println("Erreur afficher candidatures : " + ex.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // Méthodes métier supplémentaires
    // ---------------------------------------------------------------

    /** Candidatures d'une offre donnée */
    public List<Candidature> findByOffreStage(int offreId) {
        List<Candidature> list = new ArrayList<>();
        String req = "SELECT * FROM candidature WHERE offre_stage_id=?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, offreId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            System.out.println("Erreur findByOffreStage : " + ex.getMessage());
        }
        return list;
    }

    /** Récupère une candidature par id */
    public Optional<Candidature> findById(int id) {
        String req = "SELECT * FROM candidature WHERE id=?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            System.out.println("Erreur findById candidature : " + ex.getMessage());
        }
        return Optional.empty();
    }

    /** Change le statut + commentaire d'une candidature */
    public void changerStatut(int id, String statut, String commentaire) {
        String req = "UPDATE candidature SET statut=?, commentaire=? WHERE id=?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setString(1, statut);
            ps.setString(2, commentaire);
            ps.setInt(3,    id);
            ps.executeUpdate();
            System.out.println("Statut candidature mis à jour !");
        } catch (SQLException ex) {
            System.out.println("Erreur changerStatut : " + ex.getMessage());
        }
    }

    private Candidature mapRow(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setId(rs.getInt("id"));
        c.setOffreStageId(rs.getInt("offre_stage_id"));
        c.setEtudiantId(rs.getInt("etudiant_id"));
        c.setStatut(rs.getString("statut"));
        c.setCommentaire(rs.getString("commentaire"));
        c.setDateCandidature(rs.getTimestamp("date_candidature"));
        return c;
    }
}