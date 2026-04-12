package tn.formini.entities.evenements;


import tn.formini.entities.Users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Evenement {

    private int id;
    private String titre;
    private String description;
    private Date date_debut;
    private Date date_fin;
    private String lieu;
    private String image;
    private Integer nombre_places;
    private boolean is_actif;
    private String type;
    private String filieres;      // JSON
    private String tags;          // JSON
    private String image360;
    private String url_street_view;
    private String resume_auto;
    private Date resume_generated_at;
    private String live_summary_data; // JSON
    private String url_live;
    private boolean live;
    private String stream_url;


    private User organisateur; // objet lié

    /** 1-N : participations à cet événement */
    private List<ParticipationEvenement> participations = new ArrayList<>();

    /** 1-N : commentaires live */
    private List<LiveComment> liveComments = new ArrayList<>();

    /** 1-N : réactions live */
    private List<LiveReaction> liveReactions = new ArrayList<>();

    /** 1-N : blogs liés à cet événement */
    private List<Blog> blogs = new ArrayList<>();

    private static final String[] TYPES_VALIDES = {
            "conference", "atelier", "webinaire", "musique",
            "technologie", "exposition", "formation", "innovation"
    };

    public Evenement() {}

    // ── Validation ─────────────────────────────────────────────────────────────

    public void valider() {
        if (titre == null || titre.trim().isEmpty())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");

        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("La description est obligatoire.");

        if (date_debut == null)
            throw new IllegalArgumentException("La date de début est obligatoire.");
        if (date_fin == null)
            throw new IllegalArgumentException("La date de fin est obligatoire.");
        if (date_fin.before(date_debut))
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");

        if (lieu == null || lieu.trim().isEmpty())
            throw new IllegalArgumentException("Le lieu est obligatoire.");
        if (lieu.length() > 255)
            throw new IllegalArgumentException("Le lieu ne doit pas dépasser 255 caractères.");

        if (type == null || type.trim().isEmpty())
            throw new IllegalArgumentException("Le type est obligatoire.");
        boolean typeOk = false;
        for (String t : TYPES_VALIDES) {
            if (t.equalsIgnoreCase(type)) { typeOk = true; break; }
        }
        if (!typeOk)
            throw new IllegalArgumentException("Type invalide. Valeurs acceptées : conference, atelier, webinaire, musique, technologie, exposition, formation, innovation.");

        if (nombre_places != null && nombre_places < 0)
            throw new IllegalArgumentException("Le nombre de places ne peut pas être négatif.");


        if (url_live != null && !url_live.isEmpty() && !url_live.startsWith("http"))
            throw new IllegalArgumentException("L'URL live doit commencer par http.");
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) {
        if (titre != null && titre.length() > 255)
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 255 caractères.");
        this.titre = titre;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate_debut() { return date_debut; }
    public void setDate_debut(Date date_debut) { this.date_debut = date_debut; }

    public Date getDate_fin() { return date_fin; }
    public void setDate_fin(Date date_fin) {
        if (date_fin != null && date_debut != null && date_fin.before(date_debut))
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        this.date_fin = date_fin;
    }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getNombre_places() { return nombre_places; }
    public void setNombre_places(Integer nombre_places) {
        if (nombre_places != null && nombre_places < 0)
            throw new IllegalArgumentException("Le nombre de places ne peut pas être négatif.");
        this.nombre_places = nombre_places;
    }

    public boolean isIs_actif() { return is_actif; }
    public void setIs_actif(boolean is_actif) { this.is_actif = is_actif; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFilieres() { return filieres; }
    public void setFilieres(String filieres) { this.filieres = filieres; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getImage360() { return image360; }
    public void setImage360(String image360) { this.image360 = image360; }

    public String getUrl_street_view() { return url_street_view; }
    public void setUrl_street_view(String url_street_view) { this.url_street_view = url_street_view; }

    public String getResume_auto() { return resume_auto; }
    public void setResume_auto(String resume_auto) { this.resume_auto = resume_auto; }

    public Date getResume_generated_at() { return resume_generated_at; }
    public void setResume_generated_at(Date resume_generated_at) { this.resume_generated_at = resume_generated_at; }

    public String getLive_summary_data() { return live_summary_data; }
    public void setLive_summary_data(String live_summary_data) { this.live_summary_data = live_summary_data; }

    public String getUrl_live() { return url_live; }
    public void setUrl_live(String url_live) { this.url_live = url_live; }

    public boolean isLive() { return live; }
    public void setLive(boolean live) { this.live = live; }

    public String getStream_url() { return stream_url; }
    public void setStream_url(String stream_url) { this.stream_url = stream_url; }



    public User getOrganisateur() { return organisateur; }


    public List<ParticipationEvenement> getParticipations() { return participations; }
    public void setParticipations(List<ParticipationEvenement> participations) { this.participations = participations; }

    public List<LiveComment> getLiveComments() { return liveComments; }
    public void setLiveComments(List<LiveComment> liveComments) { this.liveComments = liveComments; }

    public List<LiveReaction> getLiveReactions() { return liveReactions; }
    public void setLiveReactions(List<LiveReaction> liveReactions) { this.liveReactions = liveReactions; }

    public List<Blog> getBlogs() { return blogs; }
    public void setBlogs(List<Blog> blogs) { this.blogs = blogs; }

    public void setOrganisateur(User organisateur) {
        this.organisateur = organisateur;
    }

    @Override
    public String toString() {
        return "Evenement{id=" + id + ", titre='" + titre + "', type='" + type + "'}";
    }
}

