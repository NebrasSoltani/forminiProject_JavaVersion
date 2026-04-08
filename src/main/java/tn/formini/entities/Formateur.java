package tn.formini.entities;


public class Formateur {

    private int id;
    private String specialite;
    private String bio;
    private Integer experience_annees;
    private String linkedin;
    private String portfolio;
    private String cv;
    private Double note_moyenne;


    private User user;

    public Formateur() {}

    public void valider() {
 if (experience_annees != null && experience_annees < 0)
            throw new IllegalArgumentException("Les années d'expérience ne peuvent pas être négatives.");
        if (note_moyenne != null && (note_moyenne < 0 || note_moyenne > 5))
            throw new IllegalArgumentException("La note moyenne doit être comprise entre 0 et 5.");
        if (linkedin != null && !linkedin.isEmpty() && !linkedin.startsWith("http"))
            throw new IllegalArgumentException("L'URL LinkedIn doit commencer par http.");
        if (specialite != null && specialite.length() > 255)
            throw new IllegalArgumentException("La spécialité ne doit pas dépasser 255 caractères.");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Integer getExperience_annees() { return experience_annees; }
    public void setExperience_annees(Integer experience_annees) {
        if (experience_annees != null && experience_annees < 0)
            throw new IllegalArgumentException("Les années d'expérience ne peuvent pas être négatives.");
        this.experience_annees = experience_annees;
    }
    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }
    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }
    public String getCv() { return cv; }
    public void setCv(String cv) { this.cv = cv; }
    public Double getNote_moyenne() { return note_moyenne; }
    public void setNote_moyenne(Double note_moyenne) {
        if (note_moyenne != null && (note_moyenne < 0 || note_moyenne > 5))
            throw new IllegalArgumentException("La note moyenne doit être comprise entre 0 et 5.");
        this.note_moyenne = note_moyenne;
    }

    public User getUser() { return user; }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() { return "Formateur{id=" + id + ", specialite='" + specialite + "'}"; }
}


