package tn.formini.services;

import tn.formini.entities.Evenement;
import tn.formini.entities.ParticipationEvenement;
import tn.formini.entities.User;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationEvenementService implements service<ParticipationEvenement> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(ParticipationEvenement p) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO participation_evenement (date_participation, user_id, evenement_id) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setTimestamp(1, p.getDate_participation() != null ? new Timestamp(p.getDate_participation().getTime()) : null);
            ps.setInt(2, (p.getUser() != null) ? p.getUser().getId() : 0);
            ps.setInt(3, (p.getEvenement() != null) ? p.getEvenement().getId() : 0);
            ps.executeUpdate();
            System.out.println("Participation ajoutée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(ParticipationEvenement p) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE participation_evenement SET date_participation=?, user_id=?, evenement_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setTimestamp(1, p.getDate_participation() != null ? new Timestamp(p.getDate_participation().getTime()) : null);
            ps.setInt(2, (p.getUser() != null) ? p.getUser().getId() : 0);
            ps.setInt(3, (p.getEvenement() != null) ? p.getEvenement().getId() : 0);
            ps.setInt(4, p.getId());
            ps.executeUpdate();
            System.out.println("Participation modifiée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "DELETE FROM participation_evenement WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Participation supprimée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer : " + ex.getMessage());
        }
    }

    @Override
    public List<ParticipationEvenement> afficher() {
        Connection cnx = getCnx();
        List<ParticipationEvenement> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM participation_evenement";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                ParticipationEvenement p = new ParticipationEvenement();
                p.setId(rs.getInt("id"));
                p.setDate_participation(rs.getTimestamp("date_participation"));
                
                User u = new User();
                u.setId(rs.getInt("user_id"));
                p.setUser(u);
                
                Evenement e = new Evenement();
                e.setId(rs.getInt("evenement_id"));
                p.setEvenement(e);
                
                list.add(p);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher : " + ex.getMessage());
        }
        return list;
    }
}
