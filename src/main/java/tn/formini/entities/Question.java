package tn.formini.entities;


public class Question {

    private int id;
    private String enonce;
    private String type;
    private int points;
    private int ordre;
    private String explication;
    private String explications_detaillees;


    private Quiz quiz;

    private static final String[] TYPES_VALIDES = {"qcm", "vrai_faux", "texte_libre", "correspondance"};

    public Question() {}

    public void valider() {
        if (enonce == null || enonce.trim().isEmpty())
            throw new IllegalArgumentException("L'énoncé est obligatoire.");
        if (type == null || type.trim().isEmpty())
            throw new IllegalArgumentException("Le type est obligatoire.");
        if (points < 0)
            throw new IllegalArgumentException("Les points ne peuvent pas être négatifs.");
        if (ordre < 0)
            throw new IllegalArgumentException("L'ordre ne peut pas être négatif.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEnonce() { return enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getPoints() { return points; }
    public void setPoints(int points) {
        if (points < 0) throw new IllegalArgumentException("Les points ne peuvent pas être négatifs.");
        this.points = points;
    }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public String getExplication() { return explication; }
    public void setExplication(String explication) { this.explication = explication; }
    public String getExplications_detaillees() { return explications_detaillees; }
    public void setExplications_detaillees(String e) { this.explications_detaillees = e; }

    public Quiz getQuiz() { return quiz; }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    @Override
    public String toString() { return "Question{id=" + id + ", type='" + type + "', points=" + points + "}"; }
}
