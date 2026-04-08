package tn.formini.entities;


import java.util.Date;

public class LiveComment {

    private int id;
    private String content;
    private Date created_at;


    private User user;


    private Evenement evenement;

    public LiveComment() {}

    // ── Validation ─────────────────────────────────────────────────────────────

    public void valider() {
        if (content == null || content.trim().isEmpty())
            throw new IllegalArgumentException("Le contenu du commentaire est obligatoire.");
        if (content.length() > 5000)
            throw new IllegalArgumentException("Le commentaire ne doit pas dépasser 5000 caractères.");

        if (created_at == null)
            throw new IllegalArgumentException("La date de création est obligatoire.");
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) {
        if (content != null && content.length() > 5000)
            throw new IllegalArgumentException("Le commentaire ne doit pas dépasser 5000 caractères.");
        this.content = content;
    }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }



    public User getUser() { return user; }




    public Evenement getEvenement() { return evenement; }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    @Override
    public String toString() {
        return "LiveComment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", created_at=" + created_at +
                ", user=" + user +
                ", evenement=" + evenement +
                '}';
    }
}

