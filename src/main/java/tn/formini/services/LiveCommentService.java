package tn.formini.services;

import tn.formini.entities.Evenement;
import tn.formini.entities.LiveComment;
import tn.formini.entities.User;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LiveCommentService implements service<LiveComment> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(LiveComment c) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO live_comment (content, created_at, user_id, evenement_id) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getContent());
            ps.setTimestamp(2, c.getCreated_at() != null ? new Timestamp(c.getCreated_at().getTime()) : null);
            ps.setInt(3, (c.getUser() != null) ? c.getUser().getId() : 0);
            ps.setInt(4, (c.getEvenement() != null) ? c.getEvenement().getId() : 0);
            ps.executeUpdate();
            System.out.println("Commentaire live ajouté !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(LiveComment c) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE live_comment SET content=?, created_at=?, user_id=?, evenement_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getContent());
            ps.setTimestamp(2, c.getCreated_at() != null ? new Timestamp(c.getCreated_at().getTime()) : null);
            ps.setInt(3, (c.getUser() != null) ? c.getUser().getId() : 0);
            ps.setInt(4, (c.getEvenement() != null) ? c.getEvenement().getId() : 0);
            ps.setInt(5, c.getId());
            ps.executeUpdate();
            System.out.println("Commentaire live modifié !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "DELETE FROM live_comment WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Commentaire live supprimé !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer : " + ex.getMessage());
        }
    }

    @Override
    public List<LiveComment> afficher() {
        Connection cnx = getCnx();
        List<LiveComment> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM live_comment";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                LiveComment c = new LiveComment();
                c.setId(rs.getInt("id"));
                c.setContent(rs.getString("content"));
                c.setCreated_at(rs.getTimestamp("created_at"));
                list.add(c);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher : " + ex.getMessage());
        }
        return list;
    }
}
