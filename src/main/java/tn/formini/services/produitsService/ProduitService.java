package tn.formini.services.produitsService;

import tn.formini.entities.produits.Produit;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements service<Produit> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Produit p) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO produit (nom, categorie, description, prix, stock, image, statut, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getNom());
            ps.setString(2, p.getCategorie());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrix());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImage());
            ps.setString(7, p.getStatut());
            ps.setTimestamp(8, new Timestamp(p.getDate_creation().getTime()));
            ps.executeUpdate();
            System.out.println("Produit ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Produit p) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE produit SET nom=?, categorie=?, description=?, prix=?, stock=?, image=?, statut=?, date_creation=? " +
                "WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getNom());
            ps.setString(2, p.getCategorie());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrix());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImage());
            ps.setString(7, p.getStatut());
            ps.setTimestamp(8, new Timestamp(p.getDate_creation().getTime()));
            ps.setInt(9, p.getId());
            ps.executeUpdate();
            System.out.println("Produit modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "DELETE FROM produit WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Produit supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimer : " + e.getMessage());
        }
    }

    @Override
    public List<Produit> afficher() {
        Connection cnx = getCnx();
        List<Produit> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM produit ORDER BY id DESC";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setCategorie(rs.getString("categorie"));
                p.setDescription(rs.getString("description"));
                p.setPrix(rs.getBigDecimal("prix"));
                p.setStock(rs.getInt("stock"));
                p.setImage(rs.getString("image"));
                p.setStatut(rs.getString("statut"));
                p.setDate_creation(rs.getTimestamp("date_creation"));
                list.add(p);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher : " + ex.getMessage());
        }
        System.out.println("Service: " + list.size() + " produits récupérés du DB");
        return list;
    }

    // Additional methods for specific operations
    public Produit getById(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return null;

        String req = "SELECT * FROM produit WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setCategorie(rs.getString("categorie"));
                p.setDescription(rs.getString("description"));
                p.setPrix(rs.getBigDecimal("prix"));
                p.setStock(rs.getInt("stock"));
                p.setImage(rs.getString("image"));
                p.setStatut(rs.getString("statut"));
                p.setDate_creation(rs.getTimestamp("date_creation"));
                return p;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur getById : " + ex.getMessage());
        }
        return null;
    }

    public List<Produit> getByCategorie(String categorie) {
        Connection cnx = getCnx();
        List<Produit> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM produit WHERE categorie=? ORDER BY nom";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, categorie);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setCategorie(rs.getString("categorie"));
                p.setDescription(rs.getString("description"));
                p.setPrix(rs.getBigDecimal("prix"));
                p.setStock(rs.getInt("stock"));
                p.setImage(rs.getString("image"));
                p.setStatut(rs.getString("statut"));
                p.setDate_creation(rs.getTimestamp("date_creation"));
                list.add(p);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur getByCategorie : " + ex.getMessage());
        }
        return list;
    }

    public void mettreAJourStock(int id, int nouvelleQuantite) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE produit SET stock=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, nouvelleQuantite);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("Stock mis à jour avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur mise à jour stock : " + ex.getMessage());
        }
    }
}
