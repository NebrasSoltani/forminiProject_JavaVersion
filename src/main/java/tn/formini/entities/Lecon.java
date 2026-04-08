package tn.formini.entities;

public class Lecon {

    private int id;
    private String titre;
    private String description;
    private String contenu;
    private int ordre;
    private Integer duree;
    private String video_url;
    private String fichier;
    private boolean gratuit;


    private Formation formation;

    public Lecon() {}

    public void valider() {
        if (titre == null || titre.trim().isEmpty())
            throw new IllegalArgumentException("Le titre de la leçon est obligatoire.");
        if (titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");
        if (ordre < 0)
            throw new IllegalArgumentException("L'ordre ne peut pas être négatif.");
        if (duree != null && duree <= 0)
            throw new IllegalArgumentException("La durée doit être supérieure à 0.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) {
        if (ordre < 0) throw new IllegalArgumentException("L'ordre ne peut pas être négatif.");
        this.ordre = ordre;
    }
    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }
    public String getVideo_url() { return video_url; }
    public void setVideo_url(String video_url) { this.video_url = video_url; }
    public String getFichier() { return fichier; }
    public void setFichier(String fichier) { this.fichier = fichier; }
    public boolean isGratuit() { return gratuit; }
    public void setGratuit(boolean gratuit) { this.gratuit = gratuit; }
    public Formation getFormation() { return formation; }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    @Override
    public String toString() { return "Lecon{id=" + id + ", titre='" + titre + "', ordre=" + ordre + "}"; }
}


