package tn.formini.utils;

import java.util.regex.Pattern;

/**
 * Règles alignées sur {@link tn.formini.controllers.auth.SignupController} (email, téléphone, mot de passe).
 */
public final class SignupFieldValidation {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private SignupFieldValidation() {
    }

    /** Retire espaces / séparateurs ; conserve un + initial. */
    public static String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        boolean plus = t.startsWith("+");
        String digits = t.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return plus ? "+" : "";
        }
        return plus ? "+" + digits : digits;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String e = email.trim();
        return !e.isEmpty() && EMAIL_PATTERN.matcher(e).matches();
    }

    public static boolean isValidPhoneNormalized(String normalized) {
        return normalized != null && normalized.matches("\\+?[0-9]{8,12}$");
    }

    /** @return null si le mot de passe respecte les règles, sinon un message court. */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est obligatoire";
        }
        if (password.length() < 8) {
            return "Minimum 8 caractères";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Une majuscule requise";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Une minuscule requise";
        }
        if (!password.matches(".*\\d.*")) {
            return "Un chiffre requis";
        }
        return null;
    }

    public static boolean isValidNomPrenom(String s) {
        if (s == null) {
            return false;
        }
        String t = s.trim();
        return t.length() >= 2;
    }
}
