package tn.formini.entities.formations;


import tn.formini.entities.Users.User;

import java.math.BigDecimal;
import java.util.Date;

public class Inscription {

    private int id;
    private Date date_inscription;
    private String statut;
    private int progression;
    private Date date_terminee;
    private String mode_paiement;
    private BigDecimal montant_paye;
    private boolean certificat_obtenu;


    private User apprenant;


    private Formation formation;

    private static final String[] STATUTS_VALIDES = {"en_cours", "termine", "abandonne", "suspendu"};

    public Inscription() {}

    public void valider() {
        if (date_inscription == null)
            throw new IllegalArgumentException("La date d'inscription est obligatoire.");
        if (statut == null || statut.trim().isEmpty())
            throw new IllegalArgumentException("Le statut est obligatoire.");
        if (progression < 0 || progression > 100)
            throw new IllegalArgumentException("La progression doit être comprise entre 0 et 100.");
        if (montant_paye != null && montant_paye.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le montant payé ne peut pas être négatif.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getDate_inscription() { return date_inscription; }
    public void setDate_inscription(Date date_inscription) { this.date_inscription = date_inscription; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getProgression() { return progression; }
    public void setProgression(int progression) {
        if (progression < 0 || progression > 100)
            throw new IllegalArgumentException("La progression doit être entre 0 et 100.");
        this.progression = progression;
    }
    public Date getDate_terminee() { return date_terminee; }
    public void setDate_terminee(Date date_terminee) { this.date_terminee = date_terminee; }
    public String getMode_paiement() { return mode_paiement; }
    public void setMode_paiement(String mode_paiement) { this.mode_paiement = mode_paiement; }
    public BigDecimal getMontant_paye() { return montant_paye; }
    public void setMontant_paye(BigDecimal montant_paye) { this.montant_paye = montant_paye; }
    public boolean isCertificat_obtenu() { return certificat_obtenu; }
    public void setCertificat_obtenu(boolean certificat_obtenu) { this.certificat_obtenu = certificat_obtenu; }
    public User getApprenant() { return apprenant; }
    public Formation getFormation() { return formation; }

    public void setApprenant(User apprenant) {
        this.apprenant = apprenant;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    @Override
    public String toString() {
        return "Inscription{" +
                "id=" + id +
                ", date_inscription=" + date_inscription +
                ", statut='" + statut + '\'' +
                ", progression=" + progression +
                ", date_terminee=" + date_terminee +
                ", mode_paiement='" + mode_paiement + '\'' +
                ", montant_paye=" + montant_paye +
                ", certificat_obtenu=" + certificat_obtenu +
                ", apprenant=" + apprenant +
                ", formation=" + formation +
                '}';
    }
}

