package tn.formini.services;

import tn.formini.entities.Quiz;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    Connection cnx;

    public QuizService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // ─── CREATE ───────────────────────────────────────────────
    public void ajouter(Quiz q) {
        q.valider();
        String req = "INSERT INTO quiz (titre, description, duree, note_minimale, afficher_correction, melanger, formation_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, q.getTitre());
            ps.setString(2, q.getDescription());
            ps.setInt(3, q.getDuree());
            ps.setInt(4, q.getNote_minimale());
            ps.setBoolean(5, q.isAfficher_correction());
            ps.setBoolean(6, q.isMelanger());
            ps.setInt(7, q.getFormation().getId());
            ps.executeUpdate();
            System.out.println("Quiz ajouté !");
        } catch (SQLException e) {
            System.out.println("Erreur ajouter quiz : " + e.getMessage());
        }
    }

    // ─── READ ALL ─────────────────────────────────────────────
    public List<Quiz> getAll() {
        List<Quiz> list = new ArrayList<>();
        String req = "SELECT * FROM quiz";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitre(rs.getString("titre"));
                q.setDescription(rs.getString("description"));
                q.setDuree(rs.getInt("duree"));
                q.setNote_minimale(rs.getInt("note_minimale"));
                q.setAfficher_correction(rs.getBoolean("afficher_correction"));
                q.setMelanger(rs.getBoolean("melanger"));
                list.add(q);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll quiz : " + e.getMessage());
        }
        return list;
    }

    // ─── READ ONE ─────────────────────────────────────────────
    public Quiz getById(int id) {
        String req = "SELECT * FROM quiz WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitre(rs.getString("titre"));
                q.setDescription(rs.getString("description"));
                q.setDuree(rs.getInt("duree"));
                q.setNote_minimale(rs.getInt("note_minimale"));
                q.setAfficher_correction(rs.getBoolean("afficher_correction"));
                q.setMelanger(rs.getBoolean("melanger"));
                return q;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getById quiz : " + e.getMessage());
        }
        return null;
    }

    // ─── UPDATE ───────────────────────────────────────────────
    public void modifier(Quiz q) {
        q.valider();
        String req = "UPDATE quiz SET titre=?, description=?, duree=?, note_minimale=?, afficher_correction=?, melanger=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, q.getTitre());
            ps.setString(2, q.getDescription());
            ps.setInt(3, q.getDuree());
            ps.setInt(4, q.getNote_minimale());
            ps.setBoolean(5, q.isAfficher_correction());
            ps.setBoolean(6, q.isMelanger());
            ps.setInt(7, q.getId());
            ps.executeUpdate();
            System.out.println("Quiz modifié !");
        } catch (SQLException e) {
            System.out.println("Erreur modifier quiz : " + e.getMessage());
        }
    }

    // ─── DELETE ───────────────────────────────────────────────
    public void supprimer(int id) {
        String req = "DELETE FROM quiz WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Quiz supprimé !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimer quiz : " + e.getMessage());
        }
    }
}