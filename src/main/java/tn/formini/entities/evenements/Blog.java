package tn.formini.entities.evenements;


import tn.formini.entities.Users.User;

import java.util.Date;

public class Blog {

    private int id;
    private String titre;
    private String contenu;
    private String image;
    private Date date_publication;
    private String categorie;
    private int user_id;
    private String resume_auto;
    private int is_actif;
    private int live;
    private String url_live;

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

    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) {
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
        this.categorie = categorie;
    }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getResume_auto() { return resume_auto; }
    public void setResume_auto(String resume_auto) { this.resume_auto = resume_auto; }

    public int getIs_actif() { return is_actif; }
    public void setIs_actif(int is_actif) { this.is_actif = is_actif; }

    public int getLive() { return live; }
    public void setLive(int live) { this.live = live; }

    public String getUrl_live() { return url_live; }
    public void setUrl_live(String url_live) { this.url_live = url_live; }

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
                ", user_id=" + user_id +
                ", resume_auto='" + resume_auto + '\'' +
                ", is_actif=" + is_actif +
                ", live=" + live +
                ", url_live='" + url_live + '\'' +
                '}';
    }
}

