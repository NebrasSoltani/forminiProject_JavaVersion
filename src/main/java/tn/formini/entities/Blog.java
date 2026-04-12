package tn.formini.entities;


import java.util.Date;

public class Blog {

    private int id;
    private String titre;
    private String contenu;
    private String image;
    private Date date_publication;
    private String categorie;
    private int auteur_id;
    private Integer evenement_id;
    private String resume;
    private boolean is_publie;
    private String tags;

    private User auteur;
    private Evenement evenement;

    public Blog() {}

    public void valider() {
        if (titre == null || titre.trim().isEmpty())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");

        if (contenu == null || contenu.trim().isEmpty())
            throw new IllegalArgumentException("Le contenu est obligatoire.");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Date getDate_publication() { return date_publication; }
    public void setDate_publication(Date date_publication) { this.date_publication = date_publication; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public int getAuteur_id() { return auteur_id; }
    public void setAuteur_id(int auteur_id) { this.auteur_id = auteur_id; }

    public Integer getEvenement_id() { return evenement_id; }
    public void setEvenement_id(Integer evenement_id) { this.evenement_id = evenement_id; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public boolean isIs_publie() { return is_publie; }
    public void setIs_publie(boolean is_publie) { this.is_publie = is_publie; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public User getAuteur() { return auteur; }
    public void setAuteur(User auteur) { this.auteur = auteur; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", image='" + image + '\'' +
                ", date_publication=" + date_publication +
                ", categorie='" + categorie + '\'' +
                ", auteur_id=" + auteur_id +
                ", evenement_id=" + evenement_id +
                ", resume='" + resume + '\'' +
                ", is_publie=" + is_publie +
                ", tags='" + tags + '\'' +
                '}';
    }
}