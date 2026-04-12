package tn.formini.services.UsersService;

import tn.formini.entities.Users.Societe;
import tn.formini.entities.Users.User;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SocieteService implements service<Societe> {

    Connection cnx;
    public SocieteService() {
        cnx= MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Societe s) {


        String req = "INSERT INTO societe (nom_societe, secteur, description, adresse, site_web, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getNom_societe());
            ps.setString(2, s.getSecteur());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getAdresse());
            ps.setString(5, s.getSite_web());
            
            if (s.getUser() != null) {
                ps.setInt(6, s.getUser().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                s.setId(rs.getInt(1));
                System.out.println("Societe ajoutée avec succès ! ID: " + s.getId());
            } else {
                System.out.println("Erreur: Aucun ID généré pour la société");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter société : " + ex.getMessage());
            s.setId(0);
        }
    }

    @Override
    public void modifier(Societe s) {


        String req = "UPDATE societe SET nom_societe=?, secteur=?, description=?, adresse=?, site_web=?, user_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, s.getNom_societe());
            ps.setString(2, s.getSecteur());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getAdresse());
            ps.setString(5, s.getSite_web());
            
            if (s.getUser() != null) {
                ps.setInt(6, s.getUser().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            
            ps.setInt(7, s.getId());
            ps.executeUpdate();
            System.out.println("Societe modifiée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier société : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {

        
        String req = "DELETE FROM societe WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Societe supprimée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer société : " + ex.getMessage());
        }
    }

    @Override
    public List<Societe> afficher() {
        List<Societe> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM societe ORDER BY nom_societe";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Societe s = new Societe();
                s.setId(rs.getInt("id"));
                s.setNom_societe(rs.getString("nom_societe"));
                s.setSecteur(rs.getString("secteur"));
                s.setDescription(rs.getString("description"));
                s.setAdresse(rs.getString("adresse"));
                s.setSite_web(rs.getString("site_web"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    s.setUser(user);
                }
                
                list.add(s);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher société : " + ex.getMessage());
        }
        return list;
    }

    public Societe findById(int id) {


        String req = "SELECT * FROM societe WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Societe s = new Societe();
                s.setId(rs.getInt("id"));
                s.setNom_societe(rs.getString("nom_societe"));
                s.setSecteur(rs.getString("secteur"));
                s.setDescription(rs.getString("description"));
                s.setAdresse(rs.getString("adresse"));
                s.setSite_web(rs.getString("site_web"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    s.setUser(user);
                }
                
                return s;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById société : " + ex.getMessage());
        }
        return null;
    }

    public List<Societe> findByNom(String nom) {
        List<Societe> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM societe WHERE nom_societe LIKE ? ORDER BY nom_societe";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, "%" + nom + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Societe s = new Societe();
                s.setId(rs.getInt("id"));
                s.setNom_societe(rs.getString("nom_societe"));
                s.setSecteur(rs.getString("secteur"));
                s.setDescription(rs.getString("description"));
                s.setAdresse(rs.getString("adresse"));
                s.setSite_web(rs.getString("site_web"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    s.setUser(user);
                }
                
                list.add(s);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByNom société : " + ex.getMessage());
        }
        return list;
    }

    public List<Societe> findBySecteur(String secteur) {
        List<Societe> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM societe WHERE secteur LIKE ? ORDER BY secteur, nom_societe";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, "%" + secteur + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Societe s = new Societe();
                s.setId(rs.getInt("id"));
                s.setNom_societe(rs.getString("nom_societe"));
                s.setSecteur(rs.getString("secteur"));
                s.setDescription(rs.getString("description"));
                s.setAdresse(rs.getString("adresse"));
                s.setSite_web(rs.getString("site_web"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    s.setUser(user);
                }
                
                list.add(s);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findBySecteur société : " + ex.getMessage());
        }
        return list;
    }

    public List<Societe> findByAdresse(String adresse) {
        List<Societe> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM societe WHERE adresse LIKE ? ORDER BY nom_societe";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, "%" + adresse + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Societe s = new Societe();
                s.setId(rs.getInt("id"));
                s.setNom_societe(rs.getString("nom_societe"));
                s.setSecteur(rs.getString("secteur"));
                s.setDescription(rs.getString("description"));
                s.setAdresse(rs.getString("adresse"));
                s.setSite_web(rs.getString("site_web"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    s.setUser(user);
                }
                
                list.add(s);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByAdresse société : " + ex.getMessage());
        }
        return list;
    }

    public boolean exists(int id) {


        String req = "SELECT COUNT(*) FROM societe WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur exists société : " + ex.getMessage());
        }
        return false;
    }

    public boolean existsByNom(String nom) {


        String req = "SELECT COUNT(*) FROM societe WHERE nom_societe=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur existsByNom société : " + ex.getMessage());
        }
        return false;
    }

    private User getUserById(int userId) {


        String req = "SELECT * FROM user WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                user.setPassword(rs.getString("password"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setTelephone(rs.getString("telephone"));
                user.setGouvernorat(rs.getString("gouvernorat"));
                user.setDate_naissance(rs.getTimestamp("date_naissance"));
                user.setRole_utilisateur(rs.getString("role_utilisateur"));
                return user;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur getUserById : " + ex.getMessage());
        }
        return null;
    }
}
