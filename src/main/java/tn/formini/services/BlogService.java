package tn.formini.services;

import tn.formini.entities.Blog;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService implements service<Blog> {
    Connection cnx;

    public BlogService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Blog b) {
        if (cnx == null) return;
        String req = "INSERT INTO blog (titre, contenu, image, date_publication, categorie, user_id, resume_auto, is_actif, live, url_live) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, b.getTitre());
            ps.setString(2, b.getContenu());
            ps.setString(3, b.getImage());
            ps.setTimestamp(4, b.getDate_publication() != null ? new Timestamp(b.getDate_publication().getTime()) : null);
            ps.setString(5, b.getCategorie());
            ps.setInt(6, b.getUser_id());
            ps.setString(7, b.getResume_auto());
            ps.setInt(8, b.getIs_actif());
            ps.setInt(9, b.getLive());
            ps.setString(10, b.getUrl_live());
            ps.executeUpdate();
            System.out.println("Blog ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Blog b) {
        if (cnx == null) return;
        String req = "UPDATE blog SET titre=?, contenu=?, image=?, date_publication=?, categorie=?, user_id=?, resume_auto=?, is_actif=?, live=?, url_live=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, b.getTitre());
            ps.setString(2, b.getContenu());
            ps.setString(3, b.getImage());
            ps.setTimestamp(4, b.getDate_publication() != null ? new Timestamp(b.getDate_publication().getTime()) : null);
            ps.setString(5, b.getCategorie());
            ps.setInt(6, b.getUser_id());
            ps.setString(7, b.getResume_auto());
            ps.setInt(8, b.getIs_actif());
            ps.setInt(9, b.getLive());
            ps.setString(10, b.getUrl_live());
            ps.setInt(11, b.getId());
            ps.executeUpdate();
            System.out.println("Blog modifié !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        if (cnx == null) return;
        String req = "DELETE FROM blog WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Blog supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Blog> afficher() {
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
                b.setUser_id(rs.getInt("user_id"));
                b.setResume_auto(rs.getString("resume_auto"));
                b.setIs_actif(rs.getInt("is_actif"));
                b.setLive(rs.getInt("live"));
                b.setUrl_live(rs.getString("url_live"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
}