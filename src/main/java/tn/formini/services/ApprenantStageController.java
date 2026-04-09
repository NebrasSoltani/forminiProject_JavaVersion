package tn.formini.services;

import tn.formini.entities.OffreStage;
import tn.formini.entities.User;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.*;

public class OffreStageService implements service<OffreStage> {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(OffreStage o) {
        String req = """
            INSERT INTO offre_stage 
            (titre, description, entreprise, domaine, competences_requises, profil_demande,
             duree, date_debut, date_fin, type_stage, lieu, remuneration,
             contact_email, contact_tel, statut, societe_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setString(1,  o.getTitre());
            ps.setString(2,  o.getDescription());
            ps.setString(3,  o.getEntreprise());
            ps.setString(4,  o.getDomaine());
            ps.setString(5,  o.getCompetencesRequises());
            ps.setString(6,  o.getProfilDemande());
            ps.setInt(7,     o.getDuree());
            ps.setDate(8,    o.getDateDebut() != null ? new Date(o.getDateDebut().getTime()) : null);
            ps.setDate(9,    o.getDateFin()   != null ? new Date(o.getDateFin().getTime())   : null);
            ps.setString(10, o.getTypeStage());
            ps.setString(11, o.getLieu());
            ps.setDouble(12, o.getRemuneration());
            ps.setString(13, o.getContactEmail());
            ps.setString(14, o.getContactTel());
            ps.setString(15, o.getStatut());
            ps.setInt(16,    o.getSocieteId());
            ps.executeUpdate();
            System.out.println("Offre ajoutée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur ajouter offre : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(OffreStage o) {
        String req = """
            UPDATE offre_stage SET
              titre=?, description=?, entreprise=?, domaine=?, competences_requises=?,
              profil_demande=?, duree=?, date_debut=?, date_fin=?, type_stage=?,
              lieu=?, remuneration=?, contact_email=?, contact_tel=?, statut=?
            WHERE id=? AND societe_id=?
            """;
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setString(1,  o.getTitre());
            ps.setString(2,  o.getDescription());
            ps.setString(3,  o.getEntreprise());
            ps.setString(4,  o.getDomaine());
            ps.setString(5,  o.getCompetencesRequises());
            ps.setString(6,  o.getProfilDemande());
            ps.setInt(7,     o.getDuree());
            ps.setDate(8,    o.getDateDebut() != null ? new Date(o.getDateDebut().getTime()) : null);
            ps.setDate(9,    o.getDateFin()   != null ? new Date(o.getDateFin().getTime())   : null);
            ps.setString(10, o.getTypeStage());
            ps.setString(11, o.getLieu());
            ps.setDouble(12, o.getRemuneration());
            ps.setString(13, o.getContactEmail());
            ps.setString(14, o.getContactTel());
            ps.setString(15, o.getStatut());
            ps.setInt(16,    o.getId());
            ps.setInt(17,    o.getSocieteId());
            ps.executeUpdate();
            System.out.println("Offre modifiée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur modifier offre : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM offre_stage WHERE id=?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Offre supprimée avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur supprimer offre : " + ex.getMessage());
        }
    }

    @Override
    public List<OffreStage> afficher() {
        return afficherParSociete(-1); // toutes les offres
    }

    // ---------------------------------------------------------------
    // Méthodes supplémentaires (équivalent du contrôleur Spring)
    // ---------------------------------------------------------------

    /** Liste paginée et filtrée des offres d'une société */
    public Map<String, Object> rechercherParSocietePagine(
            int societeId,
            Map<String, String> filters,
            int page, int limit) {

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE societe_id=?");
        params.add(societeId);

        String titre     = filters.getOrDefault("titre",     "");
        String typeStage = filters.getOrDefault("typeStage", "");
        String statut    = filters.getOrDefault("statut",    "");
        String domaine   = filters.getOrDefault("domaine",   "");
        String lieu      = filters.getOrDefault("lieu",      "");

        if (!titre.isBlank())     { where.append(" AND titre LIKE ?");      params.add("%" + titre     + "%"); }
        if (!typeStage.isBlank()) { where.append(" AND type_stage = ?");    params.add(typeStage); }
        if (!statut.isBlank())    { where.append(" AND statut = ?");        params.add(statut); }
        if (!domaine.isBlank())   { where.append(" AND domaine LIKE ?");    params.add("%" + domaine   + "%"); }
        if (!lieu.isBlank())      { where.append(" AND lieu LIKE ?");       params.add("%" + lieu      + "%"); }

        // Compte total
        long total = 0;
        String countSql = "SELECT COUNT(*) FROM offre_stage " + where;
        try (PreparedStatement ps = getCnx().prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getLong(1);
        } catch (SQLException ex) {
            System.out.println("Erreur count offres : " + ex.getMessage());
        }

        // Données paginées
        int offset = (page - 1) * limit;
        List<OffreStage> items = new ArrayList<>();
        String dataSql = "SELECT * FROM offre_stage " + where + " ORDER BY id DESC LIMIT ? OFFSET ?";
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(limit);
        dataParams.add(offset);

        try (PreparedStatement ps = getCnx().prepareStatement(dataSql)) {
            for (int i = 0; i < dataParams.size(); i++) ps.setObject(i + 1, dataParams.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapRow(rs));
        } catch (SQLException ex) {
            System.out.println("Erreur pagination offres : " + ex.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        return result;
    }

    /** Toutes les offres d'une société (sans pagination) */
    public List<OffreStage> afficherParSociete(int societeId) {
        List<OffreStage> list = new ArrayList<>();
        String req = societeId < 0
                ? "SELECT * FROM offre_stage ORDER BY id DESC"
                : "SELECT * FROM offre_stage WHERE societe_id=? ORDER BY id DESC";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            if (societeId >= 0) ps.setInt(1, societeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            System.out.println("Erreur afficher offres : " + ex.getMessage());
        }
        return list;
    }

    /** Récupère une offre par son id */
    public Optional<OffreStage> findById(int id) {
        String req = "SELECT * FROM offre_stage WHERE id=?";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException ex) {
            System.out.println("Erreur findById offre : " + ex.getMessage());
        }
        return Optional.empty();
    }

    // Mappe un ResultSet vers un objet OffreStage
    private OffreStage mapRow(ResultSet rs) throws SQLException {
        OffreStage o = new OffreStage();
        o.setId(rs.getInt("id"));
        o.setTitre(rs.getString("titre"));
        o.setDescription(rs.getString("description"));
        o.setEntreprise(rs.getString("entreprise"));
        o.setDomaine(rs.getString("domaine"));
        o.setCompetencesRequises(rs.getString("competences_requises"));
        o.setProfilDemande(rs.getString("profil_demande"));
        o.setDuree(rs.getInt("duree"));
        o.setDateDebut(rs.getDate("date_debut"));
        o.setDateFin(rs.getDate("date_fin"));
        o.setTypeStage(rs.getString("type_stage"));
        o.setLieu(rs.getString("lieu"));
        o.setRemuneration(rs.getDouble("remuneration"));
        o.setContactEmail(rs.getString("contact_email"));
        o.setContactTel(rs.getString("contact_tel"));
        o.setStatut(rs.getString("statut"));
        o.setSocieteId(rs.getInt("societe_id"));
        return o;
    }
}