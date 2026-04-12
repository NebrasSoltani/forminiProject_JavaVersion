package tn.formini.entities.Users;


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
        // Validation du nom de la société (obligatoire)
        if (nom_societe == null || nom_societe.trim().isEmpty())
            throw new IllegalArgumentException("Le nom de la société est obligatoire.");
        if (nom_societe.length() > 255)
            throw new IllegalArgumentException("Le nom de la société ne doit pas dépasser 255 caractères.");

        // Validation du secteur (optionnel mais si fourni doit avoir une longueur raisonnable)
        if (secteur != null && !secteur.trim().isEmpty()) {
            if (secteur.length() > 100)
                throw new IllegalArgumentException("Le secteur ne doit pas dépasser 100 caractères.");
        }

        // Validation de la description (optionnelle mais si fournie doit avoir une longueur raisonnable)
        if (description != null && !description.trim().isEmpty()) {
            if (description.length() > 2000)
                throw new IllegalArgumentException("La description ne doit pas dépasser 2000 caractères.");
        }

        // Validation de l'adresse (optionnelle mais si fournie doit avoir une longueur raisonnable)
        if (adresse != null && !adresse.trim().isEmpty()) {
            if (adresse.length() > 500)
                throw new IllegalArgumentException("L'adresse ne doit pas dépasser 500 caractères.");
        }

        // Validation du site web (optionnel mais si fourni doit être valide)
        if (site_web != null && !site_web.trim().isEmpty()) {
            if (!site_web.startsWith("http://") && !site_web.startsWith("https://"))
                throw new IllegalArgumentException("L'URL du site web doit commencer par http:// ou https://.");
            if (site_web.length() > 500)
                throw new IllegalArgumentException("L'URL du site web ne doit pas dépasser 500 caractères.");
        }

        // Validation de l'utilisateur obligatoire
        if (user == null)
            throw new IllegalArgumentException("L'utilisateur est obligatoire pour une société.");
        
        // Valider l'utilisateur associé
        user.valider();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom_societe() { return nom_societe; }
    public void setNom_societe(String nom_societe) {
        if (nom_societe != null && !nom_societe.trim().isEmpty()) {
            if (nom_societe.length() > 255)
                throw new IllegalArgumentException("Le nom de la société ne doit pas dépasser 255 caractères.");
        }
        this.nom_societe = nom_societe;
    }
    public String getSecteur() { return secteur; }
    public void setSecteur(String secteur) {
        if (secteur != null && secteur.length() > 100)
            throw new IllegalArgumentException("Le secteur ne doit pas dépasser 100 caractères.");
        this.secteur = secteur;
    }
    public String getDescription() { return description; }
    public void setDescription(String description) {
        if (description != null && description.length() > 2000)
            throw new IllegalArgumentException("La description ne doit pas dépasser 2000 caractères.");
        this.description = description;
    }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) {
        if (adresse != null && adresse.length() > 500)
            throw new IllegalArgumentException("L'adresse ne doit pas dépasser 500 caractères.");
        this.adresse = adresse;
    }
    public String getSite_web() { return site_web; }
    public void setSite_web(String site_web) {
        if (site_web != null && !site_web.trim().isEmpty()) {
            if (!site_web.startsWith("http://") && !site_web.startsWith("https://"))
                throw new IllegalArgumentException("L'URL du site web doit commencer par http:// ou https://.");
            if (site_web.length() > 500)
                throw new IllegalArgumentException("L'URL du site web ne doit pas dépasser 500 caractères.");
        }
        this.site_web = site_web;
    }

    public User getUser() { return user; }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() { return "Societe{id=" + id + ", nom='" + nom_societe + "'}"; }
}
