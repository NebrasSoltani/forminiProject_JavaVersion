package tn.formini.entities;


import java.util.Date;

public class Blog {

    private int id;
    private String titre;
    private String contenu;
    private String image;
    private Date date_publication;
    private String categorie;
    private boolean is_publie;
    private String resume;
    private String tags; // JSON


    private User auteur; // objet lié

    private Evenement evenement; // objet lié (peut être null)

    public Blog() {}

    // ── Validation ─────────────────────────────────────────────────────────────

    public void valider() {
        if (titre == null || titre.trim().isEmpty())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");

        if (contenu == null || contenu.trim().isEmpty())
            throw new IllegalArgumentException("Le contenu est obligatoire.");

        if (date_publication == null)
            throw new IllegalArgumentException("La date de publication est obligatoire.");

        if (categorie == null || categorie.trim().isEmpty())
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        if (categorie.length() > 100)
            throw new IllegalArgumentException("La catégorie ne doit pas dépasser 100 caractères.");


    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) {
        if (titre != null && titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");
        this.titre = titre;
    }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Date getDate_publication() { return date_publication; }
    public void setDate_publication(Date date_publication) { this.date_publication = date_publication; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) {
        if (categorie != null && categorie.length() > 100)
            throw new IllegalArgumentException("La catégorie ne doit pas dépasser 100 caractères.");
        this.categorie = categorie;
    }

    public boolean isIs_publie() { return is_publie; }
    public void setIs_publie(boolean is_publie) { this.is_publie = is_publie; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public User getAuteur() {
        return auteur;
    }

    public void setAuteur(User auteur) {
        this.auteur = auteur;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", image='" + image + '\'' +
                ", date_publication=" + date_publication +
                ", categorie='" + categorie + '\'' +
                ", is_publie=" + is_publie +
                ", resume='" + resume + '\'' +
                ", tags='" + tags + '\'' +
                ", auteur=" + auteur +
                ", evenement=" + evenement +
                '}';
    }
}

