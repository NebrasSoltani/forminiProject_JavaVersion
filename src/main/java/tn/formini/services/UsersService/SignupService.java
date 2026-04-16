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

    public SignupService() {
        this.userService = new UserService();
        this.apprenantService = new ApprenantService();
        this.formateurService = new FormateurService();
    }

    public SignupService(UserService userService, ApprenantService apprenantService, FormateurService formateurService) {
        this.userService = userService;
        this.apprenantService = apprenantService;
        this.formateurService = formateurService;
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

        apprenant.valider();

        userService.ajouter(user);
        if (user.getId() <= 0) {
            throw new IllegalStateException("Impossible de créer le compte utilisateur.");
        }

        apprenantService.ajouter(apprenant);
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

        return formateur;
    }

    private static void normalizeUserEmail(User user) {
        if (user.getEmail() == null) {
            return;
        }
        user.setEmail(user.getEmail().trim());
    }
}
