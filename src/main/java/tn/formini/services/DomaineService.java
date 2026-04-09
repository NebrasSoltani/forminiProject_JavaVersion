package tn.formini.services;

import tn.formini.entities.Domaine;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DomaineService implements service<Domaine> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Domaine d) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO domaine (nom) VALUES (?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, d.getNom());
            ps.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                d.setId(rs.getInt(1));
                System.out.println("Domaine ajouté avec succès ! ID: " + d.getId());
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter domaine : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Domaine d) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE domaine SET nom=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, d.getNom());
            ps.setInt(2, d.getId());
            ps.executeUpdate();
            System.out.println("Domaine modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier domaine : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;
        
        String req = "DELETE FROM domaine WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Domaine supprimé avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer domaine : " + ex.getMessage());
        }
    }

    @Override
    public List<Domaine> afficher() {
        Connection cnx = getCnx();
        List<Domaine> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM domaine ORDER BY nom";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Domaine d = new Domaine();
                d.setId(rs.getInt("id"));
                d.setNom(rs.getString("nom"));
                list.add(d);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher domaine : " + ex.getMessage());
        }
        return list;
    }

    public Domaine findById(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return null;

        String req = "SELECT * FROM domaine WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Domaine d = new Domaine();
                d.setId(rs.getInt("id"));
                d.setNom(rs.getString("nom"));
                return d;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById domaine : " + ex.getMessage());
        }
        return null;
    }

    public Domaine findByNom(String nom) {
        Connection cnx = getCnx();
        if (cnx == null) return null;

        String req = "SELECT * FROM domaine WHERE nom=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Domaine d = new Domaine();
                d.setId(rs.getInt("id"));
                d.setNom(rs.getString("nom"));
                return d;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByNom domaine : " + ex.getMessage());
        }
        return null;
    }

    public List<Domaine> searchByNom(String searchTerm) {
        Connection cnx = getCnx();
        List<Domaine> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM domaine WHERE nom LIKE ? ORDER BY nom";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, "%" + searchTerm + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Domaine d = new Domaine();
                d.setId(rs.getInt("id"));
                d.setNom(rs.getString("nom"));
                list.add(d);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur searchByNom domaine : " + ex.getMessage());
        }
        return list;
    }

    public boolean exists(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return false;

        String req = "SELECT COUNT(*) FROM domaine WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur exists domaine : " + ex.getMessage());
        }
        return false;
    }

    public boolean existsByNom(String nom) {
        Connection cnx = getCnx();
        if (cnx == null) return false;

        String req = "SELECT COUNT(*) FROM domaine WHERE nom=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur existsByNom domaine : " + ex.getMessage());
        }
        return false;
    }
}
