package tn.formini.services.evenementsService;

import tn.formini.entities.evenements.LiveReaction;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LiveReactionService implements service<LiveReaction> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(LiveReaction r) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO live_reaction (type, created_at, user_id, evenement_id) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, r.getType());
            ps.setTimestamp(2, r.getCreated_at() != null ? new Timestamp(r.getCreated_at().getTime()) : null);
            ps.setInt(3, (r.getUser() != null) ? r.getUser().getId() : 0);
            ps.setInt(4, (r.getEvenement() != null) ? r.getEvenement().getId() : 0);
            ps.executeUpdate();
            System.out.println("Réaction live ajoutée !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(LiveReaction r) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE live_reaction SET type=?, created_at=?, user_id=?, evenement_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, r.getType());
            ps.setTimestamp(2, r.getCreated_at() != null ? new Timestamp(r.getCreated_at().getTime()) : null);
            ps.setInt(3, (r.getUser() != null) ? r.getUser().getId() : 0);
            ps.setInt(4, (r.getEvenement() != null) ? r.getEvenement().getId() : 0);
            ps.setInt(5, r.getId());
            ps.executeUpdate();
            System.out.println("Réaction live modifiée !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "DELETE FROM live_reaction WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Réaction live supprimée !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer : " + ex.getMessage());
        }
    }

    @Override
    public List<LiveReaction> afficher() {
        Connection cnx = getCnx();
        List<LiveReaction> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM live_reaction";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                LiveReaction r = new LiveReaction();
                r.setId(rs.getInt("id"));
                r.setType(rs.getString("type"));
                r.setCreated_at(rs.getTimestamp("created_at"));
                list.add(r);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher : " + ex.getMessage());
        }
        return list;
    }
}
