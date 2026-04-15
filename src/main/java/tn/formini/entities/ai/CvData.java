package tn.formini.entities.ai;

import java.util.ArrayList;
import java.util.List;

public class CvData {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String profession;
    private String summary;
    private List<String> competences = new ArrayList<>();
    private List<ExperienceProfessionnelle> experiences = new ArrayList<>();
    private List<FormationAcademique> formations = new ArrayList<>();
    private List<String> langues = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();
    private List<String> interets = new ArrayList<>();

    public static class ExperienceProfessionnelle {
        private String titre;
        private String entreprise;
        private String dateDebut;
        private String dateFin;
        private String description;

        // Getters and Setters
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }
        public String getEntreprise() { return entreprise; }
        public void setEntreprise(String entreprise) { this.entreprise = entreprise; }
        public String getDateDebut() { return dateDebut; }
        public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
        public String getDateFin() { return dateFin; }
        public void setDateFin(String dateFin) { this.dateFin = dateFin; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class FormationAcademique {
        private String diplome;
        private String etablissement;
        private String dateDebut;
        private String dateFin;
        private String description;

        // Getters and Setters
        public String getDiplome() { return diplome; }
        public void setDiplome(String diplome) { this.diplome = diplome; }
        public String getEtablissement() { return etablissement; }
        public void setEtablissement(String etablissement) { this.etablissement = etablissement; }
        public String getDateDebut() { return dateDebut; }
        public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
        public String getDateFin() { return dateFin; }
        public void setDateFin(String dateFin) { this.dateFin = dateFin; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Getters and Setters for CvData
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getCompetences() { return competences; }
    public void setCompetences(List<String> competences) { this.competences = competences; }
    public List<ExperienceProfessionnelle> getExperiences() { return experiences; }
    public void setExperiences(List<ExperienceProfessionnelle> experiences) { this.experiences = experiences; }
    public List<FormationAcademique> getFormations() { return formations; }
    public void setFormations(List<FormationAcademique> formations) { this.formations = formations; }
    public List<String> getLangues() { return langues; }
    public void setLangues(List<String> langues) { this.langues = langues; }
    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }
    public List<String> getInterets() { return interets; }
    public void setInterets(List<String> interets) { this.interets = interets; }

    public void valider() {
        if (nom == null || nom.trim().isEmpty()) throw new IllegalArgumentException("Nom obligatoire");
        if (prenom == null || prenom.trim().isEmpty()) throw new IllegalArgumentException("Prénom obligatoire");
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email obligatoire");
    }
}
