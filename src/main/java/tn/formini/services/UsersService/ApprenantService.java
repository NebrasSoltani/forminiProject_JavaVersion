package tn.formini.services.UsersService;

import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.User;
import tn.formini.entities.Users.Domaine;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApprenantService implements service<Apprenant> {

    Connection cnx;

    public ApprenantService(){
        cnx= MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Apprenant a) {

        String req = "INSERT INTO apprenant (genre, etat_civil, objectif, domaines_interet, user_id, domaine_id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, a.getGenre());
            ps.setString(2, a.getEtat_civil());
            ps.setString(3, a.getObjectif());
            ps.setString(4, a.getDomaines_interet());
            
            if (a.getUser() != null) {
                ps.setInt(5, a.getUser().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            
            if (a.getDomaine() != null) {
                ps.setInt(6, a.getDomaine().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            
            ps.executeUpdate();
            System.out.println("Apprenant ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter apprenant : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Apprenant a) {


        String req = "UPDATE apprenant SET genre=?, etat_civil=?, objectif=?, domaines_interet=?, user_id=?, domaine_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, a.getGenre());
            ps.setString(2, a.getEtat_civil());
            ps.setString(3, a.getObjectif());
            ps.setString(4, a.getDomaines_interet());
            
            if (a.getUser() != null) {
                ps.setInt(5, a.getUser().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            
            if (a.getDomaine() != null) {
                ps.setInt(6, a.getDomaine().getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            
            ps.setInt(7, a.getId());
            ps.executeUpdate();
            System.out.println("Apprenant modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier apprenant : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {

        
        String req = "DELETE FROM apprenant WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Apprenant supprimé avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer apprenant : " + ex.getMessage());
        }
    }

    @Override
    public List<Apprenant> afficher() {

        List<Apprenant> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM apprenant";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Apprenant a = new Apprenant();
                a.setId(rs.getInt("id"));
                a.setGenre(rs.getString("genre"));
                a.setEtat_civil(rs.getString("etat_civil"));
                a.setObjectif(rs.getString("objectif"));
                a.setDomaines_interet(rs.getString("domaines_interet"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    a.setUser(user);
                }
                
                int domaineId = rs.getInt("domaine_id");
                if (!rs.wasNull()) {
                    Domaine domaine = getDomaineById(domaineId);
                    a.setDomaine(domaine);
                }
                
                list.add(a);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher apprenant : " + ex.getMessage());
        }
        return list;
    }

    public Apprenant findById(int id) {
        if (cnx == null) return null;

        String req = "SELECT * FROM apprenant WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Apprenant a = new Apprenant();
                a.setId(rs.getInt("id"));
                a.setGenre(rs.getString("genre"));
                a.setEtat_civil(rs.getString("etat_civil"));
                a.setObjectif(rs.getString("objectif"));
                a.setDomaines_interet(rs.getString("domaines_interet"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    a.setUser(user);
                }
                
                int domaineId = rs.getInt("domaine_id");
                if (!rs.wasNull()) {
                    Domaine domaine = getDomaineById(domaineId);
                    a.setDomaine(domaine);
                }
                
                return a;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById apprenant : " + ex.getMessage());
        }
        return null;
    }

    public List<Apprenant> findByDomaine(int domaineId) {
        List<Apprenant> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM apprenant WHERE domaine_id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, domaineId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Apprenant a = new Apprenant();
                a.setId(rs.getInt("id"));
                a.setGenre(rs.getString("genre"));
                a.setEtat_civil(rs.getString("etat_civil"));
                a.setObjectif(rs.getString("objectif"));
                a.setDomaines_interet(rs.getString("domaines_interet"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    a.setUser(user);
                }
                
                Domaine domaine = getDomaineById(domaineId);
                a.setDomaine(domaine);
                
                list.add(a);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByDomaine apprenant : " + ex.getMessage());
        }
        return list;
    }

    public Apprenant findByUserId(int userId) {
        if (cnx == null || userId <= 0) {
            return null;
        }

        String req = "SELECT * FROM apprenant WHERE user_id = ? LIMIT 1";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Apprenant a = new Apprenant();
                a.setId(rs.getInt("id"));
                a.setGenre(rs.getString("genre"));
                a.setEtat_civil(rs.getString("etat_civil"));
                a.setObjectif(rs.getString("objectif"));
                a.setDomaines_interet(rs.getString("domaines_interet"));

                User user = getUserById(userId);
                a.setUser(user);

                int domaineId = rs.getInt("domaine_id");
                if (!rs.wasNull()) {
                    Domaine domaine = getDomaineById(domaineId);
                    a.setDomaine(domaine);
                }
                return a;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByUserId apprenant : " + ex.getMessage());
        }
        return null;
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

    private Domaine getDomaineById(int domaineId) {


        String req = "SELECT * FROM domaine WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, domaineId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Domaine domaine = new Domaine();
                domaine.setId(rs.getInt("id"));
                domaine.setNom(rs.getString("nom"));
                return domaine;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur getDomaineById : " + ex.getMessage());
        }
        return null;
    }
}
