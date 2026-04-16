package tn.formini.entities.produits;

import java.math.BigDecimal;

public class CommandeItem {

    private int id;
    private String nom_produit;
    private int quantite;
    private BigDecimal prix_unitaire;

    private Commande commande;

    private Produit produit;

    public CommandeItem() {}

    public void valider() {
        if (nom_produit == null || nom_produit.trim().isEmpty())
            throw new IllegalArgumentException("Le nom du produit est obligatoire.");
        if (nom_produit.length() > 255)
            throw new IllegalArgumentException("Le nom du produit ne doit pas dépasser 255 caractères.");
        if (quantite <= 0)
            throw new IllegalArgumentException("La quantité doit être supérieure à 0.");
        if (prix_unitaire == null)
            throw new IllegalArgumentException("Le prix unitaire est obligatoire.");
        if (prix_unitaire.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom_produit() { return nom_produit; }
    public void setNom_produit(String nom_produit) { this.nom_produit = nom_produit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("La quantité doit être supérieure à 0.");
        this.quantite = quantite;
    }
    public BigDecimal getPrix_unitaire() { return prix_unitaire; }
    public void setPrix_unitaire(BigDecimal prix_unitaire) { this.prix_unitaire = prix_unitaire; }
    public Commande getCommande() { return commande; }
    public Produit getProduit() { return produit; }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public BigDecimal getSousTotal() {
        return (prix_unitaire != null) ? prix_unitaire.multiply(new BigDecimal(quantite)) : BigDecimal.ZERO;
    }

    @Override
    public String toString() { return "CommandeItem{id=" + id + ", produit='" + nom_produit + "', qte=" + quantite + ", prix=" + prix_unitaire + "}"; }
}

