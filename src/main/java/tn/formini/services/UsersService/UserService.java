package tn.formini.services.UsersService;

import tn.formini.entities.Users.User;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements service<User> {

    Connection cnx;
    public UserService(){
        cnx= MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User u) {


        String req = "INSERT INTO user (email, roles, password, nom, prenom, telephone, gouvernorat, date_naissance, role_utilisateur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
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
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                u.setId(rs.getInt(1));
                System.out.println("Utilisateur ajouté avec succès ! ID: " + u.getId());
            } else {
                System.out.println("Erreur: Aucun ID généré pour l'utilisateur");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter utilisateur : " + ex.getMessage());
            // En cas d'erreur, s'assurer que l'ID n'est pas défini
            u.setId(0);
        }
    }

    @Override
    public void modifier(User u) {

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
