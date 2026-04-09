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
        // Validation du genre (optionnel mais si fourni doit être valide)
        if (genre != null && !genre.trim().isEmpty()) {
            boolean ok = false;
            for (String g : GENRES_VALIDES) if (g.equalsIgnoreCase(genre.trim())) { ok = true; break; }
            if (!ok) throw new IllegalArgumentException("Genre invalide. Valeurs : homme, femme, autre.");
        }

        // Validation de l'état civil (optionnel mais si fourni doit être valide)
        if (etat_civil != null && !etat_civil.trim().isEmpty()) {
            boolean ok = false;
            for (String e : ETATS_CIVILS_VALIDES) if (e.equalsIgnoreCase(etat_civil.trim())) { ok = true; break; }
            if (!ok) throw new IllegalArgumentException("État civil invalide. Valeurs : celibataire, marie, divorce, veuf.");
        }

        // Validation de l'objectif (optionnel mais si fourni doit avoir une longueur raisonnable)
        if (objectif != null && !objectif.trim().isEmpty()) {
            if (objectif.length() > 500)
                throw new IllegalArgumentException("L'objectif ne doit pas dépasser 500 caractères.");
        }

        // Validation des domaines d'intérêt (optionnel mais si fourni doit être un JSON valide)
        if (domaines_interet != null && !domaines_interet.trim().isEmpty()) {
            if (domaines_interet.length() > 1000)
                throw new IllegalArgumentException("Les domaines d'intérêt ne doivent pas dépasser 1000 caractères.");
            
            // Validation basique du format JSON
            if (!domaines_interet.startsWith("[") || !domaines_interet.endsWith("]")) {
                throw new IllegalArgumentException("Les domaines d'intérêt doivent être au format JSON array.");
            }
        }

        // Validation de l'utilisateur obligatoire
        if (user == null)
            throw new IllegalArgumentException("L'utilisateur est obligatoire pour un apprenant.");
        
        // Valider l'utilisateur associé
        user.valider();

        // Validation du domaine (optionnel)
        if (domaine != null) {
            domaine.valider();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) {
        if (genre != null && !genre.trim().isEmpty()) {
            boolean ok = false;
            for (String g : GENRES_VALIDES) if (g.equalsIgnoreCase(genre.trim())) { ok = true; break; }
            if (!ok) throw new IllegalArgumentException("Genre invalide. Valeurs : homme, femme, autre.");
        }
        this.genre = genre;
    }
    public String getEtat_civil() { return etat_civil; }
    public void setEtat_civil(String etat_civil) {
        if (etat_civil != null && !etat_civil.trim().isEmpty()) {
            boolean ok = false;
            for (String e : ETATS_CIVILS_VALIDES) if (e.equalsIgnoreCase(etat_civil.trim())) { ok = true; break; }
            if (!ok) throw new IllegalArgumentException("État civil invalide. Valeurs : celibataire, marie, divorce, veuf.");
        }
        this.etat_civil = etat_civil;
    }
    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) {
        if (objectif != null && objectif.length() > 500)
            throw new IllegalArgumentException("L'objectif ne doit pas dépasser 500 caractères.");
        this.objectif = objectif;
    }
    public String getDomaines_interet() { return domaines_interet; }
    public void setDomaines_interet(String domaines_interet) {
        if (domaines_interet != null && !domaines_interet.trim().isEmpty()) {
            if (domaines_interet.length() > 1000)
                throw new IllegalArgumentException("Les domaines d'intérêt ne doivent pas dépasser 1000 caractères.");
            if (!domaines_interet.startsWith("[") || !domaines_interet.endsWith("]"))
                throw new IllegalArgumentException("Les domaines d'intérêt doivent être au format JSON array.");
        }
        this.domaines_interet = domaines_interet;
    }

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
