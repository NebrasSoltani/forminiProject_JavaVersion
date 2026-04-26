package tn.formini.services.evenementsService;

import tn.formini.entities.evenements.Blog;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service JDBC dédié à la page Home pour les opérations de lecture des blogs.
 * Toutes les erreurs sont silencieuses (pas de crash).
 */
public class HomeBlogService {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    /**
     * Mappe un ResultSet sur un objet Blog.
     */
    private Blog mapRow(ResultSet rs) throws SQLException {
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
        b.setEvenement_id(rs.wasNull() ? null : evtId);
        b.setTags(rs.getString("tags"));
        return b;
    }

    /**
     * Retourne tous les blogs publiés (is_publie = 1), triés par date décroissante.
     */
    public List<Blog> getAllPublished() {
        List<Blog> list = new ArrayList<>();
        Connection cnx = getCnx();
        if (cnx == null) return list;
        String req = "SELECT * FROM blog WHERE is_publie = 1 ORDER BY date_publication DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("[HomeBlogService] getAllPublished : " + e.getMessage());
        }
        return list;
    }

    /**
     * Retourne les blogs publiés filtrés par catégorie.
     *
     * @param categorie la catégorie à filtrer (ex. "Technologie")
     */
    public List<Blog> getByCategorie(String categorie) {
        List<Blog> list = new ArrayList<>();
        if (categorie == null || categorie.isBlank()) return getAllPublished();
        Connection cnx = getCnx();
        if (cnx == null) return list;
        String req = "SELECT * FROM blog WHERE is_publie = 1 AND categorie = ? ORDER BY date_publication DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, categorie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("[HomeBlogService] getByCategorie : " + e.getMessage());
        }
        return list;
    }

    /**
     * Retourne la liste distincte des catégories de blogs publiés.
     */
    public List<String> getAllCategories() {
        List<String> cats = new ArrayList<>();
        Connection cnx = getCnx();
        if (cnx == null) return cats;
        String req = "SELECT DISTINCT categorie FROM blog WHERE is_publie = 1 AND categorie IS NOT NULL ORDER BY categorie";
        try (Statement ste = cnx.createStatement();
             ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                String c = rs.getString("categorie");
                if (c != null && !c.isBlank()) cats.add(c);
            }
        } catch (SQLException e) {
            System.out.println("[HomeBlogService] getAllCategories : " + e.getMessage());
        }
        return cats;
    }
}
