package tn.formini.entities.formations;

import tn.formini.entities.Users.User;

import java.math.BigDecimal;
import java.util.Date;

public class Formation {

    private int id;
    private String titre;
    private String categorie;
    private String niveau;
    private String langue;
    private String description_courte;
    private String description_detaillee;
    private String objectifs_pedagogiques;
    private String prerequis;
    private String programme;
    private int duree;
    private int nombre_lecons;
    private String format;
    private Date date_debut;
    private String planning;
    private String lien_live;
    private Integer nombre_seances;
    private String type_acces;
    private BigDecimal prix;
    private String type_achat;
    private BigDecimal prix_promo;
    private Date date_fin_promo;
    private String image_couverture;
    private String video_promo;
    private String statut;
    private Date date_creation;
    private Date date_publication;
    private boolean certificat;
    private boolean has_quiz;
    private boolean fichiers_telechargeables;
    private boolean forum;


    private User formateur;

    private static final String[] NIVEAUX_VALIDES = {"debutant", "intermediaire", "avance"};
    private static final String[] STATUTS_VALIDES = {"brouillon", "publie", "archive"};
    private static final String[] TYPES_ACCES_VALIDES = {"gratuit", "payant", "sur_invitation"};

    public Formation() {}

    public void valider() {
        if (titre == null || titre.trim().isEmpty())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");
        if (categorie == null || categorie.trim().isEmpty())
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        if (niveau == null || niveau.trim().isEmpty())
            throw new IllegalArgumentException("Le niveau est obligatoire.");
        if (description_courte != null && description_courte.length() > 500)
            throw new IllegalArgumentException("La description courte ne doit pas dépasser 500 caractères.");
        if (duree <= 0)
            throw new IllegalArgumentException("La durée doit être supérieure à 0.");
        if (nombre_lecons < 0)
            throw new IllegalArgumentException("Le nombre de leçons ne peut pas être négatif.");
        if (prix != null && prix.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
        if (statut == null || statut.trim().isEmpty())
            throw new IllegalArgumentException("Le statut est obligatoire.");

        if (date_creation == null)
            throw new IllegalArgumentException("La date de création est obligatoire.");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getLangue() { return langue; }
    public void setLangue(String langue) { this.langue = langue; }
    public String getDescription_courte() { return description_courte; }
    public void setDescription_courte(String d) { this.description_courte = d; }
    public String getDescription_detaillee() { return description_detaillee; }
    public void setDescription_detaillee(String d) { this.description_detaillee = d; }
    public String getObjectifs_pedagogiques() { return objectifs_pedagogiques; }
    public void setObjectifs_pedagogiques(String o) { this.objectifs_pedagogiques = o; }
    public String getPrerequis() { return prerequis; }
    public void setPrerequis(String prerequis) { this.prerequis = prerequis; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public int getDuree() { return duree; }
    public void setDuree(int duree) {
        if (duree <= 0) throw new IllegalArgumentException("La durée doit être supérieure à 0.");
        this.duree = duree;
    }
    public int getNombre_lecons() { return nombre_lecons; }
    public void setNombre_lecons(int nombre_lecons) { this.nombre_lecons = nombre_lecons; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Date getDate_debut() { return date_debut; }
    public void setDate_debut(Date date_debut) { this.date_debut = date_debut; }
    public String getPlanning() { return planning; }
    public void setPlanning(String planning) { this.planning = planning; }
    public String getLien_live() { return lien_live; }
    public void setLien_live(String lien_live) { this.lien_live = lien_live; }
    public Integer getNombre_seances() { return nombre_seances; }
    public void setNombre_seances(Integer nombre_seances) { this.nombre_seances = nombre_seances; }
    public String getType_acces() { return type_acces; }
    public void setType_acces(String type_acces) { this.type_acces = type_acces; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) {
        if (prix != null && prix.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
        this.prix = prix;
    }
    public String getType_achat() { return type_achat; }
    public void setType_achat(String type_achat) { this.type_achat = type_achat; }
    public BigDecimal getPrix_promo() { return prix_promo; }
    public void setPrix_promo(BigDecimal prix_promo) { this.prix_promo = prix_promo; }
    public Date getDate_fin_promo() { return date_fin_promo; }
    public void setDate_fin_promo(Date date_fin_promo) { this.date_fin_promo = date_fin_promo; }
    public String getImage_couverture() { return image_couverture; }
    public void setImage_couverture(String image_couverture) { this.image_couverture = image_couverture; }
    public String getVideo_promo() { return video_promo; }
    public void setVideo_promo(String video_promo) { this.video_promo = video_promo; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Date getDate_creation() { return date_creation; }
    public void setDate_creation(Date date_creation) { this.date_creation = date_creation; }
    public Date getDate_publication() { return date_publication; }
    public void setDate_publication(Date date_publication) { this.date_publication = date_publication; }
    public boolean isCertificat() { return certificat; }
    public void setCertificat(boolean certificat) { this.certificat = certificat; }
    public boolean isHas_quiz() { return has_quiz; }
    public void setHas_quiz(boolean has_quiz) { this.has_quiz = has_quiz; }
    public boolean isFichiers_telechargeables() { return fichiers_telechargeables; }
    public void setFichiers_telechargeables(boolean f) { this.fichiers_telechargeables = f; }
    public boolean isForum() { return forum; }
    public void setForum(boolean forum) { this.forum = forum; }
    public User getFormateur() { return formateur; }

    public void setFormateur(User formateur) {
        this.formateur = formateur;
    }

    @Override
    public String toString() { return "Formation{id=" + id + ", titre='" + titre + "', statut='" + statut + "'}"; }
}

