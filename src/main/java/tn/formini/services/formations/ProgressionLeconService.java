package tn.formini.services.formations;

import tn.formini.tools.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ProgressionLeconService {

    private final Connection cnx;

    public ProgressionLeconService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public Set<Integer> findCompletedLeconIds(int apprenantId, int formationId) {
        Set<Integer> ids = new HashSet<>();
        if (cnx == null) {
            return ids;
        }

        String req = "SELECT pl.lecon_id FROM progression_lecon pl " +
                "JOIN lecon l ON l.id = pl.lecon_id " +
                "WHERE pl.apprenant_id=? AND l.formation_id=? AND pl.terminee=1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, apprenantId);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("lecon_id"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur findCompletedLeconIds : " + ex.getMessage());
        }
        return ids;
    }

    public void markCompleted(int apprenantId, int leconId) {
        if (cnx == null) {
            return;
        }

        String updateReq = "UPDATE progression_lecon SET terminee=1, date_terminee=? WHERE apprenant_id=? AND lecon_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(updateReq)) {
            ps.setTimestamp(1, new Timestamp(new Date().getTime()));
            ps.setInt(2, apprenantId);
            ps.setInt(3, leconId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                return;
            }
        } catch (SQLException ex) {
            System.err.println("Erreur update progression_lecon : " + ex.getMessage());
        }

        String insertReq = "INSERT INTO progression_lecon (terminee, date_terminee, apprenant_id, lecon_id) VALUES (1, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertReq)) {
            ps.setTimestamp(1, new Timestamp(new Date().getTime()));
            ps.setInt(2, apprenantId);
            ps.setInt(3, leconId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Erreur insert progression_lecon : " + ex.getMessage());
        }
    }
}

