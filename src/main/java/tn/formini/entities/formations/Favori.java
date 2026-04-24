package tn.formini.entities.formations;


import tn.formini.entities.Users.User;

import java.util.Date;

public class Favori {

    private int id;
    private Date date_ajout;


    private User apprenant;

    private Formation formation;

    public Favori() {}

    public void valider() {
        if (date_ajout == null)
            throw new IllegalArgumentException("La date d'ajout est obligatoire.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getDate_ajout() { return date_ajout; }
    public void setDate_ajout(Date date_ajout) { this.date_ajout = date_ajout; }
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
        return "Favori{" +
                "id=" + id +
                ", date_ajout=" + date_ajout +
                ", apprenant=" + apprenant +
                ", formation=" + formation +
                '}';
    }
}

