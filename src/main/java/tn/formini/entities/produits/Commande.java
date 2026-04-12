package tn.formini.entities.produits;

import tn.formini.entities.Users.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commande {

    private int id;
    private String reference;
    private Date date_commande;
    private String statut;
    private BigDecimal total;
    private String adresse_livraison;
    private String telephone;


    private User utilisateur;

    /** 1-N : lignes de commande */
    private List<CommandeItem> items = new ArrayList<>();

    private static final String[] STATUTS_VALIDES = {"en_attente", "confirmee", "expediee", "livree", "annulee"};

    public Commande() {}

    public void valider() {
        if (reference == null || reference.trim().isEmpty())
            throw new IllegalArgumentException("La référence est obligatoire.");
        if (reference.length() > 100)
            throw new IllegalArgumentException("La référence ne doit pas dépasser 100 caractères.");
        if (date_commande == null)
            throw new IllegalArgumentException("La date de commande est obligatoire.");
        if (statut == null || statut.trim().isEmpty())
            throw new IllegalArgumentException("Le statut est obligatoire.");
        if (total == null)
            throw new IllegalArgumentException("Le total est obligatoire.");
        if (total.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le total ne peut pas être négatif.");
        if (telephone != null && !telephone.isEmpty() && !telephone.matches("^\\+?[0-9]{8,15}$"))
            throw new IllegalArgumentException("Format de téléphone invalide.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Date getDate_commande() { return date_commande; }
    public void setDate_commande(Date date_commande) { this.date_commande = date_commande; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) {
        if (total != null && total.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le total ne peut pas être négatif.");
        this.total = total;
    }
    public String getAdresse_livraison() { return adresse_livraison; }
    public void setAdresse_livraison(String adresse_livraison) { this.adresse_livraison = adresse_livraison; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }


    public User getUtilisateur() { return utilisateur; }
    public void setUtilisateur(User utilisateur) {
        this.utilisateur = utilisateur;
    }    public List<CommandeItem> getItems() { return items; }
    public void setItems(List<CommandeItem> items) { this.items = items; }

    @Override
    public String toString() { return "Commande{id=" + id + ", ref='" + reference + "', total=" + total + ", statut='" + statut + "'}"; }
}
