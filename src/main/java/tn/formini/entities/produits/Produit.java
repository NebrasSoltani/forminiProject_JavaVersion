package tn.formini.entities.produits;

import java.math.BigDecimal;
import java.util.Date;

public class Produit {

    private int id;
    private String nom;
    private String categorie;
    private String description;
    private BigDecimal prix;
    private int stock;
    private String image;
    private String statut;
    private Date date_creation;

    private static final String[] STATUTS_VALIDES = {"disponible", "epuise", "archive"};

    public Produit() {}

    public void valider() {
        if (nom == null || nom.trim().isEmpty())
            throw new IllegalArgumentException("Le nom du produit est obligatoire.");
        if (nom.length() > 255)
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 255 caractères.");
        if (categorie == null || categorie.trim().isEmpty())
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("La description est obligatoire.");
        if (prix == null)
            throw new IllegalArgumentException("Le prix est obligatoire.");
        if (prix.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
        if (stock < 0)
            throw new IllegalArgumentException("Le stock ne peut pas être négatif.");
        if (statut == null || statut.trim().isEmpty())
            throw new IllegalArgumentException("Le statut est obligatoire.");
        if (date_creation == null)
            throw new IllegalArgumentException("La date de création est obligatoire.");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) {
        if (prix != null && prix.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
        this.prix = prix;
    }
    public int getStock() { return stock; }
    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Le stock ne peut pas être négatif.");
        this.stock = stock;
    }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Date getDate_creation() { return date_creation; }
    public void setDate_creation(Date date_creation) { this.date_creation = date_creation; }

    @Override
    public String toString() { return "Produit{id=" + id + ", nom='" + nom + "', prix=" + prix + ", stock=" + stock + "}"; }
}

