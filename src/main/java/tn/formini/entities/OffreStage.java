package tn.formini.entities;


import java.util.Date;

public class OffreStage {

    private int id;
    private String titre;
    private String description;
    private String entreprise;
    private String domaine;
    private String competences_requises;
    private String profil_demande;
    private String duree;
    private Date date_debut;
    private Date date_fin;
    private String type_stage;
    private String lieu;
    private String remuneration;
    private String contact_email;
    private String contact_tel;
    private String statut;
    private Date date_publication;

    private User societe;

    private static final String[] STATUTS_VALIDES = {"ouvert", "ferme", "en_attente"};

    public OffreStage() {}

    public void valider() {
        if (titre == null || titre.trim().isEmpty())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("La description est obligatoire.");
        if (entreprise == null || entreprise.trim().isEmpty())
            throw new IllegalArgumentException("L'entreprise est obligatoire.");
        if (lieu == null || lieu.trim().isEmpty())
            throw new IllegalArgumentException("Le lieu est obligatoire.");
        if (duree == null || duree.trim().isEmpty())
            throw new IllegalArgumentException("La durée est obligatoire.");
        if (type_stage == null || type_stage.trim().isEmpty())
            throw new IllegalArgumentException("Le type de stage est obligatoire.");
        if (statut == null || statut.trim().isEmpty())
            throw new IllegalArgumentException("Le statut est obligatoire.");
        if (date_publication == null)
            throw new IllegalArgumentException("La date de publication est obligatoire.");
        if (date_debut != null && date_fin != null && date_fin.before(date_debut))
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        if (contact_email != null && !contact_email.isEmpty()
                && !contact_email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Format de l'email de contact invalide.");
        if (contact_tel != null && !contact_tel.isEmpty() && !contact_tel.matches("^\\+?[0-9]{8,15}$"))
            throw new IllegalArgumentException("Format du téléphone de contact invalide.");

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }
    public String getDomaine() { return domaine; }
    public void setDomaine(String domaine) { this.domaine = domaine; }
    public String getCompetences_requises() { return competences_requises; }
    public void setCompetences_requises(String c) { this.competences_requises = c; }
    public String getProfil_demande() { return profil_demande; }
    public void setProfil_demande(String profil_demande) { this.profil_demande = profil_demande; }
    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
    public Date getDate_debut() { return date_debut; }
    public void setDate_debut(Date date_debut) { this.date_debut = date_debut; }
    public Date getDate_fin() { return date_fin; }
    public void setDate_fin(Date date_fin) { this.date_fin = date_fin; }
    public String getType_stage() { return type_stage; }
    public void setType_stage(String type_stage) { this.type_stage = type_stage; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getRemuneration() { return remuneration; }
    public void setRemuneration(String remuneration) { this.remuneration = remuneration; }
    public String getContact_email() { return contact_email; }
    public void setContact_email(String contact_email) { this.contact_email = contact_email; }
    public String getContact_tel() { return contact_tel; }
    public void setContact_tel(String contact_tel) { this.contact_tel = contact_tel; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Date getDate_publication() { return date_publication; }
    public void setDate_publication(Date date_publication) { this.date_publication = date_publication; }

    public User getSociete() { return societe; }

    public void setSociete(User societe) {
        this.societe = societe;
    }

    @Override
    public String toString() { return "OffreStage{id=" + id + ", titre='" + titre + "', statut='" + statut + "'}"; }
}


