package tn.formini.services.formations;

import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Inscription;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InscriptionService {
    private final Connection cnx;

    public InscriptionService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public List<Formation> findFormationsByApprenant(int userId) {
        List<Formation> formations = new ArrayList<>();
        String req = "SELECT f.* FROM formation f " +
                     "JOIN inscription i ON f.id = i.formation_id " +
                     "WHERE i.apprenant_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Formation f = new Formation();
                    f.setId(rs.getInt("id"));
                    f.setTitre(rs.getString("titre"));
                    f.setCategorie(rs.getString("categorie"));
                    f.setNiveau(rs.getString("niveau"));
                    formations.add(f);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur findFormationsByApprenant : " + ex.getMessage());
        }
        return formations;
    }

    public boolean isAlreadyInscrit(int apprenantId, int formationId) {
        if (cnx == null) return false;
        String req = "SELECT 1 FROM inscription WHERE apprenant_id=? AND formation_id=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, apprenantId);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            System.err.println("Erreur isAlreadyInscrit : " + ex.getMessage());
            return false;
        }
    }

    public boolean inscrire(int apprenantId, int formationId) {
        if (cnx == null) return false;
        if (isAlreadyInscrit(apprenantId, formationId)) {
            return true;
        }

        String req = "INSERT INTO inscription (date_inscription, statut, progression, certificat_obtenu, apprenant_id, formation_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setTimestamp(1, new Timestamp(new Date().getTime()));
            ps.setString(2, "en_cours");
            ps.setInt(3, 0);
            ps.setBoolean(4, false);
            ps.setInt(5, apprenantId);
            ps.setInt(6, formationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur inscription formation : " + ex.getMessage());
            return false;
        }
    }

    public Inscription findByApprenantAndFormation(int apprenantId, int formationId) {
        if (cnx == null) return null;
        String req = "SELECT * FROM inscription WHERE apprenant_id=? AND formation_id=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, apprenantId);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Inscription inscription = new Inscription();
                    inscription.setId(rs.getInt("id"));
                    inscription.setDate_inscription(rs.getTimestamp("date_inscription"));
                    inscription.setStatut(rs.getString("statut"));
                    inscription.setProgression(rs.getInt("progression"));
                    inscription.setCertificat_obtenu(rs.getBoolean("certificat_obtenu"));
                    return inscription;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur findByApprenantAndFormation : " + ex.getMessage());
        }
        return null;
    }
}
