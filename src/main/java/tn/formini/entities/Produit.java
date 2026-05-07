package tn.formini.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "produit")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String categorie;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal prix;

    private int stock;

    // Union des deux champs: on garde 'image' pour la compatibilité avec le dashboard
    private String image;

    private String statut;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date_creation;

    public Produit() {
        this.date_creation = new Date();
    }

    public Produit(String nom, String description, String image, String categorie, BigDecimal prix, int stock, String statut) {
        this.nom = nom;
        this.description = description;
        this.image = image;
        this.categorie = categorie;
        this.prix = prix;
        this.stock = stock;
        this.statut = statut;
        this.date_creation = new Date();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    // Pour compatibilité avec l'ancien champ 'imageUrl' du controlleur REST
    public String getImageUrl() { return image; }
    public void setImageUrl(String imageUrl) { this.image = imageUrl; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Date getDate_creation() { return date_creation; }
    public void setDate_creation(Date date_creation) { this.date_creation = date_creation; }
}
