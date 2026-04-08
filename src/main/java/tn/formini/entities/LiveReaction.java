package tn.formini.entities;


import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LiveReaction {

    private int id;
    private String type;
    private Date created_at;


    private User user;


    private Evenement evenement;

    private static final List<String> TYPES_VALIDES = Arrays.asList(
            "❤️", "😂", "🔥", "👏", "🙌", "😮", "😢", "👍"
    );

    public LiveReaction() {}

    // ── Validation ─────────────────────────────────────────────────────────────

    public void valider() {
        if (type == null || type.trim().isEmpty())
            throw new IllegalArgumentException("Le type de réaction est obligatoire.");

        if (created_at == null)
            throw new IllegalArgumentException("La date de création est obligatoire.");
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) {
        if (type != null && !TYPES_VALIDES.contains(type))
            throw new IllegalArgumentException("Type de réaction invalide.");
        this.type = type;
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
        return "LiveReaction{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", created_at=" + created_at +
                ", user=" + user +
                ", evenement=" + evenement +
                '}';
    }
}
