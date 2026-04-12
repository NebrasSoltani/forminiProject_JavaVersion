package tn.formini.services.UsersService;

import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormateurService implements service<Formateur> {

    Connection cnx;

    public FormateurService() {
        cnx= MyDataBase.getInstance().getCnx();
    }


    @Override
    public void ajouter(Formateur f) {


        String req = "INSERT INTO formateur (specialite, bio, experience_annees, linkedin, portfolio, cv, note_moyenne, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, f.getSpecialite());
            ps.setString(2, f.getBio());
            
            if (f.getExperience_annees() != null) {
                ps.setInt(3, f.getExperience_annees());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            ps.setString(4, f.getLinkedin());
            ps.setString(5, f.getPortfolio());
            ps.setString(6, f.getCv());
            
            if (f.getNote_moyenne() != null) {
                ps.setDouble(7, f.getNote_moyenne());
            } else {
                ps.setNull(7, Types.DOUBLE);
            }
            
            if (f.getUser() != null) {
                ps.setInt(8, f.getUser().getId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                f.setId(rs.getInt(1));
                System.out.println("Formateur ajouté avec succès ! ID: " + f.getId());
            } else {
                System.out.println("Erreur: Aucun ID généré pour le formateur");
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter formateur : " + ex.getMessage());
            f.setId(0);
        }
    }

    @Override
    public void modifier(Formateur f) {


        String req = "UPDATE formateur SET specialite=?, bio=?, experience_annees=?, linkedin=?, portfolio=?, cv=?, note_moyenne=?, user_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, f.getSpecialite());
            ps.setString(2, f.getBio());
            
            if (f.getExperience_annees() != null) {
                ps.setInt(3, f.getExperience_annees());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            
            ps.setString(4, f.getLinkedin());
            ps.setString(5, f.getPortfolio());
            ps.setString(6, f.getCv());
            
            if (f.getNote_moyenne() != null) {
                ps.setDouble(7, f.getNote_moyenne());
            } else {
                ps.setNull(7, Types.DOUBLE);
            }
            
            if (f.getUser() != null) {
                ps.setInt(8, f.getUser().getId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.setInt(9, f.getId());
            ps.executeUpdate();
            System.out.println("Formateur modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier formateur : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {

        
        String req = "DELETE FROM formateur WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Formateur supprimé avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer formateur : " + ex.getMessage());
        }
    }

    @Override
    public List<Formateur> afficher() {
        List<Formateur> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM formateur ORDER BY specialite";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Formateur f = new Formateur();
                f.setId(rs.getInt("id"));
                f.setSpecialite(rs.getString("specialite"));
                f.setBio(rs.getString("bio"));
                
                int experience = rs.getInt("experience_annees");
                if (!rs.wasNull()) {
                    f.setExperience_annees(experience);
                }
                
                f.setLinkedin(rs.getString("linkedin"));
                f.setPortfolio(rs.getString("portfolio"));
                f.setCv(rs.getString("cv"));
                
                double note = rs.getDouble("note_moyenne");
                if (!rs.wasNull()) {
                    f.setNote_moyenne(note);
                }
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    f.setUser(user);
                }
                
                list.add(f);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher formateur : " + ex.getMessage());
        }
        return list;
    }

    public Formateur findById(int id) {


        String req = "SELECT * FROM formateur WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Formateur f = new Formateur();
                f.setId(rs.getInt("id"));
                f.setSpecialite(rs.getString("specialite"));
                f.setBio(rs.getString("bio"));
                
                int experience = rs.getInt("experience_annees");
                if (!rs.wasNull()) {
                    f.setExperience_annees(experience);
                }
                
                f.setLinkedin(rs.getString("linkedin"));
                f.setPortfolio(rs.getString("portfolio"));
                f.setCv(rs.getString("cv"));
                
                double note = rs.getDouble("note_moyenne");
                if (!rs.wasNull()) {
                    f.setNote_moyenne(note);
                }
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    f.setUser(user);
                }
                
                return f;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById formateur : " + ex.getMessage());
        }
        return null;
    }

    public List<Formateur> findBySpecialite(String specialite) {
        List<Formateur> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM formateur WHERE specialite LIKE ? ORDER BY specialite";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, "%" + specialite + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Formateur f = new Formateur();
                f.setId(rs.getInt("id"));
                f.setSpecialite(rs.getString("specialite"));
                f.setBio(rs.getString("bio"));
                
                int experience = rs.getInt("experience_annees");
                if (!rs.wasNull()) {
                    f.setExperience_annees(experience);
                }
                
                f.setLinkedin(rs.getString("linkedin"));
                f.setPortfolio(rs.getString("portfolio"));
                f.setCv(rs.getString("cv"));
                
                double note = rs.getDouble("note_moyenne");
                if (!rs.wasNull()) {
                    f.setNote_moyenne(note);
                }
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    f.setUser(user);
                }
                
                list.add(f);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findBySpecialite formateur : " + ex.getMessage());
        }
        return list;
    }

    public List<Formateur> findByExperienceMin(int minExperience) {
        List<Formateur> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM formateur WHERE experience_annees >= ? ORDER BY experience_annees DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, minExperience);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Formateur f = new Formateur();
                f.setId(rs.getInt("id"));
                f.setSpecialite(rs.getString("specialite"));
                f.setBio(rs.getString("bio"));
                
                int experience = rs.getInt("experience_annees");
                if (!rs.wasNull()) {
                    f.setExperience_annees(experience);
                }
                
                f.setLinkedin(rs.getString("linkedin"));
                f.setPortfolio(rs.getString("portfolio"));
                f.setCv(rs.getString("cv"));
                
                double note = rs.getDouble("note_moyenne");
                if (!rs.wasNull()) {
                    f.setNote_moyenne(note);
                }
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    f.setUser(user);
                }
                
                list.add(f);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByExperienceMin formateur : " + ex.getMessage());
        }
        return list;
    }

    public List<Formateur> findByNoteMin(double minNote) {
        List<Formateur> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM formateur WHERE note_moyenne >= ? ORDER BY note_moyenne DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setDouble(1, minNote);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Formateur f = new Formateur();
                f.setId(rs.getInt("id"));
                f.setSpecialite(rs.getString("specialite"));
                f.setBio(rs.getString("bio"));
                
                int experience = rs.getInt("experience_annees");
                if (!rs.wasNull()) {
                    f.setExperience_annees(experience);
                }
                
                f.setLinkedin(rs.getString("linkedin"));
                f.setPortfolio(rs.getString("portfolio"));
                f.setCv(rs.getString("cv"));
                
                double note = rs.getDouble("note_moyenne");
                if (!rs.wasNull()) {
                    f.setNote_moyenne(note);
                }
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    User user = getUserById(userId);
                    f.setUser(user);
                }
                
                list.add(f);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByNoteMin formateur : " + ex.getMessage());
        }
        return list;
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
