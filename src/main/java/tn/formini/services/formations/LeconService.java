package tn.formini.services.formations;

import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Lecon;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeconService implements service<Lecon> {

    private final Connection cnx;

    public LeconService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Lecon lecon) {
        if (cnx == null) return;

        String req = "INSERT INTO lecon (titre, description, contenu, ordre, duree, video_url, fichier, gratuit, formation_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            mapLeconToStatement(ps, lecon, false);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    lecon.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter lecon : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Lecon lecon) {
        if (cnx == null) return;

        String req = "UPDATE lecon SET titre=?, description=?, contenu=?, ordre=?, duree=?, video_url=?, fichier=?, gratuit=?, formation_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            mapLeconToStatement(ps, lecon, true);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Erreur modifier lecon : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        if (cnx == null) return;

        String req = "DELETE FROM lecon WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer lecon : " + ex.getMessage());
        }
    }

    @Override
    public List<Lecon> afficher() {
        List<Lecon> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM lecon ORDER BY formation_id ASC, ordre ASC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSetToLecon(rs));
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher lecon : " + ex.getMessage());
        }

        return list;
    }

    public List<Lecon> findByFormationId(int formationId) {
        List<Lecon> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM lecon WHERE formation_id=? ORDER BY ordre ASC, id ASC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLecon(rs));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByFormationId lecon : " + ex.getMessage());
        }

        return list;
    }

    public int countByFormationId(int formationId) {
        if (cnx == null) return 0;

        String req = "SELECT COUNT(*) FROM lecon WHERE formation_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur countByFormationId lecon : " + ex.getMessage());
        }

        return 0;
    }

    private void mapLeconToStatement(PreparedStatement ps, Lecon l, boolean includeIdAtEnd) throws SQLException {
        int i = 1;
        ps.setString(i++, l.getTitre());
        ps.setString(i++, l.getDescription());
        ps.setString(i++, l.getContenu());
        ps.setInt(i++, l.getOrdre());

        if (l.getDuree() != null) {
            ps.setInt(i++, l.getDuree());
        } else {
            ps.setNull(i++, Types.INTEGER);
        }

        ps.setString(i++, l.getVideo_url());
        ps.setString(i++, l.getFichier());
        ps.setBoolean(i++, l.isGratuit());

        int formationId = (l.getFormation() != null) ? l.getFormation().getId() : 0;
        ps.setInt(i++, formationId);

        if (includeIdAtEnd) {
            ps.setInt(i, l.getId());
        }
    }

    private Lecon mapResultSetToLecon(ResultSet rs) throws SQLException {
        Lecon l = new Lecon();
        l.setId(rs.getInt("id"));
        l.setTitre(rs.getString("titre"));
        l.setDescription(rs.getString("description"));
        l.setContenu(rs.getString("contenu"));
        l.setOrdre(rs.getInt("ordre"));

        int duree = rs.getInt("duree");
        if (!rs.wasNull()) {
            l.setDuree(duree);
        }

        l.setVideo_url(rs.getString("video_url"));
        l.setFichier(rs.getString("fichier"));
        l.setGratuit(rs.getBoolean("gratuit"));

        int formationId = rs.getInt("formation_id");
        if (!rs.wasNull()) {
            Formation formation = new Formation();
            formation.setId(formationId);
            l.setFormation(formation);
        }

        return l;
    }
}

