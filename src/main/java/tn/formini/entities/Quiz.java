package tn.formini.entities;

public class Quiz {

    private int id;
    private String titre;
    private String description;
    private int duree;
    private int note_minimale;
    private boolean afficher_correction;
    private boolean melanger;

    private Formation formation;

    public Quiz() {}

    public void valider() {
        if (titre == null || titre.trim().isEmpty())
            throw new IllegalArgumentException("Le titre du quiz est obligatoire.");
        if (titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");
        if (description != null && description.length() > 500)
            throw new IllegalArgumentException("La description ne doit pas dépasser 500 caractères.");
        if (duree <= 0)
            throw new IllegalArgumentException("La durée du quiz doit être supérieure à 0.");
        if (note_minimale < 0 || note_minimale > 100)
            throw new IllegalArgumentException("La note minimale doit être comprise entre 0 et 100.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDuree() { return duree; }
    public void setDuree(int duree) {
        if (duree <= 0) throw new IllegalArgumentException("La durée doit être supérieure à 0.");
        this.duree = duree;
    }
    public int getNote_minimale() { return note_minimale; }
    public void setNote_minimale(int note_minimale) {
        if (note_minimale < 0 || note_minimale > 100)
            throw new IllegalArgumentException("La note minimale doit être entre 0 et 100.");
        this.note_minimale = note_minimale;
    }
    public boolean isAfficher_correction() { return afficher_correction; }
    public void setAfficher_correction(boolean afficher_correction) { this.afficher_correction = afficher_correction; }
    public boolean isMelanger() { return melanger; }
    public void setMelanger(boolean melanger) { this.melanger = melanger; }
    public Formation getFormation() { return formation; }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    @Override
    public String toString() { return "Quiz{id=" + id + ", titre='" + titre + "', duree=" + duree + "min}"; }
}