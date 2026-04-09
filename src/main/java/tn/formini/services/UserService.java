package tn.formini.services;

import tn.formini.entities.User;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements service<User> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User u) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO user (email, roles, password, nom, prenom, telephone, gouvernorat, date_naissance, role_utilisateur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getNom());
            ps.setString(5, u.getPrenom());
            ps.setString(6, u.getTelephone());
            ps.setString(7, u.getGouvernorat());
            ps.setTimestamp(8, u.getDate_naissance() != null ? new Timestamp(u.getDate_naissance().getTime()) : null);
            ps.setString(9, u.getRole_utilisateur());
            ps.executeUpdate();
            System.out.println("Utilisateur ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter utilisateur : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(User u) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE user SET email=?, roles=?, password=?, nom=?, prenom=?, telephone=?, gouvernorat=?, date_naissance=?, role_utilisateur=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getNom());
            ps.setString(5, u.getPrenom());
            ps.setString(6, u.getTelephone());
            ps.setString(7, u.getGouvernorat());
            ps.setTimestamp(8, u.getDate_naissance() != null ? new Timestamp(u.getDate_naissance().getTime()) : null);
            ps.setString(9, u.getRole_utilisateur());
            ps.setInt(10, u.getId());
            ps.executeUpdate();
            System.out.println("Utilisateur modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier utilisateur : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;
        String req = "DELETE FROM user WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Utilisateur supprimé avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer utilisateur : " + ex.getMessage());
        }
    }

    @Override
    public List<User> afficher() {
        Connection cnx = getCnx();
        List<User> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM user";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setRole_utilisateur(rs.getString("role_utilisateur"));
                list.add(u);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher utilisateur : " + ex.getMessage());
        }
        return list;
    }
}
