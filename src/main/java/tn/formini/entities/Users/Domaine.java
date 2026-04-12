package tn.formini.entities.Users;


public class Domaine {

    private int id;
    private String nom;

    public Domaine() {}

    public void valider() {
        if (nom == null || nom.trim().isEmpty())
            throw new IllegalArgumentException("Le nom du domaine est obligatoire.");
        if (nom.length() > 100)
            throw new IllegalArgumentException("Le nom du domaine ne doit pas dépasser 100 caractères.");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) {
        if (nom != null && nom.length() > 100)
            throw new IllegalArgumentException("Le nom du domaine ne doit pas dépasser 100 caractères.");
        this.nom = nom;
    }

    @Override
    public String toString() { return "Domaine{id=" + id + ", nom='" + nom + "'}"; }
}

