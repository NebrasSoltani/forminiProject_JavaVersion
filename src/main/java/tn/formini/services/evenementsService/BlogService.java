package tn.formini.services.evenementsService;

import tn.formini.entities.evenements.Blog;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService implements service<Blog> {
    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Blog b) {
        Connection cnx = getCnx();
        if (cnx == null) return;
        String req = "INSERT INTO blog (titre, contenu, image, date_publication, categorie, auteur_id, resume, is_publie, evenement_id, tags) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, b.getTitre());
            ps.setString(2, b.getContenu());
            ps.setString(3, b.getImage());
            ps.setTimestamp(4, b.getDate_publication() != null ? new Timestamp(b.getDate_publication().getTime()) : null);
            ps.setString(5, b.getCategorie());
            ps.setInt(6, b.getAuteur_id());
            ps.setString(7, b.getResume());
            ps.setBoolean(8, b.isIs_publie());
            ps.setObject(9, b.getEvenement_id());
            ps.setString(10, b.getTags());
            ps.executeUpdate();
            System.out.println("Blog ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur ajouter blog : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Blog b) {
        Connection cnx = getCnx();
        if (cnx == null) return;
        String req = "UPDATE blog SET titre=?, contenu=?, image=?, date_publication=?, categorie=?, auteur_id=?, resume=?, is_publie=?, evenement_id=?, tags=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, b.getTitre());
            ps.setString(2, b.getContenu());
            ps.setString(3, b.getImage());
            ps.setTimestamp(4, b.getDate_publication() != null ? new Timestamp(b.getDate_publication().getTime()) : null);
            ps.setString(5, b.getCategorie());
            ps.setInt(6, b.getAuteur_id());
            ps.setString(7, b.getResume());
            ps.setBoolean(8, b.isIs_publie());
            ps.setObject(9, b.getEvenement_id());
            ps.setString(10, b.getTags());
            ps.setInt(11, b.getId());
            ps.executeUpdate();
            System.out.println("Blog modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur modifier blog : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;
        String req = "DELETE FROM blog WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Blog supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimer blog : " + e.getMessage());
        }
    }

    @Override
    public List<Blog> afficher() {
        Connection cnx = getCnx();
        List<Blog> list = new ArrayList<>();
        if (cnx == null) return list;
        String req = "SELECT * FROM blog";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitre(rs.getString("titre"));
                b.setContenu(rs.getString("contenu"));
                b.setImage(rs.getString("image"));
                b.setDate_publication(rs.getTimestamp("date_publication"));
                b.setCategorie(rs.getString("categorie"));
                b.setAuteur_id(rs.getInt("auteur_id"));
                b.setResume(rs.getString("resume"));
                b.setIs_publie(rs.getBoolean("is_publie"));
                int evtId = rs.getInt("evenement_id");
                if (rs.wasNull()) b.setEvenement_id(null);
                else b.setEvenement_id(evtId);
                b.setTags(rs.getString("tags"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.out.println("Erreur afficher blog : " + e.getMessage());
        }
        return list;
    }
}