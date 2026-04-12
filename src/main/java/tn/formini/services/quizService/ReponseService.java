package tn.formini.services.quizService;

import tn.formini.entities.Quizs.Question;
import tn.formini.entities.Quizs.Reponse;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {

    Connection cnx;

    public ReponseService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // ─── CREATE ───────────────────────────────────────────────
    public void ajouter(Reponse r) {
        r.valider();
        String req = "INSERT INTO reponse (texte, est_correcte, explication_reponse, question_id) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, r.getTexte());
            ps.setBoolean(2, r.isEst_correcte());
            ps.setString(3, r.getExplication_reponse());
            ps.setInt(4, r.getQuestion().getId());
            ps.executeUpdate();
            System.out.println("Réponse ajoutée !");
        } catch (SQLException e) {
            System.out.println("Erreur ajouter réponse : " + e.getMessage());
        }
    }

    // ─── READ ALL ─────────────────────────────────────────────
    public List<Reponse> getAll() {
        List<Reponse> list = new ArrayList<>();
        String req = "SELECT * FROM reponse";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Reponse r = new Reponse();
                r.setId(rs.getInt("id"));
                r.setTexte(rs.getString("texte"));
                r.setEst_correcte(rs.getBoolean("est_correcte"));
                r.setExplication_reponse(rs.getString("explication_reponse"));

                Question q = new Question();
                q.setId(rs.getInt("question_id"));
                r.setQuestion(q);

                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll réponse : " + e.getMessage());
        }
        return list;
    }

    // ─── READ ONE ─────────────────────────────────────────────
    public Reponse getById(int id) {
        String req = "SELECT * FROM reponse WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reponse r = new Reponse();
                r.setId(rs.getInt("id"));
                r.setTexte(rs.getString("texte"));
                r.setEst_correcte(rs.getBoolean("est_correcte"));
                r.setExplication_reponse(rs.getString("explication_reponse"));
                return r;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getById réponse : " + e.getMessage());
        }
        return null;
    }

    // ─── UPDATE ───────────────────────────────────────────────
    public void modifier(Reponse r) {
        r.valider();
        String req = "UPDATE reponse SET texte=?, est_correcte=?, explication_reponse=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, r.getTexte());
            ps.setBoolean(2, r.isEst_correcte());
            ps.setString(3, r.getExplication_reponse());
            ps.setInt(4, r.getId());
            ps.executeUpdate();
            System.out.println("Réponse modifiée !");
        } catch (SQLException e) {
            System.out.println("Erreur modifier réponse : " + e.getMessage());
        }
    }

    // ─── DELETE ───────────────────────────────────────────────
    public void supprimer(int id) {
        String req = "DELETE FROM reponse WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Réponse supprimée !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimer réponse : " + e.getMessage());
        }
    }
}