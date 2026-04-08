package tn.formini.entities;


public class Reponse {

    private int id;
    private String texte;
    private boolean est_correcte;
    private String explication_reponse;

    private Question question;

    public Reponse() {}

    public void valider() {
        if (texte == null || texte.trim().isEmpty())
            throw new IllegalArgumentException("Le texte de la réponse est obligatoire.");
        if (texte.length() > 500)
            throw new IllegalArgumentException("Le texte ne doit pas dépasser 500 caractères.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTexte() { return texte; }
    public void setTexte(String texte) {
        if (texte != null && texte.length() > 500)
            throw new IllegalArgumentException("Le texte ne doit pas dépasser 500 caractères.");
        this.texte = texte;
    }
    public boolean isEst_correcte() { return est_correcte; }
    public void setEst_correcte(boolean est_correcte) { this.est_correcte = est_correcte; }
    public String getExplication_reponse() { return explication_reponse; }
    public void setExplication_reponse(String e) { this.explication_reponse = e; }
    public Question getQuestion() { return question; }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @Override
    public String toString() { return "Reponse{id=" + id + ", correcte=" + est_correcte + "}"; }
}
