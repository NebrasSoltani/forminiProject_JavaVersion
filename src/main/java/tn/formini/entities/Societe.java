package tn.formini.entities;


public class Societe {

    private int id;
    private String nom_societe;
    private String secteur;
    private String description;
    private String adresse;
    private String site_web;


    private User user;

    public Societe() {}

    public void valider() {
        if (nom_societe == null || nom_societe.trim().isEmpty())
            throw new IllegalArgumentException("Le nom de la société est obligatoire.");
        if (nom_societe.length() > 255)
            throw new IllegalArgumentException("Le nom de la société ne doit pas dépasser 255 caractères.");
        if (site_web != null && !site_web.isEmpty() && !site_web.startsWith("http"))
            throw new IllegalArgumentException("L'URL du site web doit commencer par http.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom_societe() { return nom_societe; }
    public void setNom_societe(String nom_societe) { this.nom_societe = nom_societe; }
    public String getSecteur() { return secteur; }
    public void setSecteur(String secteur) { this.secteur = secteur; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getSite_web() { return site_web; }
    public void setSite_web(String site_web) {
        if (site_web != null && !site_web.isEmpty() && !site_web.startsWith("http"))
            throw new IllegalArgumentException("L'URL du site web doit commencer par http.");
        this.site_web = site_web;
    }

    public User getUser() { return user; }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() { return "Societe{id=" + id + ", nom='" + nom_societe + "'}"; }
}
