package tn.formini.entities;

import java.math.BigDecimal;
import java.util.Date;

public class ResultatQuiz {

    private int id;
    private BigDecimal note;
    private int nombre_bonnes_reponses;
    private int nombre_total_questions;
    private Date date_tentative;
    private boolean reussi;
    private String details_reponses;


    private User apprenant;

    private Quiz quiz;

    public ResultatQuiz() {}

    public void valider() {
        if (note == null)
            throw new IllegalArgumentException("La note est obligatoire.");
        if (note.compareTo(BigDecimal.ZERO) < 0 || note.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("La note doit être comprise entre 0 et 100.");
        if (nombre_bonnes_reponses < 0)
            throw new IllegalArgumentException("Le nombre de bonnes réponses ne peut pas être négatif.");
        if (nombre_total_questions <= 0)
            throw new IllegalArgumentException("Le nombre total de questions doit être supérieur à 0.");
        if (nombre_bonnes_reponses > nombre_total_questions)
            throw new IllegalArgumentException("Les bonnes réponses ne peuvent pas dépasser le total des questions.");
        if (date_tentative == null)
            throw new IllegalArgumentException("La date de tentative est obligatoire.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public BigDecimal getNote() { return note; }
    public void setNote(BigDecimal note) { this.note = note; }
    public int getNombre_bonnes_reponses() { return nombre_bonnes_reponses; }
    public void setNombre_bonnes_reponses(int n) { this.nombre_bonnes_reponses = n; }
    public int getNombre_total_questions() { return nombre_total_questions; }
    public void setNombre_total_questions(int n) { this.nombre_total_questions = n; }
    public Date getDate_tentative() { return date_tentative; }
    public void setDate_tentative(Date date_tentative) { this.date_tentative = date_tentative; }
    public boolean isReussi() { return reussi; }
    public void setReussi(boolean reussi) { this.reussi = reussi; }
    public String getDetails_reponses() { return details_reponses; }
    public void setDetails_reponses(String d) { this.details_reponses = d; }

    public User getApprenant() { return apprenant; }

    public void setApprenant(User apprenant) {
        this.apprenant = apprenant;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Quiz getQuiz() { return quiz; }

    @Override
    public String toString() { return "ResultatQuiz{id=" + id + ", note=" + note + ", reussi=" + reussi + "}"; }
}

