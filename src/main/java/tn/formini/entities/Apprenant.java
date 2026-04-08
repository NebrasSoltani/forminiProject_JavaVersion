package tn.formini.entities;


public class Apprenant {

    private int id;
    private String genre;
    private String etat_civil;
    private String objectif;
    private String domaines_interet; // JSON


    private User user;


    private Domaine domaine;

    private static final String[] GENRES_VALIDES = {"homme", "femme", "autre"};
    private static final String[] ETATS_CIVILS_VALIDES = {"celibataire", "marie", "divorce", "veuf"};

    public Apprenant() {}

    public void valider() {
     if (genre != null) {
            boolean ok = false;
            for (String g : GENRES_VALIDES) if (g.equalsIgnoreCase(genre)) { ok = true; break; }
            if (!ok) throw new IllegalArgumentException("Genre invalide. Valeurs : homme, femme, autre.");
        }
        if (etat_civil != null) {
            boolean ok = false;
            for (String e : ETATS_CIVILS_VALIDES) if (e.equalsIgnoreCase(etat_civil)) { ok = true; break; }
            if (!ok) throw new IllegalArgumentException("État civil invalide. Valeurs : celibataire, marie, divorce, veuf.");
        }

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getEtat_civil() { return etat_civil; }
    public void setEtat_civil(String etat_civil) { this.etat_civil = etat_civil; }
    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) { this.objectif = objectif; }
    public String getDomaines_interet() { return domaines_interet; }
    public void setDomaines_interet(String domaines_interet) { this.domaines_interet = domaines_interet; }

    public User getUser() { return user; }

    public Domaine getDomaine() { return domaine; }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDomaine(Domaine domaine) {
        this.domaine = domaine;
    }

    @Override
    public String toString() {
        return "Apprenant{" +
                "id=" + id +
                ", genre='" + genre + '\'' +
                ", etat_civil='" + etat_civil + '\'' +
                ", objectif='" + objectif + '\'' +
                ", domaines_interet='" + domaines_interet + '\'' +
                ", user=" + user +
                ", domaine=" + domaine +
                '}';
    }
}
