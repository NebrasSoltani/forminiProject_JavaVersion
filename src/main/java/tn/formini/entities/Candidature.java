package tn.formini.entities;

import java.util.Date;

public class Candidature {

    private int id;
    private String statut;
    private String lettre_motivation;
    private String cv;
    private Date date_candidature;
    private String commentaire;


    private OffreStage offreStage;


    private User apprenant;

    private static final String[] STATUTS_VALIDES = {"en_attente", "acceptee", "refusee", "en_cours"};

    public Candidature() {}

    public void valider() {
        if (statut == null || statut.trim().isEmpty())
            throw new IllegalArgumentException("Le statut est obligatoire.");
        boolean statutOk = false;
        for (String s : STATUTS_VALIDES) if (s.equalsIgnoreCase(statut)) { statutOk = true; break; }
        if (!statutOk)
            throw new IllegalArgumentException("Statut invalide. Valeurs : en_attente, acceptee, refusee, en_cours.");
        if (lettre_motivation == null || lettre_motivation.trim().isEmpty())
            throw new IllegalArgumentException("La lettre de motivation est obligatoire.");
        if (date_candidature == null)
            throw new IllegalArgumentException("La date de candidature est obligatoire.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getLettre_motivation() { return lettre_motivation; }
    public void setLettre_motivation(String lettre_motivation) { this.lettre_motivation = lettre_motivation; }
    public String getCv() { return cv; }
    public void setCv(String cv) { this.cv = cv; }
    public Date getDate_candidature() { return date_candidature; }
    public void setDate_candidature(Date date_candidature) { this.date_candidature = date_candidature; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public OffreStage getOffreStage() { return offreStage; }
    public User getApprenant() { return apprenant; }

    public void setOffreStage(OffreStage offreStage) {
        this.offreStage = offreStage;
    }

    public void setApprenant(User apprenant) {
        this.apprenant = apprenant;
    }

    @Override
    public String toString() {
        return "Candidature{" +
                "id=" + id +
                ", statut='" + statut + '\'' +
                ", lettre_motivation='" + lettre_motivation + '\'' +
                ", cv='" + cv + '\'' +
                ", date_candidature=" + date_candidature +
                ", commentaire='" + commentaire + '\'' +
                ", offreStage=" + offreStage +
                ", apprenant=" + apprenant +
                '}';
    }
}
