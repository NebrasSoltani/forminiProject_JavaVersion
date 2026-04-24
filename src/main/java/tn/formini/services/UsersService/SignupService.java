package tn.formini.services.UsersService;

import tn.formini.entities.Users.Apprenant;
import tn.formini.entities.Users.Formateur;
import tn.formini.entities.Users.User;

/**
 * Inscription : crée un {@link User} puis le profil {@link Apprenant} ou {@link Formateur}.
 * Le rôle Symfony-style ({@code roles}) et {@code role_utilisateur} sont imposés par ce service.
 */
public class SignupService {

    private final UserService userService;
    private final ApprenantService apprenantService;
    private final FormateurService formateurService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    public SignupService() {
        this.userService = new UserService();
        this.apprenantService = new ApprenantService();
        this.formateurService = new FormateurService();
        this.emailVerificationService = new EmailVerificationService();
        this.emailService = new EmailService();
    }

    public SignupService(UserService userService, ApprenantService apprenantService, FormateurService formateurService,
                         EmailVerificationService emailVerificationService, EmailService emailService) {
        this.userService = userService;
        this.apprenantService = apprenantService;
        this.formateurService = formateurService;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    /**
     * Inscrit un apprenant : enregistre l'utilisateur avec ROLE_APPRENANT puis la ligne apprenant.
     *
     * @throws IllegalArgumentException validation métier ou email déjà utilisé
     * @throws IllegalStateException    échec de persistance du compte utilisateur
     */
    public Apprenant signupApprenant(Apprenant apprenant) {
        if (apprenant == null) {
            throw new IllegalArgumentException("L'apprenant est obligatoire.");
        }
        User user = apprenant.getUser();
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur est obligatoire pour l'inscription apprenant.");
        }

        normalizeUserEmail(user);
        if (userService.emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        user.setRole_utilisateur("apprenant");
        user.setRoles("[\"ROLE_APPRENANT\"]");
        user.setIs_email_verified(false); // Email not verified initially

        apprenant.valider();

        userService.ajouter(user);
        if (user.getId() <= 0) {
            throw new IllegalStateException("Impossible de créer le compte utilisateur.");
        }

        apprenantService.ajouter(apprenant);

        // Generate and send verification email
        sendVerificationEmail(user);

        return apprenant;
    }

    /**
     * Inscrit un formateur : enregistre l'utilisateur avec ROLE_FORMATEUR puis la ligne formateur.
     *
     * @throws IllegalArgumentException validation métier ou email déjà utilisé
     * @throws IllegalStateException    échec de persistance du compte utilisateur ou du profil formateur
     */
    public Formateur signupFormateur(Formateur formateur) {
        if (formateur == null) {
            throw new IllegalArgumentException("Le formateur est obligatoire.");
        }
        User user = formateur.getUser();
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur est obligatoire pour l'inscription formateur.");
        }

        normalizeUserEmail(user);
        if (userService.emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        user.setRole_utilisateur("formateur");
        user.setRoles("[\"ROLE_FORMATEUR\"]");
        user.setIs_email_verified(false); // Email not verified initially

        formateur.valider();

        userService.ajouter(user);
        if (user.getId() <= 0) {
            throw new IllegalStateException("Impossible de créer le compte utilisateur.");
        }

        formateurService.ajouter(formateur);
        if (formateur.getId() <= 0) {
            userService.supprimer(user.getId());
            throw new IllegalStateException("Impossible de créer le profil formateur.");
        }

        // Generate and send verification email
        sendVerificationEmail(user);

        return formateur;
    }

    /**
     * Generate and send verification email to user
     * @param user User object
     */
    private void sendVerificationEmail(User user) {
        try {
            String token = emailVerificationService.generateAndSaveToken(user.getId());
            if (token != null) {
                String fullName = user.getNom() + " " + user.getPrenom();
                emailService.sendVerificationEmail(user.getEmail(), fullName, token);
                System.out.println("Verification email sent to: " + user.getEmail());
            } else {
                System.err.println("Failed to generate verification token for user: " + user.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Error sending verification email: " + e.getMessage());
            // Don't fail signup if email fails - user can request resend later
        }
    }

    private static void normalizeUserEmail(User user) {
        if (user.getEmail() == null) {
            return;
        }
        user.setEmail(user.getEmail().trim());
    }
}
