package tn.formini.entities.Users;


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
        // Validation de la spécialité (obligatoire)
        if (specialite == null || specialite.trim().isEmpty())
            throw new IllegalArgumentException("La spécialité est obligatoire.");
        if (specialite.length() > 255)
            throw new IllegalArgumentException("La spécialité ne doit pas dépasser 255 caractères.");

        // Validation de la bio (optionnelle mais si fournie doit avoir une longueur raisonnable)
        if (bio != null && !bio.trim().isEmpty()) {
            if (bio.length() > 2000)
                throw new IllegalArgumentException("La bio ne doit pas dépasser 2000 caractères.");
        }

        // Validation des années d'expérience (optionnelles mais si fournies doivent être valides)
        if (experience_annees != null) {
            if (experience_annees < 0)
                throw new IllegalArgumentException("Les années d'expérience ne peuvent pas être négatives.");
            if (experience_annees > 70)
                throw new IllegalArgumentException("Les années d'expérience ne peuvent pas dépasser 70 ans.");
        }

        // Validation de la note moyenne (optionnelle mais si fournie doit être valide)
        if (note_moyenne != null) {
            if (note_moyenne < 0 || note_moyenne > 5)
                throw new IllegalArgumentException("La note moyenne doit être comprise entre 0 et 5.");
        }

        // Validation des URLs (optionnelles mais si fournies doivent être valides)
        if (linkedin != null && linkedin.length() > 500)
            throw new IllegalArgumentException("L'URL LinkedIn ne doit pas dépasser 500 caractères.");

        if (portfolio != null && portfolio.length() > 500)
            throw new IllegalArgumentException("L'URL portfolio ne doit pas dépasser 500 caractères.");

        if (cv != null && cv.length() > 500)
            throw new IllegalArgumentException("L'URL du CV ne doit pas dépasser 500 caractères.");

        // Validation de l'utilisateur obligatoire
        if (user == null)
            throw new IllegalArgumentException("L'utilisateur est obligatoire pour un formateur.");
        
        // Valider l'utilisateur associé
        user.valider();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) {
        if (specialite != null && !specialite.trim().isEmpty()) {
            if (specialite.length() > 255)
                throw new IllegalArgumentException("La spécialité ne doit pas dépasser 255 caractères.");
        }
        this.specialite = specialite;
    }
    public String getBio() { return bio; }
    public void setBio(String bio) {
        if (bio != null && bio.length() > 2000)
            throw new IllegalArgumentException("La bio ne doit pas dépasser 2000 caractères.");
        this.bio = bio;
    }
    public Integer getExperience_annees() { return experience_annees; }
    public void setExperience_annees(Integer experience_annees) {
        if (experience_annees != null && experience_annees < 0)
            throw new IllegalArgumentException("Les années d'expérience ne peuvent pas être négatives.");
        this.experience_annees = experience_annees;
    }
    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) {
        if (linkedin != null && linkedin.length() > 500)
            throw new IllegalArgumentException("L'URL LinkedIn ne doit pas dépasser 500 caractères.");
        this.linkedin = linkedin;
    }
    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) {
        if (portfolio != null && portfolio.length() > 500)
            throw new IllegalArgumentException("L'URL portfolio ne doit pas dépasser 500 caractères.");
        this.portfolio = portfolio;
    }
    public String getCv() { return cv; }
    public void setCv(String cv) {
        if (cv != null && cv.length() > 500)
            throw new IllegalArgumentException("L'URL du CV ne doit pas dépasser 500 caractères.");
        this.cv = cv;
    }
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


