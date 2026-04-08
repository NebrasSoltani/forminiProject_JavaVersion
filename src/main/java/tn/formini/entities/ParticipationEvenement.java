package tn.formini.entities;


import java.util.Date;

public class ParticipationEvenement {

    private int id;
    private Date date_participation;


    private User user;


    private Evenement evenement;

    public ParticipationEvenement() {}

    // ── Validation ─────────────────────────────────────────────────────────────

    public void valider() {
        if (date_participation == null)
            throw new IllegalArgumentException("La date de participation est obligatoire.");
        if (date_participation.after(new Date()))
            throw new IllegalArgumentException("La date de participation ne peut pas être dans le futur.");
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDate_participation() { return date_participation; }
    public void setDate_participation(Date date_participation) {
        if (date_participation != null && date_participation.after(new Date()))
            throw new IllegalArgumentException("La date de participation ne peut pas être dans le futur.");
        this.date_participation = date_participation;
    }



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
        return "ParticipationEvenement{" +
                "id=" + id +
                ", date_participation=" + date_participation +
                ", user=" + user +
                ", evenement=" + evenement +
                '}';
    }
}

