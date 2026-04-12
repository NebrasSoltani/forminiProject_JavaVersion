package tn.formini.entities.Users;


import java.util.Date;


public class User {

    private int id;
    private String email;
    private String roles; // JSON
    private String password;
    private String nom;
    private String prenom;
    private String telephone;
    private String gouvernorat;
    private Date date_naissance;
    private String profession;
    private String niveau_etude;
    private String role_utilisateur;
    private String photo;
    private boolean is_email_verified;
    private String email_verification_token;
    private Date email_verification_token_expires_at;
    private Date email_verified_at;
    private String google_id;
    private String github_id;
    private String oauth_provider;
    private String avatar_url;
    private String google_authenticator_secret;
    private String backup_codes; // JSON
    private boolean email_auth_enabled;
    private boolean google_auth_enabled;
    private boolean phone_verified;
    private Date phone_verified_at;

    private static final String[] ROLES_VALIDES = {"admin", "formateur", "apprenant", "societe"};

    public User() {}

    // ── Validation ────────────────────────────────────────────────────────────

    public void valider() {
        // Validation de l'email (obligatoire)
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("L'email est obligatoire.");
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Format d'email invalide.");
        if (email.length() > 255)
            throw new IllegalArgumentException("L'email ne doit pas dépasser 255 caractères.");

        // Validation du mot de passe (obligatoire)
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        if (password.length() > 255)
            throw new IllegalArgumentException("Le mot de passe ne doit pas dépasser 255 caractères.");
        if (!password.matches(".*[A-Z].*"))
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une majuscule.");
        if (!password.matches(".*[a-z].*"))
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une minuscule.");
        if (!password.matches(".*[0-9].*"))
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre.");

        // Validation du nom (obligatoire)
        if (nom == null || nom.trim().isEmpty())
            throw new IllegalArgumentException("Le nom est obligatoire.");
        if (nom.length() > 100)
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 100 caractères.");
        if (!nom.matches("^[a-zA-Z\\u00C0-\\u017F\\s\\-']+$"))
            throw new IllegalArgumentException("Le nom ne doit contenir que des lettres, espaces, tirets et apostrophes.");

        // Validation du prénom (obligatoire)
        if (prenom == null || prenom.trim().isEmpty())
            throw new IllegalArgumentException("Le prénom est obligatoire.");
        if (prenom.length() > 100)
            throw new IllegalArgumentException("Le prénom ne doit pas dépasser 100 caractères.");
        if (!prenom.matches("^[a-zA-Z\\u00C0-\\u017F\\s\\-']+$"))
            throw new IllegalArgumentException("Le prénom ne doit contenir que des lettres, espaces, tirets et apostrophes.");

        // Validation du téléphone (obligatoire)
        if (telephone == null || telephone.trim().isEmpty())
            throw new IllegalArgumentException("Le téléphone est obligatoire.");
        if (!telephone.matches("^\\+?[0-9]{8,12}$"))
            throw new IllegalArgumentException("Format de téléphone invalide (8 à 12 chiffres).");

        // Validation de la date de naissance (obligatoire)
        if (date_naissance == null)
            throw new IllegalArgumentException("La date de naissance est obligatoire.");
        if (date_naissance.after(new Date()))
            throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur.");
        
        // Vérifier que l'utilisateur a au moins 13 ans
        Date minDate = new Date(System.currentTimeMillis() - (13L * 365 * 24 * 60 * 60 * 1000));
        if (date_naissance.after(minDate))
            throw new IllegalArgumentException("L'utilisateur doit avoir au moins 13 ans.");

        // Validation du rôle utilisateur (obligatoire)
        if (role_utilisateur == null || role_utilisateur.trim().isEmpty())
            throw new IllegalArgumentException("Le rôle utilisateur est obligatoire.");
        boolean roleOk = false;
        for (String r : ROLES_VALIDES) {
            if (r.equalsIgnoreCase(role_utilisateur)) { roleOk = true; break; }
        }
        if (!roleOk)
            throw new IllegalArgumentException("Rôle invalide. Valeurs acceptées : admin, formateur, apprenant, societe.");

        // Validation du gouvernorat (optionnel)
        if (gouvernorat != null && !gouvernorat.trim().isEmpty()) {
            if (gouvernorat.length() > 50)
                throw new IllegalArgumentException("Le gouvernorat ne doit pas dépasser 50 caractères.");
        }

        // Validation des champs optionnels
        if (profession != null && profession.length() > 100)
            throw new IllegalArgumentException("La profession ne doit pas dépasser 100 caractères.");
        
        if (niveau_etude != null && niveau_etude.length() > 100)
            throw new IllegalArgumentException("Le niveau d'étude ne doit pas dépasser 100 caractères.");
        
        if (photo != null && photo.length() > 500)
            throw new IllegalArgumentException("L'URL de la photo ne doit pas dépasser 500 caractères.");
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
                throw new IllegalArgumentException("Format d'email invalide.");
            if (email.length() > 255)
                throw new IllegalArgumentException("L'email ne doit pas dépasser 255 caractères.");
        }
        this.email = email;
    }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 8)
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
            if (password.length() > 255)
                throw new IllegalArgumentException("Le mot de passe ne doit pas dépasser 255 caractères.");
            if (!password.matches(".*[A-Z].*"))
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins une majuscule.");
            if (!password.matches(".*[a-z].*"))
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins une minuscule.");
            if (!password.matches(".*[0-9].*"))
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre.");
        }
        this.password = password;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) {
        if (nom != null && !nom.trim().isEmpty()) {
            if (nom.length() > 100)
                throw new IllegalArgumentException("Le nom ne doit pas dépasser 100 caractères.");
            if (!nom.matches("^[a-zA-Z\\u00C0-\\u017F\\s\\-']+$"))
                throw new IllegalArgumentException("Le nom ne doit contenir que des lettres, espaces, tirets et apostrophes.");
        }
        this.nom = nom;
    }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) {
        if (prenom != null && !prenom.trim().isEmpty()) {
            if (prenom.length() > 100)
                throw new IllegalArgumentException("Le prénom ne doit pas dépasser 100 caractères.");
            if (!prenom.matches("^[a-zA-Z\\u00C0-\\u017F\\s\\-']+$"))
                throw new IllegalArgumentException("Le prénom ne doit contenir que des lettres, espaces, tirets et apostrophes.");
        }
        this.prenom = prenom;
    }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) {
        if (telephone != null && !telephone.matches("^\\+?[0-9]{8,12}$"))
            throw new IllegalArgumentException("Format de téléphone invalide.");
        this.telephone = telephone;
    }

    public String getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(String gouvernorat) {
        if (gouvernorat != null && gouvernorat.length() > 50)
            throw new IllegalArgumentException("Le gouvernorat ne doit pas dépasser 50 caractères.");
        this.gouvernorat = gouvernorat;
    }

    public Date getDate_naissance() { return date_naissance; }
    public void setDate_naissance(Date date_naissance) {
        if (date_naissance != null && date_naissance.after(new Date()))
            throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur.");
        this.date_naissance = date_naissance;
    }

    public String getProfession() { return profession; }
    public void setProfession(String profession) {
        if (profession != null && profession.length() > 100)
            throw new IllegalArgumentException("La profession ne doit pas dépasser 100 caractères.");
        this.profession = profession;
    }

    public String getNiveau_etude() { return niveau_etude; }
    public void setNiveau_etude(String niveau_etude) {
        if (niveau_etude != null && niveau_etude.length() > 100)
            throw new IllegalArgumentException("Le niveau d'étude ne doit pas dépasser 100 caractères.");
        this.niveau_etude = niveau_etude;
    }

    public String getRole_utilisateur() { return role_utilisateur; }
    public void setRole_utilisateur(String role_utilisateur) { this.role_utilisateur = role_utilisateur; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) {
        if (photo != null && photo.length() > 500)
            throw new IllegalArgumentException("L'URL de la photo ne doit pas dépasser 500 caractères.");
        this.photo = photo;
    }

    public boolean isIs_email_verified() { return is_email_verified; }
    public void setIs_email_verified(boolean is_email_verified) { this.is_email_verified = is_email_verified; }

    public String getEmail_verification_token() { return email_verification_token; }
    public void setEmail_verification_token(String t) { this.email_verification_token = t; }

    public Date getEmail_verification_token_expires_at() { return email_verification_token_expires_at; }
    public void setEmail_verification_token_expires_at(Date d) { this.email_verification_token_expires_at = d; }

    public Date getEmail_verified_at() { return email_verified_at; }
    public void setEmail_verified_at(Date email_verified_at) { this.email_verified_at = email_verified_at; }

    public String getGoogle_id() { return google_id; }
    public void setGoogle_id(String google_id) { this.google_id = google_id; }

    public String getGithub_id() { return github_id; }
    public void setGithub_id(String github_id) { this.github_id = github_id; }

    public String getOauth_provider() { return oauth_provider; }
    public void setOauth_provider(String oauth_provider) { this.oauth_provider = oauth_provider; }

    public String getAvatar_url() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }

    public String getGoogle_authenticator_secret() { return google_authenticator_secret; }
    public void setGoogle_authenticator_secret(String s) { this.google_authenticator_secret = s; }

    public String getBackup_codes() { return backup_codes; }
    public void setBackup_codes(String backup_codes) { this.backup_codes = backup_codes; }

    public boolean isEmail_auth_enabled() { return email_auth_enabled; }
    public void setEmail_auth_enabled(boolean email_auth_enabled) { this.email_auth_enabled = email_auth_enabled; }

    public boolean isGoogle_auth_enabled() { return google_auth_enabled; }
    public void setGoogle_auth_enabled(boolean google_auth_enabled) { this.google_auth_enabled = google_auth_enabled; }

    public boolean isPhone_verified() { return phone_verified; }
    public void setPhone_verified(boolean phone_verified) { this.phone_verified = phone_verified; }

    public Date getPhone_verified_at() { return phone_verified_at; }
    public void setPhone_verified_at(Date phone_verified_at) { this.phone_verified_at = phone_verified_at; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role='" + role_utilisateur + "'}";
    }
}
