package tn.formini.services.quizService;

import tn.formini.entities.Quizs.Quiz;
import tn.formini.entities.Quizs.ResultatQuiz;
import tn.formini.entities.Users.User;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultatQuizService {

    Connection cnx;

    public ResultatQuizService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // ─── CREATE ───────────────────────────────────────────────
    public void ajouter(ResultatQuiz r) {
        r.valider();
        String req = "INSERT INTO resultat_quiz (note, nombre_bonnes_reponses, nombre_total_questions, date_tentative, reussi, details_reponses, apprenant_id, quiz_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setBigDecimal(1, r.getNote());
            ps.setInt(2, r.getNombre_bonnes_reponses());
            ps.setInt(3, r.getNombre_total_questions());
            ps.setDate(4, new java.sql.Date(r.getDate_tentative().getTime()));
            ps.setBoolean(5, r.isReussi());
            ps.setString(6, r.getDetails_reponses());
            ps.setInt(7, r.getApprenant().getId());
            ps.setInt(8, r.getQuiz().getId());
            ps.executeUpdate();
            System.out.println("Résultat ajouté !");
        } catch (SQLException e) {
            System.out.println("Erreur ajouter résultat : " + e.getMessage());
        }
    }

    // ─── READ ALL ─────────────────────────────────────────────
    public List<ResultatQuiz> getAll() {
        List<ResultatQuiz> list = new ArrayList<>();
        String req = "SELECT * FROM resultat_quiz";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                ResultatQuiz r = new ResultatQuiz();
                r.setId(rs.getInt("id"));
                r.setNote(rs.getBigDecimal("note"));
                r.setNombre_bonnes_reponses(rs.getInt("nombre_bonnes_reponses"));
                r.setNombre_total_questions(rs.getInt("nombre_total_questions"));
                r.setDate_tentative(rs.getDate("date_tentative"));
                r.setReussi(rs.getBoolean("reussi"));
                r.setDetails_reponses(rs.getString("details_reponses"));

                User u = new User();
                u.setId(rs.getInt("apprenant_id"));
                r.setApprenant(u);

                Quiz q = new Quiz();
                q.setId(rs.getInt("quiz_id"));
                r.setQuiz(q);

                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll résultat : " + e.getMessage());
        }
        return list;
    }

    // ─── READ ONE ─────────────────────────────────────────────
    public ResultatQuiz getById(int id) {
        String req = "SELECT * FROM resultat_quiz WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ResultatQuiz r = new ResultatQuiz();
                r.setId(rs.getInt("id"));
                r.setNote(rs.getBigDecimal("note"));
                r.setNombre_bonnes_reponses(rs.getInt("nombre_bonnes_reponses"));
                r.setNombre_total_questions(rs.getInt("nombre_total_questions"));
                r.setDate_tentative(rs.getDate("date_tentative"));
                r.setReussi(rs.getBoolean("reussi"));
                r.setDetails_reponses(rs.getString("details_reponses"));
                return r;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getById résultat : " + e.getMessage());
        }
        return null;
    }

    // ─── UPDATE ───────────────────────────────────────────────
    public void modifier(ResultatQuiz r) {
        r.valider();
        String req = "UPDATE resultat_quiz SET note=?, nombre_bonnes_reponses=?, nombre_total_questions=?, date_tentative=?, reussi=?, details_reponses=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setBigDecimal(1, r.getNote());
            ps.setInt(2, r.getNombre_bonnes_reponses());
            ps.setInt(3, r.getNombre_total_questions());
            ps.setDate(4, new java.sql.Date(r.getDate_tentative().getTime()));
            ps.setBoolean(5, r.isReussi());
            ps.setString(6, r.getDetails_reponses());
            ps.setInt(7, r.getId());
            ps.executeUpdate();
            System.out.println("Résultat modifié !");
        } catch (SQLException e) {
            System.out.println("Erreur modifier résultat : " + e.getMessage());
        }
    }

    // ─── DELETE ───────────────────────────────────────────────
    public void supprimer(int id) {
        String req = "DELETE FROM resultat_quiz WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Résultat supprimé !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimer résultat : " + e.getMessage());
        }
    }
}