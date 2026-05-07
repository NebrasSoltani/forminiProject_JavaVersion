package tn.formini.services.formations;

import tn.formini.entities.Users.User;
import tn.formini.entities.formations.Formation;
import tn.formini.services.service;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormationService implements service<Formation> {

    private final Connection cnx;
    private String lastDeleteError;

    public FormationService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Formation formation) {
        if (cnx == null) return;

        String req = "INSERT INTO formation (titre, categorie, niveau, langue, description_courte, description_detaillee, " +
                "objectifs_pedagogiques, prerequis, programme, duree, nombre_lecons, format, date_debut, planning, lien_live, " +
                "nombre_seances, type_acces, prix, type_achat, prix_promo, date_fin_promo, image_couverture, video_promo, " +
                "statut, date_creation, date_publication, certificat, has_quiz, fichiers_telechargeables, forum, formateur_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            mapFormationToStatement(ps, formation, false);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    formation.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter formation : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Formation formation) {
        if (cnx == null) return;

        String req = "UPDATE formation SET titre=?, categorie=?, niveau=?, langue=?, description_courte=?, description_detaillee=?, " +
                "objectifs_pedagogiques=?, prerequis=?, programme=?, duree=?, nombre_lecons=?, format=?, date_debut=?, planning=?, " +
                "lien_live=?, nombre_seances=?, type_acces=?, prix=?, type_achat=?, prix_promo=?, date_fin_promo=?, image_couverture=?, " +
                "video_promo=?, statut=?, date_creation=?, date_publication=?, certificat=?, has_quiz=?, fichiers_telechargeables=?, forum=?, " +
                "formateur_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            mapFormationToStatement(ps, formation, true);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Erreur modifier formation : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        deleteById(id);
    }

    public boolean deleteById(int id) {
        if (cnx == null) {
            lastDeleteError = "Connexion base de donnees indisponible.";
            return false;
        }
        lastDeleteError = null;

        boolean previousAutoCommit;
        try {
            previousAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);
        } catch (SQLException ex) {
            lastDeleteError = ex.getMessage();
            System.out.println("Erreur supprimer formation : " + ex.getMessage());
            return false;
        }

        try {
            executeDelete("DELETE FROM progression_lecon WHERE lecon_id IN (SELECT id FROM lecon WHERE formation_id=?)", id);
            executeDelete("DELETE FROM inscription WHERE formation_id=?", id);
            executeDelete("DELETE FROM reponse WHERE question_id IN (SELECT q.id FROM question q JOIN quiz z ON z.id=q.quiz_id WHERE z.formation_id=?)", id);
            executeDelete("DELETE FROM question WHERE quiz_id IN (SELECT id FROM quiz WHERE formation_id=?)", id);
            executeDelete("DELETE FROM resultat_quiz WHERE quiz_id IN (SELECT id FROM quiz WHERE formation_id=?)", id);
            executeDelete("DELETE FROM quiz WHERE formation_id=?", id);
            executeDelete("DELETE FROM lecon WHERE formation_id=?", id);

            int deletedFormation = executeDelete("DELETE FROM formation WHERE id=?", id);
            if (deletedFormation == 0) {
                cnx.rollback();
                lastDeleteError = "Formation introuvable ou deja supprimee.";
                return false;
            }

            cnx.commit();
            return true;
        } catch (SQLException ex) {
            try {
                cnx.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Erreur rollback suppression formation : " + rollbackEx.getMessage());
            }
            lastDeleteError = ex.getMessage();
            System.out.println("Erreur supprimer formation : " + ex.getMessage());
            return false;
        } finally {
            try {
                cnx.setAutoCommit(previousAutoCommit);
            } catch (SQLException ex) {
                System.out.println("Erreur restauration auto-commit formation : " + ex.getMessage());
            }
        }
    }

    public String getLastDeleteError() {
        return lastDeleteError;
    }

    private int executeDelete(String sql, int id) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    @Override
    public List<Formation> afficher() {
        List<Formation> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM formation ORDER BY id DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSetToFormation(rs));
            }
        } catch (SQLException ex) {
            System.out.println("Erreur afficher formation : " + ex.getMessage());
        }

        return list;
    }

    public List<Formation> findByFormateurId(int formateurId) {
        List<Formation> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM formation WHERE formateur_id=? ORDER BY id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, formateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFormation(rs));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findByFormateurId formation : " + ex.getMessage());
        }

        return list;
    }

    public List<Formation> findPublished() {
        List<Formation> list = new ArrayList<>();
        if (cnx == null) return list;

        String req = "SELECT * FROM formation WHERE LOWER(statut)='publie' ORDER BY date_publication DESC, id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToFormation(rs));
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findPublished formation : " + ex.getMessage());
        }
        return list;
    }

    public Formation findById(int id) {
        if (cnx == null) return null;

        String req = "SELECT * FROM formation WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFormation(rs);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erreur findById formation : " + ex.getMessage());
        }

        return null;
    }

    public int countByFormateurId(int formateurId) {
        if (cnx == null) return 0;

        String req = "SELECT COUNT(*) FROM formation WHERE formateur_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, formateurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur countByFormateurId : " + ex.getMessage());
        }

        return 0;
    }

    private void mapFormationToStatement(PreparedStatement ps, Formation f, boolean includeIdAtEnd) throws SQLException {
        int i = 1;
        ps.setString(i++, f.getTitre());
        ps.setString(i++, f.getCategorie());
        ps.setString(i++, f.getNiveau());
        ps.setString(i++, f.getLangue());
        ps.setString(i++, f.getDescription_courte());
        ps.setString(i++, f.getDescription_detaillee());
        ps.setString(i++, f.getObjectifs_pedagogiques());
        ps.setString(i++, f.getPrerequis());
        ps.setString(i++, f.getProgramme());
        ps.setInt(i++, f.getDuree());
        ps.setInt(i++, f.getNombre_lecons());
        ps.setString(i++, f.getFormat());
        setTimestampOrNull(ps, i++, f.getDate_debut());
        ps.setString(i++, f.getPlanning());
        ps.setString(i++, f.getLien_live());

        if (f.getNombre_seances() != null) {
            ps.setInt(i++, f.getNombre_seances());
        } else {
            ps.setNull(i++, Types.INTEGER);
        }

        ps.setString(i++, f.getType_acces());
        ps.setBigDecimal(i++, f.getPrix());
        ps.setString(i++, f.getType_achat());
        ps.setBigDecimal(i++, f.getPrix_promo());
        setTimestampOrNull(ps, i++, f.getDate_fin_promo());
        ps.setString(i++, f.getImage_couverture());
        ps.setString(i++, f.getVideo_promo());
        ps.setString(i++, f.getStatut());
        setTimestampOrNull(ps, i++, f.getDate_creation());
        setTimestampOrNull(ps, i++, f.getDate_publication());
        ps.setBoolean(i++, f.isCertificat());
        ps.setBoolean(i++, f.isHas_quiz());
        ps.setBoolean(i++, f.isFichiers_telechargeables());
        ps.setBoolean(i++, f.isForum());

        int formateurId = (f.getFormateur() != null) ? f.getFormateur().getId() : 0;
        ps.setInt(i++, formateurId);

        if (includeIdAtEnd) {
            ps.setInt(i, f.getId());
        }
    }

    private Formation mapResultSetToFormation(ResultSet rs) throws SQLException {
        Formation f = new Formation();
        f.setId(rs.getInt("id"));
        f.setTitre(rs.getString("titre"));
        f.setCategorie(rs.getString("categorie"));
        f.setNiveau(rs.getString("niveau"));
        f.setLangue(rs.getString("langue"));
        f.setDescription_courte(rs.getString("description_courte"));
        f.setDescription_detaillee(rs.getString("description_detaillee"));
        f.setObjectifs_pedagogiques(rs.getString("objectifs_pedagogiques"));
        f.setPrerequis(rs.getString("prerequis"));
        f.setProgramme(rs.getString("programme"));
        f.setDuree(rs.getInt("duree"));
        f.setNombre_lecons(rs.getInt("nombre_lecons"));
        f.setFormat(rs.getString("format"));
        f.setDate_debut(rs.getTimestamp("date_debut"));
        f.setPlanning(rs.getString("planning"));
        f.setLien_live(rs.getString("lien_live"));

        int nombreSeances = rs.getInt("nombre_seances");
        if (!rs.wasNull()) {
            f.setNombre_seances(nombreSeances);
        }

        f.setType_acces(rs.getString("type_acces"));
        f.setPrix(rs.getBigDecimal("prix"));
        f.setType_achat(rs.getString("type_achat"));
        f.setPrix_promo(rs.getBigDecimal("prix_promo"));
        f.setDate_fin_promo(rs.getTimestamp("date_fin_promo"));
        f.setImage_couverture(rs.getString("image_couverture"));
        f.setVideo_promo(rs.getString("video_promo"));
        f.setStatut(rs.getString("statut"));
        f.setDate_creation(rs.getTimestamp("date_creation"));
        f.setDate_publication(rs.getTimestamp("date_publication"));
        f.setCertificat(rs.getBoolean("certificat"));
        f.setHas_quiz(rs.getBoolean("has_quiz"));
        f.setFichiers_telechargeables(rs.getBoolean("fichiers_telechargeables"));
        f.setForum(rs.getBoolean("forum"));

        int formateurId = rs.getInt("formateur_id");
        if (!rs.wasNull()) {
            User formateur = new User();
            formateur.setId(formateurId);
            f.setFormateur(formateur);
        }

        return f;
    }

    private void setTimestampOrNull(PreparedStatement ps, int index, Date date) throws SQLException {
        if (date != null) {
            ps.setTimestamp(index, new Timestamp(date.getTime()));
        } else {
            ps.setNull(index, Types.TIMESTAMP);
        }
    }
}

