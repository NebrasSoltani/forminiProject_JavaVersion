package tn.formini.entities.formations;


import tn.formini.entities.Users.User;

import java.util.Date;

public class ProgressionLecon {

    private int id;
    private boolean terminee;
    private Date date_terminee;


    private User apprenant;

    private Lecon lecon;

    public ProgressionLecon() {}

    public void valider() {

        if (terminee && date_terminee == null)
            throw new IllegalArgumentException("La date de fin est obligatoire lorsque la leçon est terminée.");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public boolean isTerminee() { return terminee; }
    public void setTerminee(boolean terminee) { this.terminee = terminee; }
    public Date getDate_terminee() { return date_terminee; }
    public void setDate_terminee(Date date_terminee) { this.date_terminee = date_terminee; }
    public User getApprenant() { return apprenant; }
    public Lecon getLecon() { return lecon; }

    public void setApprenant(User apprenant) {
        this.apprenant = apprenant;
    }

    public void setLecon(Lecon lecon) {
        this.lecon = lecon;
    }

    @Override
    public String toString() {
        return "ProgressionLecon{" +
                "id=" + id +
                ", terminee=" + terminee +
                ", date_terminee=" + date_terminee +
                ", apprenant=" + apprenant +
                ", lecon=" + lecon +
                '}';
    }
}
