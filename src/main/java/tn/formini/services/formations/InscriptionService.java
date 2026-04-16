package tn.formini.services.formations;

import tn.formini.entities.formations.Formation;
import tn.formini.entities.formations.Inscription;
import tn.formini.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscriptionService {
    private Connection cnx;

    public InscriptionService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public List<Formation> findFormationsByApprenant(int userId) {
        List<Formation> formations = new ArrayList<>();
        String req = "SELECT f.* FROM formation f " +
                     "JOIN inscription i ON f.id = i.formation_id " +
                     "WHERE i.apprenant_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Formation f = new Formation();
                f.setId(rs.getInt("id"));
                f.setTitre(rs.getString("titre"));
                f.setCategorie(rs.getString("categorie"));
                f.setNiveau(rs.getString("niveau"));
                formations.add(f);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur findFormationsByApprenant : " + ex.getMessage());
        }
        return formations;
    }
}
