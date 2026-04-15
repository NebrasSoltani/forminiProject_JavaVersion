package tn.formini.services.produitsService;

import tn.formini.entities.Users.User;
import tn.formini.entities.produits.Commande;
import tn.formini.entities.produits.CommandeItem;
import tn.formini.services.cart.CartItem;
import tn.formini.tools.MyDataBase;
import tn.formini.tools.SessionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CommandeService {

    private Connection getCnx() {
        return MyDataBase.getInstance().getCnx();
    }

    /**
     * Crée une commande et ses lignes à partir du panier.
     * Hypothèses de tables:
     * - commande(id PK AI, reference, date_commande, statut, total, adresse_livraison, telephone, user_id NULL)
     * - commande_item(id PK AI, nom_produit, quantite, prix_unitaire, commande_id, produit_id NULL)
     */
    public int createOrderFromCart(List<CartItem> cartItems, String adresseLivraison, String telephone) throws SQLException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Panier vide");
        }

        Connection cnx = getCnx();
        if (cnx == null) {
            throw new SQLException("Connexion DB indisponible");
        }

        User user = SessionManager.getCurrentUser();
        Integer userId = (user != null) ? user.getId() : null;

        BigDecimal total = cartItems.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String reference = "CMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Date now = new Date();

        // Stock validation (fail fast)
        for (CartItem ci : cartItems) {
            int qte = ci.getQuantity();
            int stock = ci.getProduit().getStock();
            if (qte > stock) {
                throw new IllegalArgumentException("Stock insuffisant pour: " + ci.getProduit().getNom()
                        + " (stock=" + stock + ", demandé=" + qte + ")");
            }
        }

        boolean oldAutoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            int commandeId = insertCommande(cnx, reference, now, total, adresseLivraison, telephone, userId);
            insertCommandeItems(cnx, commandeId, cartItems);
            decrementStock(cnx, cartItems);

            cnx.commit();
            return commandeId;
        } catch (Exception e) {
            cnx.rollback();
            if (e instanceof SQLException) throw (SQLException) e;
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        } finally {
            cnx.setAutoCommit(oldAutoCommit);
        }
    }

    private int insertCommande(Connection cnx,
                              String reference,
                              Date dateCommande,
                              BigDecimal total,
                              String adresseLivraison,
                              String telephone,
                              Integer userId) throws SQLException {

        String sql = "INSERT INTO commande (reference, date_commande, statut, total, adresse_livraison, telephone, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reference);
            ps.setTimestamp(2, new Timestamp(dateCommande.getTime()));
            ps.setString(3, "en_attente");
            ps.setBigDecimal(4, total);
            ps.setString(5, adresseLivraison);
            ps.setString(6, telephone);
            if (userId == null) ps.setNull(7, java.sql.Types.INTEGER);
            else ps.setInt(7, userId);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Impossible de récupérer l'id de la commande");
    }

    private void insertCommandeItems(Connection cnx, int commandeId, List<CartItem> cartItems) throws SQLException {
        String sql = "INSERT INTO commande_item (nom_produit, quantite, prix_unitaire, commande_id, produit_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (CartItem ci : cartItems) {
                ps.setString(1, ci.getProduit().getNom());
                ps.setInt(2, ci.getQuantity());
                ps.setBigDecimal(3, ci.getProduit().getPrix());
                ps.setInt(4, commandeId);
                ps.setInt(5, ci.getProduit().getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void decrementStock(Connection cnx, List<CartItem> cartItems) throws SQLException {
        String sql = "UPDATE produit SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (CartItem ci : cartItems) {
                ps.setInt(1, ci.getQuantity());
                ps.setInt(2, ci.getProduit().getId());
                ps.setInt(3, ci.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Optional: helper mapping if needed later
    public Commande toCommandeEntity(int id, String reference, Date date, String statut, BigDecimal total, String adresse, String tel) {
        Commande c = new Commande();
        c.setId(id);
        c.setReference(reference);
        c.setDate_commande(date);
        c.setStatut(statut);
        c.setTotal(total);
        c.setAdresse_livraison(adresse);
        c.setTelephone(tel);
        return c;
    }

    public CommandeItem toCommandeItemEntity(int id, String nom, int qte, BigDecimal prixUnitaire) {
        CommandeItem i = new CommandeItem();
        i.setId(id);
        i.setNom_produit(nom);
        i.setQuantite(qte);
        i.setPrix_unitaire(prixUnitaire);
        return i;
    }
}

