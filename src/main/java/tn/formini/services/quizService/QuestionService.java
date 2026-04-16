package tn.formini.services.quizService;

import tn.formini.entities.Question;
import tn.formini.entities.Quiz;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    Connection cnx;

    public QuestionService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    /** Rafraîchit la connexion depuis le singleton (utile si connexion rétablie depuis) */
    private Connection getCnx() {
        cnx = MyDataBase.getInstance().getCnx();
        return cnx;
    }

    // ─── CREATE ───────────────────────────────────────────────
    public void ajouter(Question q) {
        q.valider();
        String req = "INSERT INTO question (enonce, type, points, ordre, explication, explications_detaillees, quiz_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, q.getEnonce());
            ps.setString(2, q.getType());
            ps.setInt(3, q.getPoints());
            ps.setInt(4, q.getOrdre());
            ps.setString(5, q.getExplication());
            ps.setString(6, q.getExplications_detaillees());
            ps.setInt(7, q.getQuiz().getId());
            ps.executeUpdate();
            System.out.println("Question ajoutée !");
        } catch (SQLException e) {
            System.out.println("Erreur ajouter question : " + e.getMessage());
        }
    }

    // ─── READ ALL ─────────────────────────────────────────────
    public List<Question> getAll() {
        List<Question> list = new ArrayList<>();
        if (getCnx() == null) {
            System.out.println("[QuestionService] Connexion DB indisponible.");
            return list;
        }
        String req = "SELECT * FROM question";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt("id"));
                q.setEnonce(rs.getString("enonce"));
                q.setType(rs.getString("type"));
                q.setPoints(rs.getInt("points"));
                q.setOrdre(rs.getInt("ordre"));
                q.setExplication(rs.getString("explication"));
                q.setExplications_detaillees(rs.getString("explications_detaillees"));

                Quiz quiz = new Quiz();
                quiz.setId(rs.getInt("quiz_id"));
                q.setQuiz(quiz);

                list.add(q);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll question : " + e.getMessage());
        }
        return list;
    }

    // ─── READ ONE ─────────────────────────────────────────────
    public Question getById(int id) {
        String req = "SELECT * FROM question WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt("id"));
                q.setEnonce(rs.getString("enonce"));
                q.setType(rs.getString("type"));
                q.setPoints(rs.getInt("points"));
                q.setOrdre(rs.getInt("ordre"));
                q.setExplication(rs.getString("explication"));
                q.setExplications_detaillees(rs.getString("explications_detaillees"));
                return q;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getById question : " + e.getMessage());
        }
        return null;
    }

    // ─── UPDATE ───────────────────────────────────────────────
    public void modifier(Question q) {
        q.valider();
        String req = "UPDATE question SET enonce=?, type=?, points=?, ordre=?, explication=?, explications_detaillees=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, q.getEnonce());
            ps.setString(2, q.getType());
            ps.setInt(3, q.getPoints());
            ps.setInt(4, q.getOrdre());
            ps.setString(5, q.getExplication());
            ps.setString(6, q.getExplications_detaillees());
            ps.setInt(7, q.getId());
            ps.executeUpdate();
            System.out.println("Question modifiée !");
        } catch (SQLException e) {
            System.out.println("Erreur modifier question : " + e.getMessage());
        }
    }

    // ─── DELETE ───────────────────────────────────────────────
    public void supprimer(int id) {
        String req = "DELETE FROM question WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Question supprimée !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimer question : " + e.getMessage());
        }
    }
}