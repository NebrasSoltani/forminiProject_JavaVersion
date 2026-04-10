package tn.formini.services;

import tn.formini.entities.Evenement;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements service<Evenement> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Evenement e) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "INSERT INTO evenement (titre, description, date_debut, date_fin, lieu, image, nombre_places, " +
                "is_actif, type, filieres, tags, image360, url_street_view, resume_auto, resume_generated_at, " +
                "live_summary_data, url_live, live, stream_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, e.getDate_debut() != null ? new Timestamp(e.getDate_debut().getTime()) : null);
            ps.setTimestamp(4, e.getDate_fin() != null ? new Timestamp(e.getDate_fin().getTime()) : null);
            ps.setString(5, e.getLieu());
            ps.setString(6, e.getImage());
            ps.setInt(7, e.getNombre_places());
            ps.setBoolean(8, e.isIs_actif());
            ps.setString(9, e.getType());
            ps.setString(10, e.getFilieres());
            ps.setString(11, e.getTags());
            ps.setString(12, e.getImage360());
            ps.setString(13, e.getUrl_street_view());
            ps.setString(14, e.getResume_auto());
            ps.setTimestamp(15, e.getResume_generated_at() != null ? new Timestamp(e.getResume_generated_at().getTime()) : null);
            ps.setString(16, e.getLive_summary_data());
            ps.setString(17, e.getUrl_live());
            ps.setBoolean(18, e.isLive());
            ps.setString(19, e.getStream_url());
            ps.executeUpdate();
            System.out.println("Événement ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Evenement e) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "UPDATE evenement SET titre=?, description=?, date_debut=?, date_fin=?, lieu=?, image=?, " +
                "nombre_places=?, is_actif=?, type=?, filieres=?, tags=?, image360=?, url_street_view=?, " +
                "resume_auto=?, resume_generated_at=?, live_summary_data=?, url_live=?, live=?, stream_url=? " +
                "WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, e.getDate_debut() != null ? new Timestamp(e.getDate_debut().getTime()) : null);
            ps.setTimestamp(4, e.getDate_fin() != null ? new Timestamp(e.getDate_fin().getTime()) : null);
            ps.setString(5, e.getLieu());
            ps.setString(6, e.getImage());
            ps.setInt(7, e.getNombre_places());
            ps.setBoolean(8, e.isIs_actif());
            ps.setString(9, e.getType());
            ps.setString(10, e.getFilieres());
            ps.setString(11, e.getTags());
            ps.setString(12, e.getImage360());
            ps.setString(13, e.getUrl_street_view());
            ps.setString(14, e.getResume_auto());
            ps.setTimestamp(15, e.getResume_generated_at() != null ? new Timestamp(e.getResume_generated_at().getTime()) : null);
            ps.setString(16, e.getLive_summary_data());
            ps.setString(17, e.getUrl_live());
            ps.setBoolean(18, e.isLive());
            ps.setString(19, e.getStream_url());
            ps.setInt(20, e.getId());
            ps.executeUpdate();
            System.out.println("Événement modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        Connection cnx = getCnx();
        if (cnx == null) return;

        String req = "DELETE FROM evenement WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Événement supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimer : " + e.getMessage());
        }
    }

    @Override
    public List<Evenement> afficher() {
        Connection cnx = getCnx();
        List<Evenement> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM evenement";
        try {
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setDate_debut(rs.getTimestamp("date_debut"));
                e.setDate_fin(rs.getTimestamp("date_fin"));
                e.setLieu(rs.getString("lieu"));
                e.setImage(rs.getString("image"));
                e.setNombre_places(rs.getInt("nombre_places"));
                e.setIs_actif(rs.getBoolean("is_actif"));
                e.setType(rs.getString("type"));
                e.setFilieres(rs.getString("filieres"));
                e.setTags(rs.getString("tags"));
                e.setImage360(rs.getString("image360"));
                e.setUrl_street_view(rs.getString("url_street_view"));
                e.setResume_auto(rs.getString("resume_auto"));
                e.setResume_generated_at(rs.getTimestamp("resume_generated_at"));
                e.setLive_summary_data(rs.getString("live_summary_data"));
                e.setUrl_live(rs.getString("url_live"));
                e.setLive(rs.getBoolean("live"));
                e.setStream_url(rs.getString("stream_url"));
                list.add(e);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher : " + ex.getMessage());
        }
        return list;
    }
}