package tn.formini.services.evenementsService;

import tn.formini.entities.evenements.Evenement;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service JDBC dédié à la page Home pour les opérations de lecture des événements.
 * Toutes les erreurs sont silencieuses (pas de crash).
 */
public class HomeEvenementService {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    /**
     * Mappe un ResultSet sur un objet Evenement.
     */
    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setId(rs.getInt("id"));
        e.setTitre(rs.getString("titre"));
        e.setDescription(rs.getString("description"));
        e.setDate_debut(rs.getTimestamp("date_debut"));
        e.setDate_fin(rs.getTimestamp("date_fin"));
        e.setLieu(rs.getString("lieu"));
        e.setImage(rs.getString("image"));
        int nb = rs.getInt("nombre_places");
        e.setNombre_places(rs.wasNull() ? null : nb);
        e.setIs_actif(rs.getBoolean("is_actif"));
        e.setType(rs.getString("type"));
        e.setFilieres(rs.getString("filieres"));
        e.setTags(rs.getString("tags"));
        e.setImage360(rs.getString("image360"));
        e.setUrl_street_view(rs.getString("url_street_view"));
        e.setResume_auto(rs.getString("resume_auto"));
        e.setLive_summary_data(rs.getString("live_summary_data"));
        e.setUrl_live(rs.getString("url_live"));
        e.setLive(rs.getBoolean("live"));
        e.setStream_url(rs.getString("stream_url"));
        return e;
    }

    /**
     * Retourne tous les événements actifs (is_actif = 1), triés par date de début.
     */
    public List<Evenement> getAllActifs() {
        List<Evenement> list = new ArrayList<>();
        Connection cnx = getCnx();
        if (cnx == null) return list;
        String req = "SELECT * FROM evenement WHERE is_actif = 1 ORDER BY date_debut ASC";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            System.out.println("[HomeEvenementService] getAllActifs : " + ex.getMessage());
        }
        return list;
    }

    /**
     * Retourne les événements actifs filtrés par type.
     *
     * @param type le type à filtrer (ex. "conference")
     */
    public List<Evenement> getByType(String type) {
        List<Evenement> list = new ArrayList<>();
        if (type == null || type.isBlank()) return getAllActifs();
        Connection cnx = getCnx();
        if (cnx == null) return list;
        String req = "SELECT * FROM evenement WHERE is_actif = 1 AND type = ? ORDER BY date_debut ASC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            System.out.println("[HomeEvenementService] getByType : " + ex.getMessage());
        }
        return list;
    }

    /**
     * Retourne la liste distincte des types d'événements actifs.
     */
    public List<String> getAllTypes() {
        List<String> types = new ArrayList<>();
        Connection cnx = getCnx();
        if (cnx == null) return types;
        String req = "SELECT DISTINCT type FROM evenement WHERE is_actif = 1 AND type IS NOT NULL ORDER BY type";
        try (Statement ste = cnx.createStatement();
             ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                String t = rs.getString("type");
                if (t != null && !t.isBlank()) types.add(t);
            }
        } catch (SQLException ex) {
            System.out.println("[HomeEvenementService] getAllTypes : " + ex.getMessage());
        }
        return types;
    }
}
